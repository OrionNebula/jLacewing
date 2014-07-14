package net.lotrek.lacewing.client.packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;

public class PacketHandlerClient extends Thread
{
	private LacewingClient client;
	private HashMap<Class<? extends ClientReadPacket>, MethodObjectPair> packetTriggers = new HashMap<Class<? extends ClientReadPacket>, MethodObjectPair>();
	
	public void run()
	{
		registerPacketTrigger(ClientReadPacket1BinaryServerMessage.class, new MethodObjectPair("handleBinaryServerMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket2BinaryChannelMessage.class, new MethodObjectPair("handleBinaryChannelMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket3BinaryPeerMessage.class, new MethodObjectPair("handleBinaryPeerMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket4BinaryServerChannelMessage.class, new MethodObjectPair("handleBinaryServerChannelMessage", PacketHandlerClient.class, this));
		
		registerPacketTrigger(ClientReadPacket5ObjectServerMessage.class, new MethodObjectPair("handleObjectServerMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket6ObjectChannelMessage.class, new MethodObjectPair("handleObjectChannelMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket7ObjectPeerMessage.class, new MethodObjectPair("handleObjectPeerMessage", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket8ObjectServerChannelMessage.class, new MethodObjectPair("handleObjectServerChannelMessage", PacketHandlerClient.class, this));
		
		registerPacketTrigger(ClientReadPacket0Response3LeaveChannel.class, new MethodObjectPair("handleLeaveChannel", ClientChannel.class, null));
		registerPacketTrigger(ClientReadPacket9Peer.class, new MethodObjectPair("handlePeer", PacketHandlerClient.class, this));
		registerPacketTrigger(ClientReadPacket11Ping.class, new MethodObjectPair("handlePing", PacketHandlerClient.class, this));
		
		while(!interrupted())
		{
			try {
				ClientReadPacket packet = ClientReadPacket.readPacketFromStream(client.getInputStream());
				
				if(packet == null)
					continue;
				
				if(packetTriggers.containsKey(packet.getClass()))
					packetTriggers.get(packet.getClass()).invoke(packet);
				else
					throw new LacewingException("Packet " + packet.getPacketType() + " : " + packet.getClass().getSimpleName() + " has no handler");
				
			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | LacewingException e) {
				client.handleOtherworldException(e);
			}
		}
	}
	
	public void interrupt()
	{
		super.interrupt();
		
		while(this.isAlive()) System.out.print("");
	}
	
	public void init(LacewingClient lc)
	{
		this.setName("PacketHandlerClient " + lc);
		client = lc;
		start();
	}
	
	public LacewingClient getClient()
	{
		return client;
	}
	
	public void registerPacketTrigger(Class<? extends ClientReadPacket> packetType, MethodObjectPair toInvoke)
	{
		packetTriggers.put(packetType, toInvoke);
	}
	
	public void handleBinaryServerMessage(ClientReadPacket1BinaryServerMessage packet)
	{
		client.getPacketActions().onBinaryServerMessage(packet.getSubChannel(), packet.getMessageData());
	}
	
	public void handleBinaryChannelMessage(ClientReadPacket2BinaryChannelMessage packet)
	{
		client.getPacketActions().onBinaryChannelMessage(packet.getSubChannel(), packet.getChannel(), packet.getPeer(), packet.getMessageData());
	}
	
	public void handleBinaryPeerMessage(ClientReadPacket3BinaryPeerMessage packet)
	{
		client.getPacketActions().onBinaryPeerMessage(packet.getSubChannel(), packet.getChannel(), packet.getPeer(), packet.getMessageData());
	}
	
	public void handleBinaryServerChannelMessage(ClientReadPacket4BinaryServerChannelMessage packet)
	{
		client.getPacketActions().onBinaryServerChannelMessage(packet.getSubChannel(), packet.getChannel(), packet.getMessageData());
	}
	
	public void handleObjectServerMessage(ClientReadPacket5ObjectServerMessage packet)
	{
		client.getPacketActions().onObjectServerMessage(packet.getSubChannel(), packet.getMessageData());
	}
	
	public void handleObjectChannelMessage(ClientReadPacket6ObjectChannelMessage packet)
	{
		client.getPacketActions().onObjectChannelMessage(packet.getSubChannel(), packet.getChannel(), packet.getPeer(), packet.getMessageData());
	}
	
	public void handleObjectPeerMessage(ClientReadPacket7ObjectPeerMessage packet)
	{
		client.getPacketActions().onObjectPeerMessage(packet.getSubChannel(), packet.getChannel(), packet.getPeer(), packet.getMessageData());
	}
	
	public void handleObjectServerChannelMessage(ClientReadPacket8ObjectServerChannelMessage packet)
	{
		client.getPacketActions().onObjectServerChannelMessage(packet.getSubChannel(), packet.getChannel(), packet.getMessageData());
	}
	
	public void handlePeer(ClientReadPacket9Peer packet)
	{
		switch(packet.getActionType(client))
		{
		case ClientReadPacket9Peer.JOIN:
			packet.updatePeer(getClient());
			client.getPacketActions().onPeerJoin(ClientChannel.getChannel(client, packet.getChannelID()), ClientPeer.getPeer(client, packet.getPeerID()));
			return;
		case ClientReadPacket9Peer.CHANGE:
			client.getPacketActions().onPeerChanged(ClientChannel.getChannel(client, packet.getChannelID()), ClientPeer.getPeer(client, packet.getPeerID()), packet.getName(), packet.isChannelMaster());
			break;
		case ClientReadPacket9Peer.LEFT:
			client.getPacketActions().onPeerLeft(ClientChannel.getChannel(client, packet.getChannelID()), ClientPeer.getPeer(client, packet.getPeerID()));
			break;
		}
		packet.updatePeer(getClient());
	}
	
	public void handlePing(ClientReadPacket11Ping packet)
	{
		try {
			ClientWritePacket.writePacketToStream(client.getOutputStream(), new ClientWritePacket9Pong());
		} catch (IOException | LacewingException e) {
			e.printStackTrace();
		}
	}
	
	
	public static PacketHandlerClient getThreadAsThis()
	{
		return (PacketHandlerClient)Thread.currentThread();
	}
}
