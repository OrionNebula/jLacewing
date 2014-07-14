package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ClientReadPacket0Response1SetName extends ClientReadPacket0Response
{
	private String name;
	
	public int getPacketSubtype()
	{
		return 1;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			name = new String(DataTools.readDataBlock(is, is.read()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getName()
	{
		return this.name;
	}
}
