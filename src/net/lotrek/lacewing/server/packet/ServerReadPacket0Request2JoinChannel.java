package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket0Request2JoinChannel extends ServerReadPacket0Request
{
	private boolean hide, close;
	private String name;
	
	public int getPacketSubtype()
	{
		return 2;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			int flags = is.read();
			hide = (flags & 1) == 1;
			close = ((flags >> 1) & 1) == 1;
			name = new String(DataTools.readDataBlock(is, is.available()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getIsHiding()
	{
		return hide;
	}
	
	public boolean getIsClosing()
	{
		return close;
	}
}
