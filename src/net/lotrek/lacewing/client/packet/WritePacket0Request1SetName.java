package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class WritePacket0Request1SetName extends WritePacket
{
	private String name;
	
	public WritePacket0Request1SetName(String name)
	{
		this.name = name;
	}
	
	public int getPacketLength()
	{
		return name.length() + 1;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.writeByte(1);
			dos.write(name.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
