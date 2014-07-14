package net.lotrek.lacewing.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.client.packet.PacketHandlerClient;
import net.lotrek.lacewing.client.packet.ClientReadPacket;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response0Connect;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response1SetName;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response2JoinChannel;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response3LeaveChannel;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response4ChannelList;
import net.lotrek.lacewing.client.packet.ClientReadPacket11Ping;
import net.lotrek.lacewing.client.packet.ClientReadPacket1BinaryServerMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket2BinaryChannelMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket3BinaryPeerMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket4BinaryServerChannelMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket5ObjectServerMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket6ObjectChannelMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket7ObjectPeerMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket8ObjectServerChannelMessage;
import net.lotrek.lacewing.client.packet.ClientReadPacket9Peer;
import net.lotrek.lacewing.client.packet.ClientWritePacket;
import net.lotrek.lacewing.client.packet.ClientWritePacket0Request0Connect;
import net.lotrek.lacewing.client.packet.ClientWritePacket0Request1SetName;
import net.lotrek.lacewing.client.packet.ClientWritePacket0Request4ChannelList;
import net.lotrek.lacewing.client.packet.ClientWritePacket1BinaryServerMessage;
import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;

public class LacewingClient
{
	static
	{
		regsiterPackets();
	}
	
	/**
	 * The map of this instance's known channels
	 */
	public final HashMap<String, ClientChannel> globalChannelMap = new HashMap<String, ClientChannel>();
	/**
	 * The map of this instance's known peers
	 */
	public final HashMap<Integer, ClientPeer> knownPeers = new HashMap<Integer, ClientPeer>();
	
	private Socket sock;
	private String address;
	private int port;
	private DataInputStream dis;
	private DataOutputStream dos;
	private boolean isConnected, isActive;
	private PacketHandlerClient packetHandler = new PacketHandlerClient();
	private ClientPacketActions packetActions;
	private volatile int clientID = -1;
	private volatile String name, welcomeMessage;
	private volatile ClientChannel[] temporaryChannelList;
	
