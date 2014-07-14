package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;

public abstract class ServerWritePacket
{
	public abstract int getPacketLength();
	public abstract int getPacketType();
	public abstract void writePacketData(DataOutputStream dos);
	
	public static void writePacketToStream(DataOutputStream dos, ServerWritePacket packet) throws IOException, LacewingException
	{
		dos.writeByte((packet.getPacketType() & 0xf) << 4);
		DataTools.writeLengthHeader(dos, packet.getPacketLength());
		packet.writePacketData(dos);
	}
}
