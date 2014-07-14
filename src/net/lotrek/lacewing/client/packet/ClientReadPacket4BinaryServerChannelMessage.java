package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.ClientChannel;

public class ClientReadPacket4BinaryServerChannelMessage extends ClientReadPacket
{
	private int subChannel, channel;
	private byte[] data;
	
	public ClientReadPacket getProcessedPacket()
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
	
	public ClientChannel getChannel()
	{
		return ClientChannel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), channel);
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
