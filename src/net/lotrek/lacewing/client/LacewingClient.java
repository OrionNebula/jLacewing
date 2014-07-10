package net.lotrek.lacewing.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.client.packet.PacketHandlerClient;
import net.lotrek.lacewing.client.packet.ReadPacket;
import net.lotrek.lacewing.client.packet.ReadPacket0Response;
import net.lotrek.lacewing.client.packet.ReadPacket0Response0Connect;
import net.lotrek.lacewing.client.packet.ReadPacket0Response1SetName;
import net.lotrek.lacewing.client.packet.ReadPacket0Response2JoinChannel;
import net.lotrek.lacewing.client.packet.ReadPacket0Response3LeaveChannel;
import net.lotrek.lacewing.client.packet.ReadPacket0Response4ChannelList;
import net.lotrek.lacewing.client.packet.ReadPacket11Ping;
import net.lotrek.lacewing.client.packet.ReadPacket1BinaryServerMessage;
import net.lotrek.lacewing.client.packet.ReadPacket2BinaryChannelMessage;
import net.lotrek.lacewing.client.packet.ReadPacket3BinaryPeerMessage;
import net.lotrek.lacewing.client.packet.ReadPacket9Peer;
import net.lotrek.lacewing.client.packet.WritePacket;
import net.lotrek.lacewing.client.packet.WritePacket0Request0Connect;
import net.lotrek.lacewing.client.packet.WritePacket0Request1SetName;
import net.lotrek.lacewing.client.packet.WritePacket0Request4ChannelList;
import net.lotrek.lacewing.client.packet.WritePacket1BinaryServerMessage;
import net.lotrek.lacewing.client.structure.Channel;
import net.lotrek.lacewing.client.structure.Peer;

public class LacewingClient
{
	static
	{
		regsiterPackets();
	}
	
	public final HashMap<String, Channel> globalChannelMap = new HashMap<String, Channel>();
	public final HashMap<Integer, Peer> knownPeers = new HashMap<Integer, Peer>();
	
	private Socket sock;
	private String address;
	private int port;
	private DataInputStream dis;
	private DataOutputStream dos;
	private boolean isConnected, isActive;
	private PacketHandlerClient packetHandler = new PacketHandlerClient();
	private volatile int clientID = -1;
	private volatile String name, welcomeMessage;
	private volatile Channel[] temporaryChannelList;
	
