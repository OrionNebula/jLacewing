package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ClientWritePacket6ObjectPeerMessage extends ClientWritePacket
{
	private byte[] data;
	private int subChannel, channel, peer;
	
	public ClientWritePacket6ObjectPeerMessage(int subChannel, int channel, int peer, String data)
	{
		this.subChannel = subChannel & 0xff;
		this.channel = channel;
		this.peer = peer;
		this.data = data.getBytes();
	}
	
	public int getPacketLength()
	{
		return 5 + data.length;
	}

	public int getPacketType()
	{
		return 3;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(subChannel);
			DataTools.writeInversedShort(dos, channel);
			DataTools.writeInversedShort(dos, peer);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needsFirstZero()
	{
		return false;
	}

}
