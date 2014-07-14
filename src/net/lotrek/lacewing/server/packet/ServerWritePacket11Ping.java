package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;

public class ServerWritePacket11Ping extends ServerWritePacket
{

	public int getPacketLength()
	{
		return 0;
	}

	public int getPacketType()
	{
		return 11;
	}

	public void writePacketData(DataOutputStream dos)
	{
		
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
