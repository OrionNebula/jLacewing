package net.lotrek.lacewing.server.packet;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public abstract class ServerReadPacket0Request extends ServerReadPacket
{
	private static ArrayList<Class<? extends ServerReadPacket0Request>> packetMap = new ArrayList<Class<? extends ServerReadPacket0Request>>();
	private byte privateType;
	
	public abstract int getPacketSubtype();
	public abstract void readPacketData(ByteArrayInputStream is);
	
	public ServerReadPacket getProcessedPacket()
	{
		byte type = this.getPacketData()[0];
		privateType = type;
		try {
			ServerReadPacket0Request packet = packetMap.get(type).newInstance();
			ByteArrayInputStream bais = new ByteArrayInputStream(getPacketData(), 1, getPacketData().length - 1);
			packet.readPacketData(bais);
			return packet;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	public int getPacketType()
	{
		return 0;
	}
	
	public final int getPrivateType()
	{
		return privateType;
	}

	public static void registerSubpacket(Class<? extends ServerReadPacket0Request> packetType, int id)
	{
		packetMap.add(id, packetType);
	}
}
