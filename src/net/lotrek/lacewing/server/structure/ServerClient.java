package net.lotrek.lacewing.server.structure;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.server.LacewingServer;
import net.lotrek.lacewing.server.packet.PacketHandlerServer;
import net.lotrek.lacewing.server.packet.ServerReadPacket;
import net.lotrek.lacewing.server.packet.ServerReadPacket9Pong;
import net.lotrek.lacewing.server.packet.ServerWritePacket;
import net.lotrek.lacewing.server.packet.ServerWritePacket0Response3LeaveChannel;
import net.lotrek.lacewing.server.packet.ServerWritePacket11Ping;
import net.lotrek.lacewing.server.packet.ServerWritePacket1BinaryServerMessage;
import net.lotrek.lacewing.server.packet.ServerWritePacket3BinaryPeerMessage;
import net.lotrek.lacewing.server.packet.ServerWritePacket5ObjectServerMessage;
import net.lotrek.lacewing.server.packet.ServerWritePacket7ObjectPeerMessage;

public class ServerClient
{
	private Socket clientSocket;
	private DataOutputStream dos;
	private DataInputStream dis;
	private int id;
	private String name;
	private PacketHandlerServer packetHandler = new PacketHandlerServer();
	public final ArrayList<ServerChannel> connectedChannels = new ArrayList<ServerChannel>();
	
	public ServerClient(Socket clientSocket, LacewingServer server) throws IOException
	{
		this.clientSocket = clientSocket;
		dos = new DataOutputStream(this.clientSocket.getOutputStream());
		dis = new DataInputStream(this.clientSocket.getInputStream());
		
		packetHandler.init(this, server);
	}
	
	public void disconnectFromChannel(ServerChannel ch) throws IOException, LacewingException
	{
		if(!Arrays.asList(ch.getClients()).contains(this))
			return;
		
		connectedChannels.remove(ch);
		ch.removeClient(this);
		ServerWritePacket.writePacketToStream(this.getOutputStream(), new ServerWritePacket0Response3LeaveChannel(ch.getID(), true));
		if(ch.getChannelMaster() == this && ch.isWillClose())
		{
			for(ServerClient c : ch.getClients())
			{
				ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket0Response3LeaveChannel(ch.getID(), true));
				c.connectedChannels.remove(ch);
			}
			ch.clearClients();
		}
		
		if(ch.getClients().length == 0)
		{
			packetHandler.getServer().globalChannelList.remove(ch.getName());
			packetHandler.getServer().globalChannelsByID.remove(ch.getID());
		}
		
		System.gc();
	}
	
	public PacketHandlerServer getPacketHandler()
	{
		return packetHandler;
	}

	public void setID(int newID)
	{
		this.id = newID;
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public void setName(String newName)
	{
		this.name = newName;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public DataInputStream getInputStream()
	{
		return dis;
	}
	
	public DataOutputStream getOutputStream()
	{
		return dos;
	}
	
	public Socket getSocket()
	{
		return clientSocket;
	}
	
	public void disconnectClient() throws IOException, LacewingException
	{
		if(!this.clientSocket.isClosed())
			for(ServerChannel ch : connectedChannels.toArray(new ServerChannel[0]))
				this.disconnectFromChannel(ch);
		if(!this.clientSocket.isClosed())
			this.clientSocket.close();
		packetHandler.getServer().getClientHandler().clients.remove(this.getID());
		packetHandler.cancel();
	}
	
	public void disconnectClientWithoutBlock() throws IOException, LacewingException
	{
		if(!this.clientSocket.isClosed())
			for(ServerChannel ch : connectedChannels.toArray(new ServerChannel[0]))
				this.disconnectFromChannel(ch);
		if(!this.clientSocket.isClosed())
			this.clientSocket.close();
		packetHandler.getServer().getClientHandler().clients.remove(this.getID());
		packetHandler.interrupt();
	}
	
	public void writeToJoinChannel(OutputStream os, ServerChannel channel) throws IOException
	{
		DataTools.writeInversedShort(os, getID());
		os.write(channel.getChannelMaster() == this ? 1 : 0);
		os.write(this.getName().length());
		os.write(this.getName().getBytes());
	}
	
	public int getLengthInJoinChannel()
	{
		return 4 + this.getName().length();
	}
	
	private long lastTime = -1;
	public Object[] attemptPing() throws LacewingException, IOException
	{
		if(lastTime == -1)
			lastTime = System.currentTimeMillis();
		
		if(System.currentTimeMillis() - lastTime >= 1000)
		{
			try {
				ServerWritePacket.writePacketToStream(getOutputStream(), new ServerWritePacket11Ping());
			} catch (IOException e) {
				this.clientSocket.close();
				packetHandler.getServer().getClientHandler().clients.remove(this.getID());
				return new Object[]{false};
			}
			lastTime = System.currentTimeMillis();
			
			while(System.currentTimeMillis() - lastTime < 500)
			{
				ServerReadPacket packet = ServerReadPacket.readPacketFromStream(getInputStream());
				
				if(packet == null)
					continue;
				
				if(packet instanceof ServerReadPacket9Pong)
					return new Object[]{true};
				else
					return new Object[]{true, packet};
			}
			
			lastTime = System.currentTimeMillis();
			return new Object[]{false};
		}else
			return new Object[]{true};
	}
	
	public String toString()
	{
		return "ServerClient { name: \"" + this.getName() + "\" id: " + getID() + " channels: " + Arrays.toString(connectedChannels.toArray()) + " }";
	}
	
	public void sendBinaryServerMessage(int subchannel, byte[] data) throws IOException, LacewingException
	{
		ServerWritePacket.writePacketToStream(getOutputStream(), new ServerWritePacket1BinaryServerMessage(subchannel, data));
	}
	
	public void sendBinaryPeerMessage(int subchannel, int channel, int peerFrom, byte[] data) throws IOException, LacewingException
	{
		ServerWritePacket.writePacketToStream(getOutputStream(), new ServerWritePacket3BinaryPeerMessage(subchannel, channel, peerFrom, data));
	}
	
	public void sendObjectServerMessage(int subchannel, String json) throws IOException, LacewingException
	{
		ServerWritePacket.writePacketToStream(getOutputStream(), new ServerWritePacket5ObjectServerMessage(subchannel, json.getBytes()));
	}
	
	public void sendObjectPeerMessage(int subchannel, int channel, int peerFrom, String json) throws IOException, LacewingException
	{
		ServerWritePacket.writePacketToStream(getOutputStream(), new ServerWritePacket7ObjectPeerMessage(subchannel, channel, peerFrom, json.getBytes()));
	}
	
	public String getAddress()
	{
		return clientSocket.getInetAddress().getHostAddress();
	}
}