	public LacewingClient(String addr, int port) throws LacewingException
	{
		try {
			sock = new Socket(addr, port);
			this.address = addr;
			this.port = port;
			dis = new DataInputStream(sock.getInputStream());
			dos = new DataOutputStream(sock.getOutputStream());
			
			isConnected = true;
			
			getPacketHandler().init(this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to open socket");
			le.initCause(e);
			throw le;
		}
	}
	
	public DataInputStream getInputStream() throws LacewingException
	{
		if(!isConnected)
			throw new LacewingException("Client is not connected");
		
		return dis;
	}
	
	public DataOutputStream getOutputStream() throws LacewingException
	{
		if(!isConnected)
			throw new LacewingException("Client is not connected");
		
		return dos;
	}
	
	public String pair() throws LacewingException
	{
		if(isActive)
			return "";
		
		try {
			getPacketHandler().registerPacketTrigger(ReadPacket0Response0Connect.class, new MethodObjectPair(this.getClass().getMethod("handleConnect", ReadPacket0Response0Connect.class), this));
			WritePacket.writePacketToStream(getOutputStream(), new WritePacket0Request0Connect());
			while(getClientID() == -1) getClientID();
			this.isActive = true;
			return this.welcomeMessage;
		} catch (IOException | NoSuchMethodException | SecurityException e) {
			LacewingException le = new LacewingException("Unable to pair with server");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleConnect(ReadPacket0Response0Connect packet)
	{
		this.welcomeMessage = packet.getWelcomeMessage();
		this.clientID = packet.getClientID();
	}
	
	public void setName(String name) throws LacewingException
	{
		try {
			this.name = null;
			getPacketHandler().registerPacketTrigger(ReadPacket0Response1SetName.class, new MethodObjectPair(this.getClass().getMethod("handleNameSet", ReadPacket0Response1SetName.class), this));
			WritePacket.writePacketToStream(getOutputStream(), new WritePacket0Request1SetName(name));
			while(getName() == null) getName();
		} catch (IOException | NoSuchMethodException | SecurityException e) {
			LacewingException le = new LacewingException("Unable to set name");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleNameSet(ReadPacket0Response1SetName packet)
	{
		this.name = packet.getName();
	}
	
	public Channel[] getChannelList() throws LacewingException
	{
		try {
			getPacketHandler().registerPacketTrigger(ReadPacket0Response4ChannelList.class, new MethodObjectPair(this.getClass().getMethod("handleChannelList", ReadPacket0Response4ChannelList.class), this));
			WritePacket.writePacketToStream(getOutputStream(), new WritePacket0Request4ChannelList());
			while(temporaryChannelList == null) this.toString();
			Channel[] tmp = temporaryChannelList;
			temporaryChannelList = null;
			return tmp;
		} catch (IOException | NoSuchMethodException | SecurityException e) {
			LacewingException le = new LacewingException("Unable to set name");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleChannelList(ReadPacket0Response4ChannelList packet)
	{
		temporaryChannelList = packet.getChannels();
	}
	
	public void sendBinaryServerMessage(int subChannel, byte[] data) throws LacewingException
	{
		try {
			WritePacket.writePacketToStream(getOutputStream(), new WritePacket1BinaryServerMessage(subChannel, data));
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to set name");
			le.initCause(e);
			throw le;
		}
	}
	
	public PacketHandlerClient getPacketHandler()
	{
		return packetHandler;
	}

	public int getClientID()
	{
		return this.clientID;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void disconnect() throws LacewingException
	{
		if(!isActive)
			throw new LacewingException("Client has not paired with server");
		
		getPacketHandler().interrupt();
		
		try {
			sock.close();
			isConnected = false;
			isActive = false;
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to close socket");
			le.initCause(e);
			throw le;
		}
	}
	
	public void reconnect() throws LacewingException
	{
		try {
			sock = new Socket(this.address, port);
			dis = new DataInputStream(sock.getInputStream());
			dos = new DataOutputStream(sock.getOutputStream());
			
			isConnected = true;
			
			globalChannelMap.clear();
			knownPeers.clear();
			
			packetHandler = new PacketHandlerClient();
			getPacketHandler().init(this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to open socket");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleOtherworldException(Exception e)
	{
		e.printStackTrace();
	}
	
	private static void regsiterPackets()
	{
		ReadPacket.registerPacket(ReadPacket0Response0Connect.class, 0);
		{
			ReadPacket0Response.registerSubpacket(ReadPacket0Response0Connect.class, 0);
			ReadPacket0Response.registerSubpacket(ReadPacket0Response1SetName.class, 1);
			ReadPacket0Response.registerSubpacket(ReadPacket0Response2JoinChannel.class, 2);
			ReadPacket0Response.registerSubpacket(ReadPacket0Response3LeaveChannel.class, 3);
			ReadPacket0Response.registerSubpacket(ReadPacket0Response4ChannelList.class, 4);
		}
		
		ReadPacket.registerPacket(ReadPacket1BinaryServerMessage.class, 1);
		ReadPacket.registerPacket(ReadPacket2BinaryChannelMessage.class, 2);
		ReadPacket.registerPacket(ReadPacket3BinaryPeerMessage.class, 3);
		ReadPacket.registerPacket(null, 4);
		ReadPacket.registerPacket(null, 5);
		ReadPacket.registerPacket(null, 6);
		ReadPacket.registerPacket(null, 7);
		ReadPacket.registerPacket(null, 8);
		ReadPacket.registerPacket(ReadPacket9Peer.class, 9);
		ReadPacket.registerPacket(null, 10);
		ReadPacket.registerPacket(ReadPacket11Ping.class, 11);
	}
}
