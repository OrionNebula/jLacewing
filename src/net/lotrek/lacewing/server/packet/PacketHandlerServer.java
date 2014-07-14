package net.lotrek.lacewing.server.packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.server.LacewingServer;
import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public class PacketHandlerServer extends Thread
{
	private ServerClient client;
	private LacewingServer server;
	private HashMap<Class<? extends ServerReadPacket>, MethodObjectPair> packetTriggers = new HashMap<Class<? extends ServerReadPacket>, MethodObjectPair>();
	
	public void run()
	{
		try {
			if(client.getInputStream().read() != 0)
				client.disconnectClient();
		} catch (IOException | LacewingException e) {
			e.printStackTrace();
		}
		
		registerPacketTrigger(ServerReadPacket0Request0Connect.class, new MethodObjectPair("handleConnectPacket", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket0Request1SetName.class, new MethodObjectPair("handleNameSet", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket0Request2JoinChannel.class, new MethodObjectPair("handleJoinChannel", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket0Request3LeaveChannel.class, new MethodObjectPair("handleLeaveChannel", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket0Request4ChannelList.class, new MethodObjectPair("handleChannelList", PacketHandlerServer.class, this));
		
		registerPacketTrigger(ServerReadPacket1BinaryServerMessage.class, new MethodObjectPair("handleBinaryServerMessage", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket2BinaryChannelMessage.class, new MethodObjectPair("handleBinaryChannelMessage", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket3BinaryPeerMessage.class, new MethodObjectPair("handleBinaryPeerMessage", PacketHandlerServer.class, this));
		
		registerPacketTrigger(ServerReadPacket4ObjectServerMessage.class, new MethodObjectPair("handleObjectServerMessage", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket5ObjectChannelMessage.class, new MethodObjectPair("handleObjectChannelMessage", PacketHandlerServer.class, this));
		registerPacketTrigger(ServerReadPacket6ObjectPeerMessage.class, new MethodObjectPair("handleObjectPeerMessage", PacketHandlerServer.class, this));
		
		registerPacketTrigger(ServerReadPacket8ChannelMaster.class, new MethodObjectPair("handleChannelMaster", PacketHandlerServer.class, this));
		
		registerPacketTrigger(ServerReadPacket9Pong.class, new MethodObjectPair("handlePong", PacketHandlerServer.class, this));
		
		while(!interrupted())
		{
			try {
				Object[] status = client.attemptPing();
				
				if(status.length == 1)
				{
					if(!(Boolean)status[0])
						break;
				}else if(status.length == 2)
				{
					ServerReadPacket packet = (ServerReadPacket) status[1];
					
					if(packetTriggers.containsKey(packet.getClass()))
						packetTriggers.get(packet.getClass()).invoke(packet);
					else
						throw new LacewingException("Packet " + packet.getPacketType() + " : " + packet.getClass().getSimpleName() + " has no handler");
				
					continue;
				}
				
				ServerReadPacket packet = ServerReadPacket.readPacketFromStream(client.getInputStream());
				
				if(packet == null)
					continue;
				
				if(packetTriggers.containsKey(packet.getClass()))
					packetTriggers.get(packet.getClass()).invoke(packet);
				else
					throw new LacewingException("Packet " + packet.getPacketType() + " : " + packet.getClass().getSimpleName() + " has no handler");
				
			} catch (IOException | LacewingException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Exception ef = e;
				if(e instanceof InvocationTargetException)
					ef = (Exception) e.getCause();
				if(ef instanceof SocketException)
				{
					server.getClientHandler().clients.remove(client.getID());
					break;
				}
			}
		}
		
		try {
			client.disconnectClientWithoutBlock();
		} catch (IOException | LacewingException e) {
			e.printStackTrace();
		}
	}
	
	public void handleConnectPacket(ServerReadPacket0Request0Connect packet) throws IOException, LacewingException
	{
		if(!packet.getVersion().equals("revision 3") || !server.getPacketActions().onConnectRequest(client))
			client.disconnectClient();
		else
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response0Connect(server.getMOTD(), client.getID(), true));
	}
	
	public void handleNameSet(ServerReadPacket0Request1SetName packet) throws IOException, LacewingException
	{
		String name = server.getPacketActions().onSetNameRequest(client, packet.getName());
		if(name != null)
		{
			client.setName(name);
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response1SetName(client.getName(), true));
			if(client.connectedChannels.size() > 0)
				for(ServerChannel ch : client.connectedChannels.toArray(new ServerChannel[0]))
					for(ServerClient c : ch.getClients())
						if(c != client)
							ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket9Peer(client, ch.getID()));
		}else
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response1SetName(name, false));
	}
	
	public void handleJoinChannel(ServerReadPacket0Request2JoinChannel packet) throws IOException, LacewingException
	{
		if(client.getName() == null)
		{
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response2JoinChannel(packet.getName()));
			return;
		}
		
		String name = server.getPacketActions().onJoinChannelRequest(client, packet.getIsHiding(), packet.getIsClosing(), packet.getName());
		if(name != null)
		{
			ServerChannel ch = ServerChannel.getChannel(server, name);
			if(ch.getClients().length == 0)
			{
				ch.setHidden(packet.getIsHiding());
				ch.setWillClose(packet.getIsClosing());
				ch.setChannelMaster(client);
			}
			ch.addClient(client);
			client.connectedChannels.add(ch);
			
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response2JoinChannel(ch, ch.getChannelMaster() == this.client, this.client));
			if(client.connectedChannels.size() > 0)
				for(ServerChannel chs : client.connectedChannels.toArray(new ServerChannel[0]))
					for(ServerClient c : chs.getClients())
						if(c != client)
							ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket9Peer(client, chs.getID()));
		}else
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response2JoinChannel(name));
	}
	
	public void handleLeaveChannel(ServerReadPacket0Request3LeaveChannel packet) throws IOException, LacewingException
	{
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannelID());
		if(ch == null)
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response3LeaveChannel(packet.getChannelID(), false));
		else
		{
			if(!Arrays.asList(ch.getClients()).contains(client) || !server.getPacketActions().onLeaveChannelRequest(client, ch))
			{
				ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response3LeaveChannel(ch.getID(), false));
				return;
			}
			
			client.disconnectFromChannel(ch);
			if(client.connectedChannels.size() > 0)
				for(ServerChannel chs : client.connectedChannels.toArray(new ServerChannel[0]))
					for(ServerClient c : chs.getClients())
						if(c != client)
							ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket9Peer(client, chs.getID()));
		}
		
		System.gc();
	}
	
	public void handleChannelList(ServerReadPacket0Request4ChannelList packet) throws IOException, LacewingException
	{
		if(server.getPacketActions().onChannelListRequest(client))
			ServerWritePacket.writePacketToStream(client.getOutputStream(), new ServerWritePacket0Response4ChannelList(server, client, server.isChannelListingEnabled()));
	}
	
	public void handleBinaryServerMessage(ServerReadPacket1BinaryServerMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException
	{
		server.getPacketActions().onBinaryServerMessage(client, packet.getSubChannel(), packet.getMessageData());
	}
	
	public void handleBinaryChannelMessage(ServerReadPacket2BinaryChannelMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException, IOException
	{
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannel());
		for(ServerClient c : ch.getClients())
			if(c != client)
				ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket2BinaryChannelMessage(packet.getSubChannel(), packet.getChannel(), client.getID(), packet.getMessageData()));
		server.getPacketActions().onBinaryChannelMessage(client, ch, packet.getSubChannel(), packet.getMessageData());
	}
	
	public void handleBinaryPeerMessage(ServerReadPacket3BinaryPeerMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException, IOException
	{
		ServerClient c = server.getClientHandler().clients.get(packet.getPeer());
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannel());
		if(Arrays.asList(ch.getClients()).contains(c))
		{
			c.sendBinaryPeerMessage(packet.getSubChannel(), packet.getChannel(), client.getID(), packet.getMessageData());
			server.getPacketActions().onBinaryPeerMessage(client, c, ch, packet.getSubChannel(), packet.getMessageData());
		}
	}
	
	public void handleObjectServerMessage(ServerReadPacket4ObjectServerMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException
	{
		server.getPacketActions().onObjectServerMessage(client, packet.getSubChannel(), new String(packet.getMessageData()));
	}
	
	public void handleObjectChannelMessage(ServerReadPacket5ObjectChannelMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException, IOException
	{
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannel());
		for(ServerClient c : ch.getClients())
			if(c != client)
				ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket6ObjectChannelMessage(packet.getSubChannel(), packet.getChannel(), client.getID(), packet.getMessageData()));
		server.getPacketActions().onObjectChannelMessage(client, ch, packet.getSubChannel(), new String(packet.getMessageData()));
	}
	
	public void handleObjectPeerMessage(ServerReadPacket6ObjectPeerMessage packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, LacewingException, IOException
	{
		ServerClient c = server.getClientHandler().clients.get(packet.getPeer());
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannel());
		if(Arrays.asList(ch.getClients()).contains(c))
		{
			c.sendObjectPeerMessage(packet.getSubChannel(), packet.getChannel(), client.getID(), new String(packet.getMessageData()));
			server.getPacketActions().onObjectPeerMessage(client, c, ch, packet.getSubChannel(), new String(packet.getMessageData()));
		}
	}
	
	public void handleChannelMaster(ServerReadPacket8ChannelMaster packet) throws IOException, LacewingException
	{
		ServerClient c = server.getClientHandler().clients.get(packet.getPeer());
		ServerChannel ch = ServerChannel.getChannel(server, packet.getChannel());
		if(ch != null)
			if(ch.getChannelMaster() == client)
				if(server.getPacketActions().onKickPeerRequest(client, ch, c))
				{
					c.disconnectFromChannel(ch);
					if(client.connectedChannels.size() > 0)
						for(ServerChannel chs : client.connectedChannels.toArray(new ServerChannel[0]))
							for(ServerClient cd : chs.getClients())
								if(cd != client)
									ServerWritePacket.writePacketToStream(cd.getOutputStream(), new ServerWritePacket9Peer(c.getID(), chs.getID()));
				}
	}
	
	public void handlePong(ServerReadPacket9Pong packet) {}
	
	public void cancel()
	{
		super.interrupt();
		
		while(this.isAlive()) this.isAlive();
	}
	
	public void init(ServerClient client, LacewingServer ls)
	{
		this.client = client;
		this.server = ls;
		this.setName("Packet Handler for " + client.getID());
		start();
	}
	
	public ServerClient getClient()
	{
		return client;
	}
	
	public LacewingServer getServer()
	{
		return server;
	}
	
	public void registerPacketTrigger(Class<? extends ServerReadPacket> packetType, MethodObjectPair toInvoke)
	{
		packetTriggers.put(packetType, toInvoke);
	}
	
	public static PacketHandlerServer getThreadAsThis()
	{
		return (PacketHandlerServer)Thread.currentThread();
	}
}
