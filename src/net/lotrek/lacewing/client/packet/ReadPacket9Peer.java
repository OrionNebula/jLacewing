package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.Channel;
import net.lotrek.lacewing.client.structure.Peer;

public class ReadPacket9Peer extends ReadPacket
{
	public static final int JOIN = 0, CHANGE = 1, LEFT = 2;
	
	private int channel, peer;
	private boolean isChannelMaster;
	private String name;
	
	public ReadPacket getProcessedPacket()
	{
		try {
			InputStream is = new ByteArrayInputStream(getPacketData());
			channel = DataTools.readInversedShort(is);
			peer = DataTools.readInversedShort(is);
			if(is.available() > 0)
			{
				isChannelMaster = is.read() == 1;
				name = new String(DataTools.readDataBlock(is, is.available()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public int getActionType(LacewingClient lc)
	{
		if(!lc.knownPeers.containsKey(peer)) 	return JOIN;
		else if(name == null) 					return LEFT;
		else 									return CHANGE;
	}
	
	public int getChannelID()
	{
		return channel;
	}
	
	public int getPeerID()
	{
		return peer;
	}
	
	public boolean isChannelMaster()
	{
		return isChannelMaster;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void updatePeer(LacewingClient lc)
	{
		Peer peerObj = Peer.getPeer(lc, peer);
		Channel channelObj = Channel.getChannel(lc, channel);
		
		switch (getActionType(lc)) {
		case JOIN:
			peerObj.setName(name);
			peerObj.setIsMaster(isChannelMaster);
			channelObj.addPeer(peerObj);
			break;
		case CHANGE:
			peerObj.setName(name);
			peerObj.setIsMaster(isChannelMaster);
			break;
		case LEFT:
			channelObj.removePeer(peerObj);
			lc.knownPeers.remove(peer);
			break;
		default:
			System.out.println("Some freaky impossible error occurred!");
			break;
		}
	}
	
	public int getPacketType()
	{
		return 9;
	}

}
