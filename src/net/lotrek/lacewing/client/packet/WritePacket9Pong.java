package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;

public class WritePacket9Pong extends WritePacket {

	public int getPacketLength()
	{
		return 0;
	}

	public int getPacketType()
	{
		return 9;
	}

	public void writePacketData(DataOutputStream dos) {}

	public boolean needsFirstZero()
	{
		return false;
	}

}
