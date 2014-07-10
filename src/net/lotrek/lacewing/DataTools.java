package net.lotrek.lacewing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataTools
{
	public static byte[] readDataBlock(InputStream is, int length) throws IOException
	{
		byte[] b = new byte[length];
		while(length > 0)
			length -= is.read(b, b.length - length, length);
		return b;
	}
	
	public static int readInversedShort(InputStream is) throws IOException
	{
		byte[] data = readDataBlock(is, 2);
		return ((((int)data[1] & 0xff) << 8) | ((int)data[0] & 0xff)) & 0xffff;
	}
	
	public static void writeInversedShort(OutputStream os, int data) throws IOException
	{
		data &= 0xffff;
		byte[] bata = new byte[]{(byte) (data & 0xff), (byte) ((data >> 8) & 0xff)};
		os.write(bata);
	}
	
	public static long[] readLengthHeader(DataInputStream dis) throws IOException
	{
		long length = (long)dis.readByte() & 0xff, readBytes = 1;
		if(length == 254)
		{
			length = readInversedShort(dis);
			readBytes += 2;
		}else if (length == 255)
		{
			length = (long)dis.readInt() & -1;
			readBytes += 4;
		}
		
		return new long[]{length, readBytes};
	}
	
	public static void writeLengthHeader(DataOutputStream dos, long length) throws IOException, LacewingException
	{
		if(length >= 254 &&  length < 65535)
		{
			dos.write(254);
			dos.writeShort((int)length & 0xffff);
		}else if (length >= 65535 && length < 4294967295l)
		{
			dos.write(255);
			dos.writeInt((int) length);
		}else if(length < 254)
			dos.write((int)length & 0xff);
		else
			throw new LacewingException(length + " exceeds the maximum packet length");
	}
}
