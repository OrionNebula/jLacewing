package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket0Request0Connect extends ServerReadPacket0Request
{
	private String version;
	
	public int getPacketSubtype()
	{
		return 0;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			version = new String(DataTools.readDataBlock(is, is.available()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getVersion()
	{
		return version;
	}
}
