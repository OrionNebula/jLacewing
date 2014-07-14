package net.lotrek.lacewing.client.packet;

public class ClientReadPacket5ObjectServerMessage extends ClientReadPacket
{
	private int subChannel;
	private byte[] data;
	
	public ClientReadPacket getProcessedPacket()
	{
		subChannel = (int)this.getPacketData()[0] & 0xff;
		data = new byte[this.getPacketData().length - 1];
		System.arraycopy(getPacketData(), 1, data, 0, data.length);
		
		return this;
	}

	public int getSubChannel()
	{
		return subChannel;
	}
	
	public String getMessageData()
	{
		return new String(this.data);
	}
	
	public int getPacketType()
	{
		return 5;
	}
}
