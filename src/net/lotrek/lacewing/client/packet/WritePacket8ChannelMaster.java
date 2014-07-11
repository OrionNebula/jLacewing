package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class WritePacket8ChannelMaster extends WritePacket
{
	private int channel, action, peer;
	
	public WritePacket8ChannelMaster(int channel, int action, int peer)
	{
		this.channel = channel;
		this.action = action;
		this.peer = peer;
	}
	
	public int getPacketLength()
	{
		return 5;
	}

	public int getPacketType()
	{
		return 8;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			DataTools.writeInversedShort(dos, channel);
			dos.writeByte(action);
			DataTools.writeInversedShort(dos, peer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
