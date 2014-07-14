package net.lotrek.lacewing.server.packet;

public class ServerReadPacket1BinaryServerMessage extends ServerReadPacket
{
	private int subChannel;
	private byte[] data;
	
	public ServerReadPacket getProcessedPacket()
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
	
	public byte[] getMessageData()
	{
		return this.data;
	}
	
	public int getPacketType()
	{
		return 1;
	}
}
