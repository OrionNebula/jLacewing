package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class WritePacket1BinaryServerMessage extends WritePacket
{
	private byte[] data;
	private int subChannel;
	
	public WritePacket1BinaryServerMessage(int subChannel, byte[] data)
	{
		this.subChannel = subChannel & 0xff;
		this.data = data;
	}
	
	public int getPacketLength()
	{
		return 1 + data.length;
	}

	public int getPacketType()
	{
		return 1;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel);
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
