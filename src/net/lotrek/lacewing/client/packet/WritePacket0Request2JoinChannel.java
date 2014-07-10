package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class WritePacket0Request2JoinChannel extends WritePacket
{
	private String name;
	private byte flags;
	
	public WritePacket0Request2JoinChannel(String name, boolean hideIfCreating, boolean closeIfCreating)
	{
		this.name = name;
		flags = (byte) (((hideIfCreating ? 1 : 0) << 1) | (closeIfCreating ? 1 : 0));
	}
	
	public int getPacketLength()
	{
		return name.length() + 2;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(new byte[]{2, flags});
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
