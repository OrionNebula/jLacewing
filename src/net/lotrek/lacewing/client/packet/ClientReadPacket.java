package net.lotrek.lacewing.client.packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.DataTools;

public abstract class ClientReadPacket
{
	private static ArrayList<Class<? extends ClientReadPacket>> packetMap = new ArrayList<>(12);
	private byte[] data;
	private int variant;
	
	public abstract ClientReadPacket getProcessedPacket();
	public abstract int getPacketType();
	
	public ClientReadPacket(){}
	
	public byte[] getPacketData()
	{
		return data;
	}
	
	public int getVariant()
	{
		return variant;
	}
	
	public static void registerPacket(Class<? extends ClientReadPacket> packetType, int id)
	{
		if(packetMap.size() - 1 > id)
			packetMap.set(id, packetType);
		packetMap.add(id, packetType);
	}
	
	public static ClientReadPacket readPacketFromStream(DataInputStream dis) throws IOException
	{
		if(dis.available() == 0)
			return null;
		
		int rawType = (int)dis.readByte() & 0xff, realType = (rawType >> 4) & 0xf, subType = (rawType) & 0xf;
		try {
			ClientReadPacket packet = packetMap.get(realType).newInstance();
			packet.variant = subType;
			packet.data = DataTools.readDataBlock(dis, (int) DataTools.readLengthHeader(dis)[0]);
			return packet.getProcessedPacket();
		} catch (InstantiationException | IllegalAccessException | IndexOutOfBoundsException e) {
			if(e instanceof IndexOutOfBoundsException)
				System.out.println(realType + " does not have a read mapping");
			e.printStackTrace();
		}
		return null;
	}
}
