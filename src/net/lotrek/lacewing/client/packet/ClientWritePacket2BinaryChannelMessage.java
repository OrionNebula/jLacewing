package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ClientWritePacket2BinaryChannelMessage extends ClientWritePacket
{
	private byte[] data;
	private int subChannel, channel;
	
	public ClientWritePacket2BinaryChannelMessage(int subChannel, int channel, byte[] data)
	{
		this.subChannel = subChannel & 0xff;
		this.channel = channel;
		this.data = data;
	}
	
	public int getPacketLength()
	{
		return 3 + data.length;
	}

	public int getPacketType()
	{
		return 2;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel);
			DataTools.writeInversedShort(dos, channel);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
