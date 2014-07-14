package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ClientWritePacket0Request3LeaveChannel extends ClientWritePacket
{
	private int id;
	
	public ClientWritePacket0Request3LeaveChannel(int id)
	{
		this.id = id;
	}
	
	public int getPacketLength()
	{
		return 3;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(new byte[]{3});
			DataTools.writeInversedShort(dos, id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
