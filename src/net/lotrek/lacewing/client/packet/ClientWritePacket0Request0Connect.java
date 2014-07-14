package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class ClientWritePacket0Request0Connect extends ClientWritePacket
{
	public int getPacketLength()
	{
		return "revision 3".length() + 1;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.writeByte(0);
			dos.write("revision 3".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return true;
	}

}
