package net.lotrek.lacewing.server.packet;

public class ServerReadPacket9Pong extends ServerReadPacket
{
	public ServerReadPacket getProcessedPacket()
	{
		return this;
	}

	public int getPacketType()
	{
		return 9;
	}

}
