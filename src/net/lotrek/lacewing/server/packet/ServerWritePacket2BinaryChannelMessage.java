package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerWritePacket2BinaryChannelMessage extends ServerWritePacket
{
	private int subChannel, channel, peer;
	private byte[] data;
	
	public ServerWritePacket2BinaryChannelMessage(int subChannel, int channel, int peer, byte[] data)
	{
		this.subChannel = subChannel;
		this.channel = channel;
		this.peer = peer;
		this.data = data;
	}
	
	public int getPacketLength()
	{
		return data.length + 5;
	}

	public int getPacketType()
	{
		return 2;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel & 0xf);
			DataTools.writeInversedShort(dos, channel);
			DataTools.writeInversedShort(dos, peer);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
