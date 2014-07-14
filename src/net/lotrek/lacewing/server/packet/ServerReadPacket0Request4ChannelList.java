package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;

public class ServerReadPacket0Request4ChannelList extends ServerReadPacket0Request
{
	public int getPacketSubtype()
	{
		return 4;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		
	}
}
