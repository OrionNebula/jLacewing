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
import net.lotrek.lacewing.client.packet.WritePacket8ChannelMaster;

public class Channel
{
	private String name;
	private int channelID = -1;
	private boolean isConnected, isChannelMaster;
	private volatile boolean packetReceived;
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	
	private Channel(String name)
	{
		this.name = name;
	}
	
	/**
	 * Gets the name of the channel. This should never be null.
	 * 
	 * @return the channel's name
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Gets the ID of the channel. This may be -1 if the channel has never been joined.
	 * 
	 * @return the channel's ID
	 */
	public int getChannelID()
	{
		return channelID;
	}
	
	/**
	 * Gets the list of peers. The elements of this array may be null if the channel has never been joined.
	 * 
	 * @return the list of peers
	 */
	public Peer[] getPeers()
	{
		return peers.toArray(new Peer[0]);
	}
	
	/**
	 * Gets if the channel is connected to
	 * 
	 * @return isConnected
	 */
	public boolean isConnected()
	{
		return isConnected;
	}
	
	/**
	 * Returns true if the client is the owner of this channel
	 * 
	 * @return isChannelMaster
	 */
	public boolean isChannelMaster()
	{
		return this.isChannelMaster;
	}
	
	/**
	 * Initializes the blank portions of the peer list with null peers up to a certain count.
	 * 
	 * @param peerCount the final size of the list
	 */
	public void initPeerList(int peerCount)
	{
		if(peers.size() > peerCount)
			peers.subList(peerCount, peers.size() - 1).clear();
		else
			peers.addAll(Arrays.asList(new Peer[peerCount - peers.size()]));
	}
	
	/**
	 * Replaces the peer list.
	 * 
	 * @param peers the peers to add
	 */
	public void resetPeerList(Peer[] peers)
	{
		this.peers.clear();
		this.peers.addAll(Arrays.asList(peers));
	}
	
	/**
	 * Adds a single peer
	 * 
	 * @param peerObj the peer to add
	 */
	public void addPeer(Peer peerObj)
	{
		peers.add(peerObj);
	}
	
	/**
	 * Removes a single peer
	 * 
	 * @param peerObj the peer to remove
	 */
	public void removePeer(Peer peerObj)
	{
		peers.remove(peerObj);
	}
	
	/**
	 * Attempts to join this channel
	 * 
	 * @param c the client to join with
	 * @param hide true if the channel should be hidden from the channel list if creating
	 * @param close true if the channel should be closed when this client leaves if creating
	 * @throws LacewingException thrown if joining fails or is impossible
	 */
	public void joinChannel(LacewingClient c, boolean hide, boolean close) throws LacewingException
	{
		if(isConnected)
			throw new LacewingException("Cannot join a channel you have already joined");
		
		try {
			c.getPacketHandler().registerPacketTrigger(ReadPacket0Response2JoinChannel.class, new MethodObjectPair("handleJoinChannel", Channel.class, this));
			WritePacket.writePacketToStream(c.getOutputStream(), new WritePacket0Request2JoinChannel(name, hide, close));
			while(!packetReceived) Boolean.toString(packetReceived);
			packetReceived = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleJoinChannel(ReadPacket0Response2JoinChannel packet)
	{
		this.isConnected = packet.getPacketSuccess();
		this.channelID = packet.getChannelID();
		this.name = packet.getChannelName();
		this.isChannelMaster = packet.isChannelMaster();
		this.packetReceived = true;
	}
	
	/**
	 * Attempts to leave this channel
	 * 
	 * @param c the client to leave with
	 * @throws LacewingException thrown if leaving fails or is impossible
	 */
	public void leaveChannel(LacewingClient c) throws LacewingException
	{
		if(!isConnected())
			throw new LacewingException("Cannot leave a channel you have not joined");
		
		try {
			WritePacket.writePacketToStream(c.getOutputStream(), new WritePacket0Request3LeaveChannel(this.getChannelID()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to kick a peer from this channel
	 * 
	 * @param lc the client to use when kicking
	 * @param peer the peer to kick
	 * @throws LacewingException thrown if kicking fails or is impossible
	 */
	public void kickPeer(LacewingClient lc, Peer peer) throws LacewingException
	{
		if(!peers.contains(peer) || !this.isConnected || !this.isChannelMaster)
			throw new LacewingException("Unable to kick peer");
		
		try {
			WritePacket.writePacketToStream(lc.getOutputStream(), new WritePacket8ChannelMaster(getChannelID(), 0, peer.getPeerID()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return "Channel { name: \"" + this.getName() + "\"" + (this.getChannelID() == -1 ? "" : (" id: " + this.getChannelID())) + " peers: " + peers.size() + " }";
	}
	
	/**
	 * Attempts to send a binary message to the channel
	 * 
	 * @param lc the client to send from
	 * @param subChannel the subchannel to send on
	 * @param data the data to send
	 * @throws LacewingException thrown if sending fails or is impossible
	 */
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
	
	/**
	 * Gets a channel object from the pool
	 * 
	 * @param lc the client to get from
	 * @param name the name of the channel
	 * @return the channel retrieved
	 */
	public static Channel getChannel(LacewingClient lc, String name)
	{
		if(!lc.globalChannelMap.containsKey(name))
			lc.globalChannelMap.put(name, new Channel(name));
		
		return lc.globalChannelMap.get(name);
	}
	
	/**
	 * Attempts to get a channel by its id
	 * 
	 * @param lc the client to get from
	 * @param id the id to search for
	 * @return the channel retrieved; returns null if it does not exist
	 */
	public static Channel getChannel(LacewingClient lc, int id)
	{
		for(Channel ch : lc.globalChannelMap.values())
			if(ch.getChannelID() == id)
				return ch;
		
		return null;
	}
	
	/**
	 * Gets all channels a client is connected to
	 * 
	 * @param lc the client to get from
	 * @return all connected channels
	 */
	public static Channel[] getConnectedChannels(LacewingClient lc)
	{
		ArrayList<Channel> toReturn = new ArrayList<Channel>();
		
		for(Channel ch : lc.globalChannelMap.values())
			if(ch.isConnected())
				toReturn.add(ch);
		
		return toReturn.toArray(new Channel[0]);
	}
	
	/**
	 * Reads a client from an InputStream
	 * 
	 * @param is the stream to read from
	 * @return the channel object read
	 * @throws IOException thrown on a stream error
	 */
	public static Channel getChannelFromChannelList(InputStream is) throws IOException
	{
		int peerCount = DataTools.readInversedShort(is), nameLength = is.read();
		String name = new String(DataTools.readDataBlock(is, nameLength));
		Channel toReturn = getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
		toReturn.initPeerList(peerCount);
		return toReturn;
	}
	
	public static void handleLeaveChannel(ReadPacket0Response3LeaveChannel packet)
	{
		Channel channel = Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), packet.getChannelID());
		channel.isConnected = !packet.getPacketSuccess();
		channel.isChannelMaster &= !packet.getPacketSuccess();
	}
}
