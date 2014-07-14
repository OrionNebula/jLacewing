package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.server.LacewingServer;
import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public class ServerWritePacket0Response4ChannelList extends ServerWritePacket
{
	private boolean success;
	private LacewingServer ls;
	private ServerClient toExclude;
	
	public ServerWritePacket0Response4ChannelList(LacewingServer ls, ServerClient toExclude, boolean success)
	{
		this.success = success;
		this.ls = ls;
		this.toExclude = toExclude;
	}
	
	public int getPacketLength()
	{
		int channelLength = 0;
		if(success)
			for(ServerChannel ch : ls.globalChannelList.values().toArray(new ServerChannel[0]))
				channelLength += ch.predictLengthInChannelList();
		return 2 + channelLength;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(4);
			dos.write(success ? 1 : 0);
			if(success)
				for(ServerChannel ch : ls.globalChannelList.values().toArray(new ServerChannel[0]))
					ch.writeToChannelList(dos, toExclude);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
