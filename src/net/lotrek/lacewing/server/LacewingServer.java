package net.lotrek.lacewing.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.server.packet.ServerReadPacket;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request0Connect;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request1SetName;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request2JoinChannel;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request3LeaveChannel;
import net.lotrek.lacewing.server.packet.ServerReadPacket0Request4ChannelList;
import net.lotrek.lacewing.server.packet.ServerReadPacket1BinaryServerMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket2BinaryChannelMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket3BinaryPeerMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket4ObjectServerMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket5ObjectChannelMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket6ObjectPeerMessage;
import net.lotrek.lacewing.server.packet.ServerReadPacket8ChannelMaster;
import net.lotrek.lacewing.server.packet.ServerReadPacket9Pong;
import net.lotrek.lacewing.server.structure.ServerChannel;

public class LacewingServer
{
	static
	{
		registerPackets();
	}
	
	public final HashMap<String, ServerChannel> globalChannelList = new HashMap<String, ServerChannel>();
	public final NavigableMap<Integer, ServerChannel> globalChannelsByID = new TreeMap<Integer, ServerChannel>();
	private final ClientHandlerServer clientHandler = new ClientHandlerServer();
	private ServerPacketActions packetActions;
	private String motd = "Welcome! Server is running jLacewing 1.0.0 (" + System.getProperty("os.name") + " " + System.getProperty("java.vm.name") + ")";
	private boolean enableChannelListing;
	
	public LacewingServer(String motd, boolean enableChannelListing, int port) throws LacewingException
	{
		this.motd = motd;
		this.enableChannelListing = enableChannelListing;
		
		try {
			this.clientHandler.init(port, this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("An error occurred starting the server");
			le.initCause(e);
			throw le;
		}
	}
	
	public LacewingServer(boolean enableChannelListing, int port) throws LacewingException
	{
		this.enableChannelListing = enableChannelListing;
		
		try {
			this.clientHandler.init(port, this);
		} catch (IOException e) {
			LacewingException le = new LacewingException("An error occurred starting the server");
			le.initCause(e);
			throw le;
		}
	}
	
	public LacewingServer(int port) throws LacewingException
	{
		this(true, port);
	}
	
	public LacewingServer() throws LacewingException
	{
		this(6121);
	}
	
	public String getMOTD()
	{
		return motd;
	}
	
	public void setMOTD(String newMOTD)
	{
		this.motd = newMOTD;
	}
	
	public ClientHandlerServer getClientHandler()
	{
		return clientHandler;
	}
	
	public boolean isChannelListingEnabled()
	{
		return enableChannelListing;
	}
	
	public void setEnableChannelListing(boolean newListing)
	{
		this.enableChannelListing = newListing;
	}
	
	public ServerPacketActions getPacketActions()
	{
		return packetActions;
	}

	public void setPacketActions(ServerPacketActions packetActions)
	{
		this.packetActions = packetActions;
	}

	public void stopServer()
	{
		clientHandler.interrupt();
	}
	
	private static void registerPackets()
	{
		ServerReadPacket.registerPacket(ServerReadPacket0Request0Connect.class, 0);
		{
			ServerReadPacket0Request.registerSubpacket(ServerReadPacket0Request0Connect.class, 0);
			ServerReadPacket0Request.registerSubpacket(ServerReadPacket0Request1SetName.class, 1);
			ServerReadPacket0Request.registerSubpacket(ServerReadPacket0Request2JoinChannel.class, 2);
			ServerReadPacket0Request.registerSubpacket(ServerReadPacket0Request3LeaveChannel.class, 3);
			ServerReadPacket0Request.registerSubpacket(ServerReadPacket0Request4ChannelList.class, 4);
		}
		
		ServerReadPacket.registerPacket(ServerReadPacket1BinaryServerMessage.class, 1);
		ServerReadPacket.registerPacket(ServerReadPacket2BinaryChannelMessage.class, 2);
		ServerReadPacket.registerPacket(ServerReadPacket3BinaryPeerMessage.class, 3);
		ServerReadPacket.registerPacket(ServerReadPacket4ObjectServerMessage.class, 4);
		ServerReadPacket.registerPacket(ServerReadPacket5ObjectChannelMessage.class, 5);
		ServerReadPacket.registerPacket(ServerReadPacket6ObjectPeerMessage.class, 6);
		ServerReadPacket.registerPacket(null, 7);
		ServerReadPacket.registerPacket(ServerReadPacket8ChannelMaster.class, 8);
		ServerReadPacket.registerPacket(ServerReadPacket9Pong.class, 9);
	}
}
