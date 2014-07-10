package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.Channel;
import net.lotrek.lacewing.client.structure.Peer;

public class ReadPacket0Response2JoinChannel extends ReadPacket0Response
{
	private boolean isChannelMaster;
	private String name;
	private int channelID;
	
	public int getPacketSubtype()
	{
		return 2;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		isChannelMaster = is.read() == 1;
		int nameLength = (int)is.read() & 0xff;
		try {
			name = new String(DataTools.readDataBlock(is, nameLength));
			channelID = DataTools.readInversedShort(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Peer> peers = new ArrayList<Peer>();
		
		while(is.available() > 0)
			try {
				peers.add(Peer.readPeerFromJoinChannel(PacketHandlerClient.getThreadAsThis().getClient(), is));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		Channel channelObj = Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
		channelObj.resetPeerList(peers.toArray(new Peer[0]));
	}
	
	public boolean isChannelMaster()
	{
		return this.isChannelMaster;
	}
	
	public String getChannelName()
	{
		return this.name;
	}
	
	public int getChannelID()
	{
		return this.channelID;
	}
	
	public Channel getChannelObject()
	{
		return Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
	}
}
