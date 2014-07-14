package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.ClientChannel;

public class ClientReadPacket0Response3LeaveChannel extends ClientReadPacket0Response
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
	
	public ClientChannel getChannelObject()
	{
		return ClientChannel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), getChannelID());
	}
}
