package net.lotrek.lacewing.client.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;

public abstract class ClientWritePacket
{
	public abstract int getPacketLength();
	public abstract int getPacketType();
	public abstract void writePacketData(DataOutputStream dos);
	public abstract boolean needsFirstZero();
	
	public static void writePacketToStream(DataOutputStream dos, ClientWritePacket packet) throws IOException, LacewingException
	{
		if(packet.needsFirstZero())
			dos.writeByte(0);
		
		dos.writeByte(packet.getPacketType() << 4);
		DataTools.writeLengthHeader(dos, packet.getPacketLength());
		packet.writePacketData(dos);
	}
}
