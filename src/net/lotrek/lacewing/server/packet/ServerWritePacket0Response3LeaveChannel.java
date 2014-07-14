package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerWritePacket0Response3LeaveChannel extends ServerWritePacket
{
	private int channelID;
	private boolean success;
	
	public ServerWritePacket0Response3LeaveChannel(int channelID, boolean success)
	{
		this.channelID = channelID;
		this.success = success;
	}
	
	public int getPacketLength()
	{
		return 4;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(3);
			dos.write(success ? 1 : 0);
			DataTools.writeInversedShort(dos, channelID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
