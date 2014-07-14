package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class ClientWritePacket0Request1SetName extends ClientWritePacket
{
	private String name;
	
	public ClientWritePacket0Request1SetName(String name)
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
