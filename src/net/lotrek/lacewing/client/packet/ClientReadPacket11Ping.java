package net.lotrek.lacewing.client.packet;

public class ClientReadPacket11Ping extends ClientReadPacket
{
	public ClientReadPacket getProcessedPacket()
	{
		return this;
	}

	public int getPacketType()
	{
		return 11;
	}

}
