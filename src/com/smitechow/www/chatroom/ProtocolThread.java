package com.smitechow.www.chatroom;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;

public class ProtocolThread implements Runnable{
	/*
	 * this is the server thread to handle the every socket message
	 * */
	private Socket socket;
	private Server server;
	private OutputStream writer;
	private BufferedReader reader;
	private String buffer;
	private String ip;
	private int mix_connection_number;
	
	public ProtocolThread(Server server,Socket socket) throws Exception{
		/*
		 * the constract
		 * */
		this.server=server;
		this.socket=socket;
		if(this.server==null || this.socket==null){
			System.out.println("Error, when constract the ProtocolThread!");
			throw new Exception();
		}
		this.writer=Util.getWriter(this.socket);
		this.reader=Util.getReader(this.socket);
		this.buffer="";
		InetAddress addr = this.socket.getInetAddress();
		this.ip=addr.getHostAddress();
		this.mix_connection_number=this.server.getConnectionIndexNumber();
	}
	
	public void run(){
		/*
		 * when the server call start of the class
		 * in medently called this fuc
		 * */
		try{
			CharBuffer charBuffer=CharBuffer.allocate(32);
			while(true){
				int n=this.reader.read(charBuffer);
				
				if(n<1)
					continue;
				
				char[] realCharBuffer=new char[n];
				for(int i=0;i<n;i++)
					realCharBuffer[i]=charBuffer.get(i);
				this.buffer+=String.valueOf(realCharBuffer);
				charBuffer.clear();
				
				int index=this.buffer.indexOf("\n");
				if(index==-1)
					continue;
				
				String lenstr=this.buffer.substring(0, index);
				int length=Integer.parseInt(lenstr);
				
				if(this.buffer.length()<(lenstr.length()+1+length))
					continue;
				
				//ok get a complete msg
				String message=this.buffer.substring(index+1,index+1+length);
				Command command=new Command();
				if(command.parseFromString(message)==false)
					System.err.println("Server get a wrong message!");
				else
					this.server.doRequest(this.writer,command,String.format("%s:%d", this.ip,this.mix_connection_number));
				
				//update the buffer
				this.buffer=this.buffer.substring(this.buffer.indexOf("\n")+1+length);
		}
		}catch(Exception e){
			//catch a exception, print the msg and close the socket
			e.printStackTrace();
			try{
				this.socket.close();
			}catch(Exception ce){
				ce.printStackTrace();
			}
		}
		
	}

}
