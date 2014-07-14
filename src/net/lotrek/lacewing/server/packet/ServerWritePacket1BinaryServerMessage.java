package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class ServerWritePacket1BinaryServerMessage extends ServerWritePacket
{
	private int subChannel;
	private byte[] data;
	
	public ServerWritePacket1BinaryServerMessage(int subChannel, byte[] data)
	{
		this.subChannel = subChannel;
		this.data = data;
	}
	
	public int getPacketLength()
	{
		return data.length + 1;
	}

	public int getPacketType()
	{
		return 1;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel & 0xf);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
