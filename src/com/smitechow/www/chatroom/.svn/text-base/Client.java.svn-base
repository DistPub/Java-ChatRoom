package com.smitechow.www.chatroom;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.CharBuffer;

import com.smitechow.www.chatroom.Command.CommandType;

public class Client {
	/*
	 * this is the client class
	 * a client to support the conmunication to server
	 * */
	private String ip;
	private int port;
	private Socket socket;
	private OutputStream writer;
	private BufferedReader reader;
	private BufferedReader commandReader;
	private String username;
	private boolean close=false;
	private ClientProtocolThread thread;
	
	public Client(String ip,int port) throws Exception{
		/*
		 * the constract fuc.
		 * you must input the right ip and port
		 * so client can use the ip and port to connected to server
		 * */
		this.ip=ip;
		this.port=port;
		if(this.ip=="" || this.port<1025){
			System.err.println("Constract the Client ERROR, wrong ip or port!");
			throw new Exception();
		}
	}
	
	public void connection() throws Exception{
		/*
		 * after you constract the client
		 * you can call this func
		 * after connected to server
		 * it will goto the command mode until you exit
		 * */
		
		//connect to server
		this.socket=new Socket(this.ip,this.port);
		this.writer=Util.getWriter(this.socket);
		this.reader=Util.getReader(this.socket);
		
		this.commandReader=Util.getCommandReader();
		//get the username
		while(true){
			
			System.out.println("请输入用户名:");
			this.username=this.commandReader.readLine();
			if(this.username.isEmpty())
			{
				System.err.println("You enter a empty username!");
			}
			else
				break;
		}		
		
		//connected start a thread to handle the message from the server
		
		this.thread=new ClientProtocolThread(this,this.socket,this.reader);
		Thread clientthread=new Thread(this.thread);
		clientthread.start();
		
		//start login to server
		this.start();
	}
	
	public void start() throws Exception{
		/*
		 * we need first to login to server
		 * and then we goto the command mode
		 * */
		Command command=new Command();
		command.setType(CommandType.CLIENT_LOGIN);
		command.setUsername(this.username);
		Util.sendMSG(this.writer, command.SerializeToString());
		
		while(!this.close){
			System.out.print(">>");
			
			//get the input
			String info=this.commandReader.readLine();
			
			if(this.close)
				break;
			
			if(info.isEmpty())
			{
				System.err.println("You input a Empty command!");
				continue;
			}
			
			//create the command
			command=this.createCommand(info);
			if(command==null)
			{
				System.err.println("You input a wrong command!");
				continue;
			}
			
			//send command
			Util.sendMSG(this.writer, command.SerializeToString());
		}
	}
	
	public Command createCommand(String info){
		/*
		 * this func use to create the command
		 * you konw, the doc give the format to send a message from the client
		 * and the format not very well
		 * so we need translate the message to a command for client send it to server
		 * */
		Command command=new Command();
		if(info.substring(0,1).equals("@"))
		{
			//maybe a pri msg or get msg
			int index=info.indexOf("|");
			if(index==-1)
			{
				//the get and client exit
				String type=info.substring(1);
				if(type.equals("showAllUser"))
				{
					command.setType(CommandType.CLIENT_GET_USERNAMELIST);
				}
				else if(type.equals("showGrayWord"))
				{
					command.setType(CommandType.CLIENT_GET_GRAYWORD);
				}
				else if(type.equals("exit"))
				{
					command.setType(CommandType.CLIENT_EXIT);
				}
				else
					command=null;
			}
			else
			{
				//the pri msg
				command.setType(CommandType.CLIENT_PRI_MSG);
				String usernamesstr=info.substring(1,index);
				command.setUsernames(usernamesstr.split(","));
				command.setMessage(info.substring(index+1));
			}
		}
		else if(info.length()==3 && info.equals("end"))
		{
			//the client want server exit
			command.setType(CommandType.CLIENT_CONTROL_SERVER_EXIT);
		}
		else
		{
			//this is a pub msg
			command.setType(CommandType.CLIENT_PUB_MSG);
			command.setMessage(info);
		}
		return command;
	}
	
    public class ClientProtocolThread implements Runnable{
    	/*
    	 * ok, as you see
    	 * this is a internal class
    	 * this class is the protocol to handle the resposne
    	 * */
    	private BufferedReader reader;
    	private String buffer;
    	private Socket socket;
    	public boolean close=false;
    	private Client client;
    	
    	public ClientProtocolThread(Client client,Socket socket,BufferedReader reader) throws Exception{
    		/*
    		 * the constract of the client protocol
    		 * */
    		this.client=client;
    		this.socket=socket;
    		this.reader=reader;
    		this.buffer="";
    		if(this.socket==null || this.reader==null)
    		{
    			System.err.println("Wrong socket or reader!");
    			throw new Exception();
    		}
    	}
    	
    	public void doResponse(Command command)
    	{
    		/*
    		 * after get a command we handle it there
    		 * */
    		if(command.getType()==CommandType.SERVER_CLIENT_MSG)
    		{
    			System.out.println(command.getMessage());
    		}
    		else if(command.getType()==CommandType.SERVER_CONTROL_CLIENT_EXIT)
    		{
    			System.out.println(command.getMessage());
    			this.client.close=true;
    			this.close=true;
    			try{
    				this.socket.close();
    			}catch(Exception e){}
    		}
    		else
    		{
    			System.err.println("Get a wrong message from server!");
    		}
    		
    	}
    	public void run(){
    		/*
    		 * after the thread called start
    		 * this func will be called
    		 * */
    		try{
    			CharBuffer charBuffer=CharBuffer.allocate(32);
    			while(!this.close){
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
    					System.err.println("Client get a wrong message!");
    				else
    					this.doResponse(command);
    				
    				//update the buffer
    				this.buffer=this.buffer.substring(this.buffer.indexOf("\n")+1+length);
    			}
			
    		}catch(Exception e){
    			e.printStackTrace();
    			try{
    				this.socket.close();
    			}catch(Exception ce){
    				ce.printStackTrace();
    			}
    		}
    	}
    }
}