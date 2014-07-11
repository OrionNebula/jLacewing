package net.lotrek.lacewing.client.structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.packet.WritePacket;
import net.lotrek.lacewing.client.packet.WritePacket3BinaryPeerMessage;

public class Peer
{
	private int peerID;
	private String name;
	private boolean isMaster;
	
	private Peer(int peerID)
	{
		this.peerID = peerID;
	}
	
	/**
	 * Gets this peer's id
	 * 
	 * @return the peer id
	 */
	public int getPeerID()
	{
		return peerID;
	}
	
	/**
	 * gets this peer's name. Can return null if the name is unknown
	 * 
	 * @return the peer name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets this peer's name; can cause errors if set manually
	 * 
	 * @param newName the name to set to
	 */
	public void setName(String newName)
	{
		this.name = newName;
	}
	
	/**
	 * Gets if this peer is the channel master
	 * 
	 * @return is master
	 */
	public boolean isMaster() {
		return isMaster;
	}

	/**
	 * Sets if the peer is the channel master; can cause errors if set manually
	 * 
	 * @param isMaster to set isMaster
	 */
	public void setIsMaster(boolean isMaster)
	{
		this.isMaster = isMaster;
	}
	
	public String toString()
	{
		return "Peer { name: \"" + this.getName() + "\" id: " + peerID + " }";
	}
	
	/**
	 * Attempts to send a binary message to this peer
	 * 
	 * @param lc the client to send on
	 * @param subchannel the subchannel to send on
	 * @param channel the channel to send on; the peer must be a member of this channel
	 * @param data the data to send
	 * @throws LacewingException thrown if sending fails or is impossible
	 */
	public void sendBinaryMessage(LacewingClient lc, int subchannel, Channel channel, byte[] data) throws LacewingException
	{
		if(channel.isConnected())
		{
			if(!Arrays.asList(channel.getPeers()).contains(this))
				throw new LacewingException("Peer is not a member of this channel");
		}else
			throw new LacewingException("Client not connected to channel");
		
		try {
			WritePacket.writePacketToStream(lc.getOutputStream(), new WritePacket3BinaryPeerMessage(subchannel, channel.getChannelID(), getPeerID(), data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets a peer from the client's pool
	 * 
	 * @param lc the client to retrieve from
	 * @param id the peer's id
	 * @return the retrieved peer object
	 */
	public static Peer getPeer(LacewingClient lc, int id)
	{
		if(!lc.knownPeers.containsKey(id))
			lc.knownPeers.put(id, new Peer(id));
		
		return lc.knownPeers.get(id);
	}
	
	/**
	 * Reads a peer from an InputStream
	 * 
	 * @param lc the client to read into
	 * @param is the stream to read from
	 * @return the peer object read
	 * @throws IOException thrown on a stream error
	 */
	public static Peer readPeerFromJoinChannel(LacewingClient lc, InputStream is) throws IOException
	{
		int id = DataTools.readInversedShort(is);
		boolean isMaster = is.read() == 1;
		int nameLength = (int)is.read() & 0xff;
		String name = new String(DataTools.readDataBlock(is, nameLength));
		Peer toReturn = getPeer(lc, id);
		toReturn.setIsMaster(isMaster);
		toReturn.setName(name);
		return toReturn;
	}
}
