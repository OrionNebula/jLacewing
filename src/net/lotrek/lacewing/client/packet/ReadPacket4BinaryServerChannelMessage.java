package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.Channel;

public class ReadPacket4BinaryServerChannelMessage extends ReadPacket
{
	private int subChannel, channel;
	private byte[] data;
	
	public ReadPacket getProcessedPacket()
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(getPacketData(), 1, getPacketData().length - 1);
		subChannel = (int)this.getPacketData()[0] & 0xff;
		try {
			channel = DataTools.readInversedShort(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = new byte[this.getPacketData().length - 3];
		System.arraycopy(getPacketData(), 3, data, 0, data.length);
		
		return this;
	}

	public int getSubChannel()
	{
		return subChannel;
	}
	
	public Channel getChannel()
	{
		return Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), channel);
	}
	
	public byte[] getMessageData()
	{
		return this.data;
	}
	
	public int getPacketType()
	{
		return 4;
	}
}
