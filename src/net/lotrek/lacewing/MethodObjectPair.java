package net.lotrek.lacewing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodObjectPair
{
	private String toInvoke;
	private Object object;
	private Class<?> parent;
	
	public <T> MethodObjectPair(String toInvoke, Class<T> parent, T object)
	{
		this.toInvoke = toInvoke;
		this.object = object;
		this.parent = parent;
	}
	
	public Object invoke(Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		try {
			Method meth = parent.getDeclaredMethod(toInvoke, args[0].getClass());
			meth.setAccessible(true);
			return meth.invoke(object, args);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
