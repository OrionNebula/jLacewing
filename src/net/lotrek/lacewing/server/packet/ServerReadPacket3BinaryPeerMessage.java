package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket3BinaryPeerMessage extends ServerReadPacket
{
	private int subChannel, channel, peer;
	private byte[] data;
	
	public ServerReadPacket getProcessedPacket()
	{
		subChannel = (int)this.getPacketData()[0] & 0xff;
		try {
			channel = DataTools.readInversedShort(new ByteArrayInputStream(getPacketData(), 1, this.getPacketData().length - 1));
			peer = DataTools.readInversedShort(new ByteArrayInputStream(getPacketData(), 3, this.getPacketData().length - 3));
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = new byte[this.getPacketData().length - 5];
		System.arraycopy(getPacketData(), 5, data, 0, data.length);
		
		return this;
	}

	public int getSubChannel()
	{
		return subChannel;
	}
	
	public int getChannel()
	{
		return channel;
	}
	
	public int getPeer()
	{
		return peer;
	}
	
	public byte[] getMessageData()
	{
		return this.data;
	}
	
	public int getPacketType()
	{
		return 3;
	}
}
