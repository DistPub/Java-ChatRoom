package com.smitechow.www.chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	/*
	 * this is a tool class
	 * */
	public static String getNowTime(){
		Date currentTime = new Date(); 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(currentTime);
	}
	public static OutputStream getWriter(Socket socket) throws IOException{
        OutputStream socketOut=socket.getOutputStream();
        return socketOut;
    }
    public static BufferedReader getReader(Socket socket) throws IOException{
        InputStream socketIn=socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }
    public static BufferedReader getCommandReader() throws IOException{
        return new BufferedReader(new InputStreamReader(System.in));
    }
    public static String strJoin(String[] strs,String splitchar)
    {
    	String temp=strs[0];
    	for(int i=1;i<strs.length;i++)
    	{
    		temp+=splitchar;
    		temp+=strs[i];
    	}
    	return temp;
    }
	public static void sendMSG(OutputStream writer,String message){
		String header=String.valueOf(message.length())+"\n";
		try{
			writer.write(header.getBytes());
			writer.write(message.getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static boolean ArrayhasKey(String[] strs,String s)
	{
		for(int i=0;i<strs.length;i++)
		{
			if(strs[i].equals(s))
				return true;
		}
		return false;
	}

}
