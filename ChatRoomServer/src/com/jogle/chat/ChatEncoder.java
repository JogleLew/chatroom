package com.jogle.chat;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import sun.misc.*;

public class ChatEncoder {
	private static int KEY = 5;
	
	public static String encode(String input) {
		String s = encodeBase64(input);
		String news = "";
		for (int i = 0; i < s.length(); i++)
			news += (char) (s.charAt(i) + i % KEY);
		return news;
	}
	
	public static String decode(String input) {
		String s = "";
		for (int i = 0; i < input.length(); i++)
			s += (char) (input.charAt(i) - i % KEY);
		return decodeBase64(s);
	}
	
	public static String encodeBase64(String input){
		try {
			byte[] inputb = input.getBytes();
	        Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
	        Method mainMethod = clazz.getMethod("encode", byte[].class);  
	        mainMethod.setAccessible(true);  
	        Object retObj = mainMethod.invoke(null, new Object[]{inputb});
	        return (String)retObj;
		} catch(Exception e) {
			return "";
		}
    }
    
    public static String decodeBase64(String input) {  
    	try {
	        Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
	        Method mainMethod = clazz.getMethod("decode", String.class);  
	        mainMethod.setAccessible(true);  
	        Object retObj = mainMethod.invoke(null, input);
	        return new String((byte[])retObj);
    	} catch (Exception e) {
	    	return "";
	    }
    } 
}