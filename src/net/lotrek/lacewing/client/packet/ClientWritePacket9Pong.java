package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;

public class ClientWritePacket9Pong extends ClientWritePacket {

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
