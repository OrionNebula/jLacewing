package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;

public class ClientReadPacket9Peer extends ClientReadPacket
{
	public static final int JOIN = 0, CHANGE = 1, LEFT = 2;
	
	private int channel, peer, action = -1;
	private boolean isChannelMaster;
	private String name;
	
	public ClientReadPacket getProcessedPacket()
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
			action = getActionType(PacketHandlerClient.getThreadAsThis().getClient());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public int getActionType(LacewingClient lc)
	{
		if(action > -1)
			return action;
		
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
		ClientPeer peerObj = ClientPeer.getPeer(lc, peer);
		ClientChannel channelObj = ClientChannel.getChannel(lc, channel);
		
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
