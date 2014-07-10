package net.lotrek.lacewing.client.structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.packet.PacketHandlerClient;
import net.lotrek.lacewing.client.packet.ReadPacket0Response2JoinChannel;
import net.lotrek.lacewing.client.packet.ReadPacket0Response3LeaveChannel;
import net.lotrek.lacewing.client.packet.WritePacket;
import net.lotrek.lacewing.client.packet.WritePacket0Request2JoinChannel;
import net.lotrek.lacewing.client.packet.WritePacket0Request3LeaveChannel;
import net.lotrek.lacewing.client.packet.WritePacket2BinaryChannelMessage;

public class Channel
{
	private String name;
	private int channelID = -1;
	private boolean isConnected;
	private volatile boolean packetReceived;
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	
	private Channel(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getChannelID()
	{
		return channelID;
	}
	
	public Peer[] getPeers()
	{
		return peers.toArray(new Peer[0]);
	}
	
	public boolean isConnected()
	{
		return isConnected;
	}
	
	public void initPeerList(int peerCount)
	{
		if(peers.size() > peerCount)
			peers.subList(peerCount, peers.size() - 1).clear();
		else
			peers.addAll(Arrays.asList(new Peer[peerCount - peers.size()]));
	}
	
	public void resetPeerList(Peer[] peers)
	{
		this.peers.clear();
		this.peers.addAll(Arrays.asList(peers));
	}
	
	public void addPeer(Peer peerObj)
	{
		if(!peers.contains(peerObj))
			peers.add(peerObj);
	}
	
	public void removePeer(Peer peerObj)
	{
		peers.remove(peerObj);
	}
	
	public void joinChannel(LacewingClient c, boolean hide, boolean close) throws LacewingException
	{
		if(isConnected)
			throw new LacewingException("Cannot join a channel you have already joined");
		
		try {
			c.getPacketHandler().registerPacketTrigger(ReadPacket0Response2JoinChannel.class, new MethodObjectPair(this.getClass().getMethod("handleJoinChannel", ReadPacket0Response2JoinChannel.class), this));
			WritePacket.writePacketToStream(c.getOutputStream(), new WritePacket0Request2JoinChannel(name, hide, close));
			while(!packetReceived) Boolean.toString(packetReceived);
			packetReceived = false;
		} catch (NoSuchMethodException | SecurityException | IOException | LacewingException e) {
			e.printStackTrace();
		}
	}
	
	public void handleJoinChannel(ReadPacket0Response2JoinChannel packet)
	{
		this.isConnected = packet.getPacketSuccess();
		this.channelID = packet.getChannelID();
		this.name = packet.getChannelName();
		this.packetReceived = true;
	}
	
	public void leaveChannel(LacewingClient c) throws LacewingException
	{
		if(!isConnected())
			throw new LacewingException("Cannot leave a channel you have not joined");
		
		try {
			c.getPacketHandler().registerPacketTrigger(ReadPacket0Response3LeaveChannel.class, new MethodObjectPair(this.getClass().getMethod("handleLeaveChannel", ReadPacket0Response3LeaveChannel.class), this));
			WritePacket.writePacketToStream(c.getOutputStream(), new WritePacket0Request3LeaveChannel(this.getChannelID()));
			while(!packetReceived) Boolean.toString(packetReceived);
			packetReceived = false;
		} catch (NoSuchMethodException | SecurityException | IOException | LacewingException e) {
			e.printStackTrace();
		}
	}
	
	public void handleLeaveChannel(ReadPacket0Response3LeaveChannel packet)
	{
		this.isConnected = !packet.getPacketSuccess();
		this.channelID = packet.getChannelID();
		this.packetReceived = true;
	}
	
	public String toString()
	{
		return "Channel { name: \"" + this.getName() + "\"" + (this.getChannelID() == -1 ? "" : (" id: " + this.getChannelID())) + " peers: " + peers.size() + " }";
	}
	
	public void sendBinaryMessage(LacewingClient lc, int subChannel, byte[] data) throws LacewingException
	{
		if(!isConnected())
			throw new LacewingException("Cannot send a message to an unjoined channel!");
		
		try {
			WritePacket.writePacketToStream(lc.getOutputStream(), new WritePacket2BinaryChannelMessage(subChannel, getChannelID(), data));
		} catch (IOException e) {
			LacewingException le = new LacewingException("Unable to send binary message to channel \"" + this.getName() + "\"");
			le.initCause(e);
			throw le;
		}
	}
	
	public static Channel getChannel(LacewingClient lc, String name)
	{
		if(!lc.globalChannelMap.containsKey(name))
			lc.globalChannelMap.put(name, new Channel(name));
		
		return lc.globalChannelMap.get(name);
	}
	
	public static Channel getChannel(LacewingClient lc, int id)
	{
		for(Channel ch : lc.globalChannelMap.values())
			if(ch.getChannelID() == id)
				return ch;
		
		return null;
	}
	
	public static Channel[] getConnectedChannels(LacewingClient lc)
	{
		ArrayList<Channel> toReturn = new ArrayList<Channel>();
		
		for(Channel ch : lc.globalChannelMap.values())
			if(ch.isConnected())
				toReturn.add(ch);
		
		return toReturn.toArray(new Channel[0]);
	}
	
	public static Channel getChannelFromChannelList(InputStream is) throws IOException
	{
		int peerCount = DataTools.readInversedShort(is), nameLength = is.read();
		String name = new String(DataTools.readDataBlock(is, nameLength));
		Channel toReturn = getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
		toReturn.initPeerList(peerCount);
		return toReturn;
	}
}
