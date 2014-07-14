package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket0Request1SetName extends ServerReadPacket0Request
{
	private String name;
	
	public int getPacketSubtype()
	{
		return 1;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			name = new String(DataTools.readDataBlock(is, is.available()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName()
	{
		return name;
	}
}
