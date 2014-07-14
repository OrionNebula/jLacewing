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
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response2JoinChannel;
import net.lotrek.lacewing.client.packet.ClientReadPacket0Response3LeaveChannel;
import net.lotrek.lacewing.client.packet.ClientWritePacket;
import net.lotrek.lacewing.client.packet.ClientWritePacket0Request2JoinChannel;
import net.lotrek.lacewing.client.packet.ClientWritePacket0Request3LeaveChannel;
import net.lotrek.lacewing.client.packet.ClientWritePacket2BinaryChannelMessage;
import net.lotrek.lacewing.client.packet.ClientWritePacket8ChannelMaster;

public class ClientChannel
{
	private String name;
	private int channelID = -1;
	private boolean isConnected, isChannelMaster;
	private volatile boolean packetReceived;
	private ArrayList<ClientPeer> peers = new ArrayList<ClientPeer>();
	
	private ClientChannel(String name)
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
	public ClientPeer[] getPeers()
	{
		return peers.toArray(new ClientPeer[0]);
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
			peers.addAll(Arrays.asList(new ClientPeer[peerCount - peers.size()]));
	}
	
	/**
	 * Replaces the peer list.
	 * 
	 * @param peers the peers to add
	 */
	public void resetPeerList(ClientPeer[] peers)
	{
		this.peers.clear();
		this.peers.addAll(Arrays.asList(peers));
	}
	
	/**
	 * Adds a single peer
	 * 
	 * @param peerObj the peer to add
	 */
	public void addPeer(ClientPeer peerObj)
	{
		peers.add(peerObj);
	}
	
	/**
	 * Removes a single peer
	 * 
	 * @param peerObj the peer to remove
	 */
	public void removePeer(ClientPeer peerObj)
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
			c.getPacketHandler().registerPacketTrigger(ClientReadPacket0Response2JoinChannel.class, new MethodObjectPair("handleJoinChannel", ClientChannel.class, this));
			ClientWritePacket.writePacketToStream(c.getOutputStream(), new ClientWritePacket0Request2JoinChannel(name, hide, close));
			while(!packetReceived) Boolean.toString(packetReceived);
			packetReceived = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleJoinChannel(ClientReadPacket0Response2JoinChannel packet)
	{
		this.isConnected = packet.getPacketSuccess();
		if(this.isConnected)
		{
			this.channelID = packet.getChannelID();
			this.isChannelMaster = packet.isChannelMaster();
		}
		this.name = packet.getChannelName();
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
			ClientWritePacket.writePacketToStream(c.getOutputStream(), new ClientWritePacket0Request3LeaveChannel(this.getChannelID()));
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
	public void kickPeer(LacewingClient lc, ClientPeer peer) throws LacewingException
	{
		if(!peers.contains(peer) || !this.isConnected || !this.isChannelMaster)
			throw new LacewingException("Unable to kick peer");
		
		try {
			ClientWritePacket.writePacketToStream(lc.getOutputStream(), new ClientWritePacket8ChannelMaster(getChannelID(), 0, peer.getPeerID()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		return "ClientChannel { name: \"" + this.getName() + "\"" + (this.getChannelID() == -1 ? "" : (" id: " + this.getChannelID())) + " peers: " + peers.size() + " }";
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
			ClientWritePacket.writePacketToStream(lc.getOutputStream(), new ClientWritePacket2BinaryChannelMessage(subChannel, getChannelID(), data));
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
	public static ClientChannel getChannel(LacewingClient lc, String name)
	{
		if(!lc.globalChannelMap.containsKey(name))
			lc.globalChannelMap.put(name, new ClientChannel(name));
		
		return lc.globalChannelMap.get(name);
	}
	
	/**
	 * Attempts to get a channel by its id
	 * 
	 * @param lc the client to get from
	 * @param id the id to search for
	 * @return the channel retrieved; returns null if it does not exist
	 */
	public static ClientChannel getChannel(LacewingClient lc, int id)
	{
		for(ClientChannel ch : lc.globalChannelMap.values())
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
	public static ClientChannel[] getConnectedChannels(LacewingClient lc)
	{
		ArrayList<ClientChannel> toReturn = new ArrayList<ClientChannel>();
		
		for(ClientChannel ch : lc.globalChannelMap.values())
			if(ch.isConnected())
				toReturn.add(ch);
		
		return toReturn.toArray(new ClientChannel[0]);
	}
	
	/**
	 * Reads a client from an InputStream
	 * 
	 * @param is the stream to read from
	 * @return the channel object read
	 * @throws IOException thrown on a stream error
	 */
	public static ClientChannel getChannelFromChannelList(InputStream is) throws IOException
	{
		int peerCount = DataTools.readInversedShort(is), nameLength = is.read();
		String name = new String(DataTools.readDataBlock(is, nameLength));
		ClientChannel toReturn = getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
		toReturn.initPeerList(peerCount);
		return toReturn;
	}
	
	public static void handleLeaveChannel(ClientReadPacket0Response3LeaveChannel packet)
	{
		ClientChannel channel = ClientChannel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), packet.getChannelID());
		channel.isConnected = !packet.getPacketSuccess();
		channel.isChannelMaster &= !packet.getPacketSuccess();
		if(packet.getPacketSuccess())
			channel.channelID = -1;
		PacketHandlerClient.getThreadAsThis().getClient().getPacketActions().onChannelLeft(packet.getPacketSuccess(), channel);
	}
}
