package net.lotrek.lacewing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodObjectPair
{
	private Method rawInvoke;
	private String toInvoke;
	private Object object;
	private Class<?> parent;
	
	public <T> MethodObjectPair(String toInvoke, Class<T> parent, T object)
	{
		this.toInvoke = toInvoke;
		this.object = object;
		this.parent = parent;
	}
	
	public MethodObjectPair(Method toInvoke, Object object)
	{
		this.object = object;
		this.rawInvoke = toInvoke;
	}
	
	public Object invoke(Object...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		try {
			Method meth = rawInvoke == null ? parent.getDeclaredMethod(toInvoke, args[0].getClass()) : rawInvoke;
			meth.setAccessible(true);
			return meth.invoke(object, args);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
