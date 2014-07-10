package net.lotrek.lacewing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodObjectPair
{
	private Method toInvoke;
	private Object object;
	
	public MethodObjectPair(Method toInvoke, Object object)
	{
		this.toInvoke = toInvoke;
		this.object = object;
	}
	
	public Object invoke(Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		return toInvoke.invoke(object, args);
	}
}
