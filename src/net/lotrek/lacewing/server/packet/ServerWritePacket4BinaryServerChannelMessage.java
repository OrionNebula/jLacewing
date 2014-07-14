package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerWritePacket4BinaryServerChannelMessage extends ServerWritePacket
{
	private int subChannel, channel;
	private byte[] data;
	
	public ServerWritePacket4BinaryServerChannelMessage(int subChannel, int channel, byte[] data)
	{
		this.subChannel = subChannel;
		this.channel = channel;
		this.data = data;
	}
	
	public int getPacketLength()
	{
		return data.length + 3;
	}

	public int getPacketType()
	{
		return 4;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel & 0xf);
			DataTools.writeInversedShort(dos, channel);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
