package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket0Request3LeaveChannel extends ServerReadPacket0Request
{
	private int channelID;
	
	public int getPacketSubtype()
	{
		return 3;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			channelID = DataTools.readInversedShort(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChannelID()
	{
		return channelID;
	}
}
