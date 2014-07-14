package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ClientReadPacket0Response0Connect extends ClientReadPacket0Response
{
	private String welcomeMessage;
	private int clientID;
	
	public int getPacketSubtype()
	{
		return 0;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		try {
			clientID = DataTools.readInversedShort(is);
			welcomeMessage = new String(DataTools.readDataBlock(is, is.available()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getClientID()
	{
		return this.clientID;
	}
	
	public String getWelcomeMessage()
	{
		return this.welcomeMessage;
	}
}
