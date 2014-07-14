package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;

public class ClientReadPacket0Response2JoinChannel extends ClientReadPacket0Response
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
		if(this.getPacketSuccess())
			isChannelMaster = is.read() == 1;
		int nameLength = (int)is.read() & 0xff;
		try {
			name = new String(DataTools.readDataBlock(is, nameLength));
			if(this.getPacketSuccess())
				channelID = DataTools.readInversedShort(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<ClientPeer> peers = new ArrayList<ClientPeer>();
		
		while(is.available() > 0)
			try {
				peers.add(ClientPeer.readPeerFromJoinChannel(PacketHandlerClient.getThreadAsThis().getClient(), is));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		ClientChannel channelObj = ClientChannel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
		channelObj.resetPeerList(peers.toArray(new ClientPeer[0]));
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
	
	public ClientChannel getChannelObject()
	{
		return ClientChannel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), name);
	}
}
