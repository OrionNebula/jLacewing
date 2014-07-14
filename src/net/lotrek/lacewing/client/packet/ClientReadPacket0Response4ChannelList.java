package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.client.structure.ClientChannel;

public class ClientReadPacket0Response4ChannelList extends ClientReadPacket0Response
{
	private ArrayList<ClientChannel> channels = new ArrayList<ClientChannel>();
	
	public int getPacketSubtype()
	{
		return 4;
	}

	public void readPacketData(ByteArrayInputStream is)
	{
		while(is.available() > 0)
			try {
				channels.add(ClientChannel.getChannelFromChannelList(is));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public ClientChannel[] getChannels()
	{
		return channels.toArray(new ClientChannel[0]);
	}
}
