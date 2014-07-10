package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.client.structure.Channel;

public class ReadPacket0Response4ChannelList extends ReadPacket0Response
{
	private ArrayList<Channel> channels = new ArrayList<Channel>();
	
	public int getPacketSubtype()
	{
		return 4;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		while(is.available() > 0)
			try {
				channels.add(Channel.getChannelFromChannelList(is));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public Channel[] getChannels()
	{
		return channels.toArray(new Channel[0]);
	}
}
