package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerReadPacket8ChannelMaster extends ServerReadPacket
{
	private int channel, peer;
	
	public ServerReadPacket getProcessedPacket()
	{
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(getPacketData(), 0, 5);
			channel = DataTools.readInversedShort(bais);
			bais.read();
			peer = DataTools.readInversedShort(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public int getPeer()
	{
		return peer;
	}
	
	public int getChannel()
	{
		return channel;
	}
	
	public int getPacketType()
	{
		return 8;
	}
}
