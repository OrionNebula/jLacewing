package net.lotrek.lacewing.client.packet;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public abstract class ClientReadPacket0Response extends ClientReadPacket
{
	private static ArrayList<Class<? extends ClientReadPacket0Response>> packetMap = new ArrayList<Class<? extends ClientReadPacket0Response>>();
	private byte privateType;
	protected boolean packetSuccess;
	
	public abstract int getPacketSubtype();
	public abstract void readPacketData(ByteArrayInputStream is);
	
	public ClientReadPacket getProcessedPacket()
	{
		byte type = this.getPacketData()[0];
		privateType = type;
		boolean success = this.getPacketData()[1] == 1;
		this.packetSuccess = success;
		try {
			ClientReadPacket0Response packet = packetMap.get(type).newInstance();
			packet.packetSuccess = success;
			ByteArrayInputStream bais = new ByteArrayInputStream(getPacketData(), 2, getPacketData().length - 2);
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
	
	public final boolean getPacketSuccess()
	{
		return packetSuccess;
	}

	public static void registerSubpacket(Class<? extends ClientReadPacket0Response> packetType, int id)
	{
		packetMap.add(id, packetType);
	}
}
