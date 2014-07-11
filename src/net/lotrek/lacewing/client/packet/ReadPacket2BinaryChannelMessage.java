package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.client.structure.Channel;
import net.lotrek.lacewing.client.structure.Peer;

public class ReadPacket2BinaryChannelMessage extends ReadPacket
{
	private int subChannel, peerID, channel;
	private byte[] data;
	
	public ReadPacket getProcessedPacket()
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(getPacketData(), 1, getPacketData().length - 1);
		subChannel = (int)this.getPacketData()[0] & 0xff;
		try {
			channel = DataTools.readInversedShort(bais);
			peerID = DataTools.readInversedShort(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = new byte[this.getPacketData().length - 5];
		System.arraycopy(getPacketData(), 5, data, 0, data.length);
		
		return this;
	}

	public int getSubChannel()
	{
		return subChannel;
	}
	
	public Channel getChannel()
	{
		return Channel.getChannel(PacketHandlerClient.getThreadAsThis().getClient(), channel);
	}
	
	public Peer getPeer()
	{
		return Peer.getPeer(PacketHandlerClient.getThreadAsThis().getClient(), peerID);
	}
	
	public byte[] getMessageData()
	{
		return this.data;
	}
	
	public int getPacketType()
	{
		return 2;
	}
}
