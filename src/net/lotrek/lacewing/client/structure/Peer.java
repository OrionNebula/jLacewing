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
	
	public int getPeerID()
	{
		return peerID;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName)
	{
		this.name = newName;
	}
	
	public boolean isMaster() {
		return isMaster;
	}

	public void setIsMaster(boolean isMaster)
	{
		this.isMaster = isMaster;
	}
	
	public String toString()
	{
		return "Peer { name: \"" + this.getName() + "\" id: " + peerID + " }";
	}
	
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
	
	public static Peer getPeer(LacewingClient lc, int id)
	{
		if(!lc.knownPeers.containsKey(id))
			lc.knownPeers.put(id, new Peer(id));
		
		return lc.knownPeers.get(id);
	}
	
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