	/**
	 * Constructs a new LacewingClient connected to the specified address
	 * 
	 * @param addr the address or host name to connect to
	 * @param port the port to connect on
	 * @throws LacewingException thrown on any IO errors
	 */
	public LacewingClient(String addr, int port) throws LacewingException
	{
		try {
			sock = new Socket(addr, port);
			this.address = addr;
			this.port = port;
			dis = new DataInputStream(sock.getInputStream());
			dos = new DataOutputStream(sock.getOutputStream());
			
			isConnected = true;
			
			getPacketHandler().registerPacketTrigger(ClientReadPacket0Response0Connect.class, new MethodObjectPair("handleConnect", LacewingClient.class, this));
			getPacketHandler().init(this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to open socket");
			le.initCause(e);
			throw le;
		}
	}
	
	/**
	 * Constructs a new LacewingClient object connected to the specified address on port 6121
	 * 
	 * @param addr the address or host name to connect to
	 * @throws LacewingException thrown on any IO errors
	 */
	public LacewingClient(String addr) throws LacewingException
	{
		this(addr, 6121);
	}
	
	/**
	 * Get the socket's input stream wrapped in a DataInputStream
	 * 
	 * @return the wrapped socket stream
	 * @throws LacewingException thrown if a connection is not open
	 */
	public DataInputStream getInputStream() throws LacewingException
	{
		if(!isConnected)
			throw new LacewingException("Client is not connected");
		
		return dis;
	}
	
	/**
	 * Get the socket's output stream wrapped in a DataOuputStream
	 * 
	 * @return the wrapped socket stream
	 * @throws LacewingException thrown if a connection is not open
	 */
	public DataOutputStream getOutputStream() throws LacewingException
	{
		if(!isConnected)
			throw new LacewingException("Client is not connected");
		
		return dos;
	}
	
	/**
	 * Attempts to connect over the Lacewing protocol
	 * 
	 * @return the welcome message returned by the server
	 * @throws LacewingException thrown if pairing fails
	 */
	public String pair() throws LacewingException
	{
		if(isActive)
			return "";
		
		try {
			ClientWritePacket.writePacketToStream(getOutputStream(), new ClientWritePacket0Request0Connect());
			while(getClientID() == -1) getClientID();
			this.isActive = true;
			return this.welcomeMessage;
		} catch (IOException e)
		{
			LacewingException le = new LacewingException("Unable to pair with server");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleConnect(ClientReadPacket0Response0Connect packet)
	{
		this.welcomeMessage = packet.getWelcomeMessage();
		this.clientID = packet.getClientID();
		this.getPacketActions().onConnect(packet.getPacketSuccess(), welcomeMessage);
	}
	
	/**
	 * Attempts to set the client's name
	 * 
	 * @param name the name to set
	 * @throws LacewingException thrown if name setting fails
	 */
	public void setName(String name) throws LacewingException
	{
		if(!isActive)
			throw new LacewingException("Must be paired to set the name");
		
		try {
			this.name = null;
			getPacketHandler().registerPacketTrigger(ClientReadPacket0Response1SetName.class, new MethodObjectPair("handleNameSet", LacewingClient.class, this));
			ClientWritePacket.writePacketToStream(getOutputStream(), new ClientWritePacket0Request1SetName(name));
			while(getName() == null) getName();
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to set name");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleNameSet(ClientReadPacket0Response1SetName packet)
	{
		this.name = packet.getName();
	}
	
	/**
	 * Attempts to obtain the channel list
	 * 
	 * @return channel list
	 * @throws LacewingException thrown if channel listing fails
	 */
	public ClientChannel[] getChannelList() throws LacewingException
	{
		try {
			getPacketHandler().registerPacketTrigger(ClientReadPacket0Response4ChannelList.class, new MethodObjectPair("handleChannelList", LacewingClient.class, this));
			ClientWritePacket.writePacketToStream(getOutputStream(), new ClientWritePacket0Request4ChannelList());
			while(temporaryChannelList == null) this.toString();
			ClientChannel[] tmp = temporaryChannelList;
			temporaryChannelList = null;
			return tmp;
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to get channel list");
			le.initCause(e);
			throw le;
		}
	}
	
	public void handleChannelList(ClientReadPacket0Response4ChannelList packet)
	{
		temporaryChannelList = packet.getChannels();
	}
	
	/**
	 * Sends the sever a binary message
	 * 
	 * @param subChannel the subchannel to send on
	 * @param data the data to send
	 * @throws LacewingException thrown if sending fails
	 */
	public void sendBinaryServerMessage(int subChannel, byte[] data) throws LacewingException
	{
		try {
			ClientWritePacket.writePacketToStream(getOutputStream(), new ClientWritePacket1BinaryServerMessage(subChannel, data));
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to send binary server message");
			le.initCause(e);
			throw le;
		}
	}
	
	/**
	 * Gets the packet handler
	 * 
	 * @return this instance's packet handler
	 */
	public PacketHandlerClient getPacketHandler()
	{
		return packetHandler;
	}

	/**
	 * Gets the client ID
	 * 
	 * @return this instance's client ID
	 */
	public int getClientID()
	{
		return this.clientID;
	}
	
	/**
	 * Gets the client name
	 * 
	 * @return this instance's name
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Attempts to disconnect from the server
	 * 
	 * @throws LacewingException thrown if disconnect fails or is impossible
	 */
	public void disconnect() throws LacewingException
	{
		if(!isActive)
			throw new LacewingException("Client has not paired with server");
		
		getPacketHandler().interrupt();
		
		try {
			sock.getOutputStream().flush();
			sock.close();
			isConnected = false;
			isActive = false;
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to close socket");
			le.initCause(e);
			throw le;
		}
	}
	
	/**
	 * Attempts to reconnect this client following a disconnect
	 * 
	 * @throws LacewingException thrown if connection fails or is impossible
	 */
	public void reconnect() throws LacewingException
	{
		if(isConnected)
			throw new LacewingException("Client is already connected");
		
		try {
			sock = new Socket(this.address, port);
			dis = new DataInputStream(sock.getInputStream());
			dos = new DataOutputStream(sock.getOutputStream());
			
			isConnected = true;
			
			globalChannelMap.clear();
			knownPeers.clear();
			
			packetHandler = new PacketHandlerClient();
			getPacketHandler().registerPacketTrigger(ClientReadPacket0Response0Connect.class, new MethodObjectPair("handleConnect", LacewingClient.class, this));
			getPacketHandler().init(this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to open socket");
			le.initCause(e);
			throw le;
		}
	}
	
	public ClientPacketActions getPacketActions() {
		return packetActions;
	}

	public void setPacketActions(ClientPacketActions packetActions) {
		this.packetActions = packetActions;
	}

	/**
	 * Handles an out-of-thread exception. This method is pointless.
	 * 
	 * @param e the exception to handle
	 */
	public void handleOtherworldException(Exception e)
	{
		e.printStackTrace();
	}
	
	private static void regsiterPackets()
	{
		ClientReadPacket.registerPacket(ClientReadPacket0Response0Connect.class, 0);
		{
			ClientReadPacket0Response.registerSubpacket(ClientReadPacket0Response0Connect.class, 0);
			ClientReadPacket0Response.registerSubpacket(ClientReadPacket0Response1SetName.class, 1);
			ClientReadPacket0Response.registerSubpacket(ClientReadPacket0Response2JoinChannel.class, 2);
			ClientReadPacket0Response.registerSubpacket(ClientReadPacket0Response3LeaveChannel.class, 3);
			ClientReadPacket0Response.registerSubpacket(ClientReadPacket0Response4ChannelList.class, 4);
		}
		
		ClientReadPacket.registerPacket(ClientReadPacket1BinaryServerMessage.class, 1);
		ClientReadPacket.registerPacket(ClientReadPacket2BinaryChannelMessage.class, 2);
		ClientReadPacket.registerPacket(ClientReadPacket3BinaryPeerMessage.class, 3);
		ClientReadPacket.registerPacket(ClientReadPacket4BinaryServerChannelMessage.class, 4);
		ClientReadPacket.registerPacket(ClientReadPacket5ObjectServerMessage.class, 5);
		ClientReadPacket.registerPacket(ClientReadPacket6ObjectChannelMessage.class, 6);
		ClientReadPacket.registerPacket(ClientReadPacket7ObjectPeerMessage.class, 7);
		ClientReadPacket.registerPacket(ClientReadPacket8ObjectServerChannelMessage.class, 8);
		ClientReadPacket.registerPacket(ClientReadPacket9Peer.class, 9);
		ClientReadPacket.registerPacket(null, 10); //UDP packet
		ClientReadPacket.registerPacket(ClientReadPacket11Ping.class, 11);
	}
}
