package net.lotrek.lacewing;

public class LacewingException extends Exception
{
	private static final long serialVersionUID = 1L;
	private String message;
	
	public LacewingException(String msg)
	{
		message = msg;
	}
	
	public String getMessage()
	{
		return message;
	}
}
