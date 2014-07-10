package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.Channel;

public class ReadPacket0Response3LeaveChannel extends ReadPacket0Response
{
	private int channelID;
	
	public int getPacketSubtype()
	{
		return 3;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			channelID = DataTools.readInversedShort(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChannelID()
	{
		return this.channelID;
	}
	
	public Channel getChannelObject()
	{
		return Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), getChannelID());
	}
}
