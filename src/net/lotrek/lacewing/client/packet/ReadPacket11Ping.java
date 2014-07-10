package net.lotrek.lacewing.client.packet;

public class ReadPacket11Ping extends ReadPacket
{
	public ReadPacket getProcessedPacket()
	{
		return this;
	}

	public int getPacketType()
	{
		return 11;
	}

}
