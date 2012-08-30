package com.smitechow.www.chatroom;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import com.smitechow.www.chatroom.Command.CommandType;

public class Server {
	/*
	 * this is the server class
	 * */
	private int port;
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private int perCPUThreadNumber;
	private HashMap<String,OutputStream> writerHash;
	private HashMap<String,String> usernameHash;
	private List<Socket> allSocket;
	private List<String> usernames;
	private List<String> contents;
	private BufferedReader commandReader;
	private ServerThread serverThread;
	private boolean close=false;
	private int connection_index;
	private Statement sm;
	private String tablename;
	
	public Server(int port,int perCPUThreadNumber) throws Exception{
		/*
		 * the constract of the class
		 * */		
		this.port=port;
		this.perCPUThreadNumber=perCPUThreadNumber;
		
		if(this.port<1025||this.perCPUThreadNumber<1){
			System.out.println("Get a wrong port or thread number!");
			throw new Exception();
		}
		this.executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*this.perCPUThreadNumber);
		this.serverSocket=new ServerSocket(this.port);
		this.writerHash=new HashMap<String, OutputStream>();
		this.usernameHash=new HashMap<String, String>();
		this.connection_index=0;
		this.allSocket=new ArrayList<Socket>();		
		this.usernames=new ArrayList<String>();
		this.contents=new ArrayList<String>();
	}
	
	public boolean initSQLDataBase(String url,String username,String password){
		/*
		 * init the database
		 * */
		try{
			Class.forName("oracle.jdbc.OracleDriver");
			Connection con=DriverManager.getConnection(String.format("jdbc:oracle:thin:@%s","%s","%s",url,username,password));
			this.sm=con.createStatement();
		}catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	public void addMessageToDB(String username,String content){
		/*
		 * add to db
		 * */
		this.usernames.add(username);
		this.contents.add(content);
		
		if(this.usernames.size()==10)
		{
			//insert to db
			for(int index=0;index<this.usernames.size();index++){
				String sql=String.format("insert into %s (user,content) values('%s','%s')",this.tablename,this.usernames.get(index),this.contents.get(index));
				try{
					this.sm.execute(sql);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			//clear
			this.usernames.clear();
			this.contents.clear();
		}
	}
	
	public void shutdownAllSocket(){
		/*
		 * close all socket
		 * */
		for(int i=0;i<this.allSocket.size();i++)
		{
			try{
				this.allSocket.get(i).close();				
			}catch(Exception e)
			{}
		}
	}
	
	public int getConnectionIndexNumber(){
		/*
		 * this number is to mix the ip
		 * because the client from localhost
		 * all ip is 127.0.0.1
		 * */
		return this.connection_index++;
	}
	
	public void start() throws Exception{
		/*
		 * start the server and wait the client
		 * 
		 * in the doc
		 * we need give a command mode to server
		 * that means we need start the server in a thread
		 * so the main thread we can accept the input from command
		 * */
		String info=String.format("%s 系统提示：聊天室服务程序已启动...", Util.getNowTime());
		System.out.println(info);
		this.serverThread=new ServerThread(this,this.serverSocket);
		Thread thread=new Thread(this.serverThread);
		thread.start();
		
		//now we can start the command mode
		this.commandReader=Util.getCommandReader();
		while(!this.close){
			System.out.print(">>");
			
			//get the input
			info=this.commandReader.readLine();

			if(this.close)
				break;
			
			if(info.isEmpty())
			{
				System.err.println("You input a Empty command!");
				continue;
			}
			
			//create the command
			Command command=this.createCommand(info);
			if(command==null)
			{
				System.err.println("You input a wrong command!");
				continue;
			}
			
			this.doRequest(null, command, null);
		}
	}
	
	public Command createCommand(String info)
	{
		/*
		 * to create a command for server
		 * */
		Command command=new Command();
		if(info.subSequence(0, 1).equals("@"))
		{
			int index=info.indexOf("|");
			int index_message=info.indexOf(":");
			if(index==-1 || index_message==-1)
			{
				return null;
			}
			else
			{
				String type=info.substring(1,index);
				if(type.equals("YesTo"))
				{
					command.setType(CommandType.SERVER_PRI_MSG);
					String usernamesstr=info.substring(index+1,index_message);
					command.setUsernames(usernamesstr.split(","));
					command.setMessage(info.substring(index_message+1));
					return command;
				}
				else if(type.equals("NotTo"))
				{
					command.setType(CommandType.SERVER_PRI_EXCEPT_MSG);
					String usernamesstr=info.substring(index+1,index_message);
					command.setUsernames(usernamesstr.split(","));
					command.setMessage(info.substring(index_message+1));
					return command;
				}
				else
				{
					return null;
				}
			}
		}
		else
		{
			return null;
		}
	}
	
	public void doRequest(OutputStream writer,Command command,String ip){
		/*
		 * this is the request handler
		 * this func can handle all command which the command may from the client and server
		 * */
		if(command.getType()==CommandType.SERVER_PRI_MSG)
		{
			//the server send
			for(int i=0;i<command.getUsernames().length;i++)
			{
				if(this.writerHash.containsKey(command.getUsernames()[i]))
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					String message=String.format("%s 系统消息:%s", Util.getNowTime(),command.getMessage());
					response.setMessage(message);
					Util.sendMSG((OutputStream)this.writerHash.get(command.getUsernames()[i]),response.SerializeToString());
				}
				else
				{
					System.err.println("Not find the connection about User:"+command.getUsernames()[i]);
				}
			}
		}
		else if(command.getType()==CommandType.SERVER_PRI_EXCEPT_MSG)
		{
			//the server send
			String[] usernames=this.writerHash.keySet().toArray(new String[0]);
			for(int i=0;i<usernames.length;i++)
			{
				if(Util.ArrayhasKey(command.getUsernames(),usernames[i]))
					continue;
				else
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					String message=String.format("%s 系统消息:%s", Util.getNowTime(),command.getMessage());
					response.setMessage(message);
					Util.sendMSG((OutputStream)this.writerHash.get(usernames[i]),response.SerializeToString());
				}
			}
		}
		else if(command.getType()==CommandType.CLIENT_CONTROL_SERVER_EXIT)
		{
			String message=String.format("%s 系统提示:%s", Util.getNowTime(),"服务器端终止");
			System.out.println(message);
			
			String[] usernames=this.writerHash.keySet().toArray(new String[0]);
			for(int i=0;i<usernames.length;i++)
			{
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					response.setMessage(message);
					Util.sendMSG((OutputStream)this.writerHash.get(usernames[i]),response.SerializeToString());
				}
			}
			try{
				this.shutdownAllSocket();
				this.serverSocket.close();
				this.serverThread.close=true;
				this.executorService.shutdown();
				this.close=true;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(command.getType()==CommandType.CLIENT_EXIT)
		{
			String message=String.format("%s 系统提示:%s退出", Util.getNowTime(),this.usernameHash.get(ip));
			System.out.println(message);
			
			String[] usernames=this.writerHash.keySet().toArray(new String[0]);
			for(int i=0;i<usernames.length;i++)
			{
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					response.setMessage(message);
					Util.sendMSG((OutputStream)this.writerHash.get(usernames[i]),response.SerializeToString());
				}
			}
			Command response=new Command();
			response.setType(CommandType.SERVER_CONTROL_CLIENT_EXIT);
			message=String.format("%s 系统提示:你已退出", Util.getNowTime());
			response.setMessage(message);
			Util.sendMSG(writer, response.SerializeToString());
			
			this.writerHash.remove(this.usernameHash.get(ip));
			this.usernameHash.remove(ip);
		}
		else if(command.getType()==CommandType.CLIENT_GET_GRAYWORD)
		{
			Command response=new Command();
			response.setType(CommandType.SERVER_CLIENT_MSG);
			String message=String.format("%s 系统提示:%s", Util.getNowTime(),"人类言论自由受上天保护，没有过滤列表！");
			response.setMessage(message);
			Util.sendMSG(writer,response.SerializeToString());
		}
		else if(command.getType()==CommandType.CLIENT_GET_USERNAMELIST)
		{
			Command response=new Command();
			response.setType(CommandType.SERVER_CLIENT_MSG);
			String message=String.format("%s 系统提示:%s", Util.getNowTime(),Util.strJoin(this.writerHash.keySet().toArray(new String[0]),","));
			response.setMessage(message);
			Util.sendMSG(writer,response.SerializeToString());
		}
		else if(command.getType()==CommandType.CLIENT_LOGIN)
		{
			if(this.writerHash.containsKey(command.getUsername()))
			{
				Command exit_response=new Command();
				exit_response.setType(CommandType.SERVER_CONTROL_CLIENT_EXIT);
				String message=String.format("%s 系统提示:用户%s已登录", Util.getNowTime(),command.getUsername());
				exit_response.setMessage(message);
				Util.sendMSG(writer, exit_response.SerializeToString());
				
				this.writerHash.remove(this.usernameHash.get(ip));
				this.usernameHash.remove(ip);
				
				Command response=new Command();
				response.setType(CommandType.SERVER_CLIENT_MSG);
				message=String.format("%s 系统提示:你所使用的用户名在%s处被重复登录", Util.getNowTime(),ip);
				response.setMessage(message);
				Util.sendMSG((OutputStream)this.writerHash.get(command.getUsername()), response.SerializeToString());
			}
			else
			{
				this.writerHash.put( command.getUsername(),writer);
				this.usernameHash.put(ip, command.getUsername());
				String info=String.format("%s 系统提示:%s IP[%s]连接成功", Util.getNowTime(),command.getUsername(),ip);
				System.out.println(info);
				
				String[] usernames=this.writerHash.keySet().toArray(new String[0]);
				for(int i=0;i<usernames.length;i++)
				{
					{
						Command response=new Command();
						response.setType(CommandType.SERVER_CLIENT_MSG);
						response.setMessage(info);
						Util.sendMSG((OutputStream)this.writerHash.get(usernames[i]),response.SerializeToString());
					}
				}
			}
		}
		else if(command.getType()==CommandType.CLIENT_PRI_MSG)
		{
			for(int i=0;i<command.getUsernames().length;i++)
			{
				if(this.writerHash.containsKey(command.getUsernames()[i]))
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					String message=String.format("%s %s:%s", Util.getNowTime(),this.usernameHash.get(ip),command.getMessage());
					response.setMessage(message);
					Util.sendMSG((OutputStream)this.writerHash.get(command.getUsernames()[i]),response.SerializeToString());
				}
				else
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					String message=String.format("%s 系统消息:%s", Util.getNowTime(),"Not find the User:"+command.getUsernames()[i]);
					response.setMessage(message);
					Util.sendMSG(writer,response.SerializeToString());
				}
			}
		}
		else if(command.getType()==CommandType.CLIENT_PUB_MSG)
		{
			String info=String.format("%s %s:%s", Util.getNowTime(),this.usernameHash.get(ip),command.getMessage());
			this.addMessageToDB(this.usernameHash.get(ip),command.getMessage());
			
			String[] usernames=this.writerHash.keySet().toArray(new String[0]);
			for(int i=0;i<usernames.length;i++)
			{
				{
					Command response=new Command();
					response.setType(CommandType.SERVER_CLIENT_MSG);
					response.setMessage(info);
					Util.sendMSG((OutputStream)this.writerHash.get(usernames[i]),response.SerializeToString());
				}
			}
		}
		else
		{
			System.err.println("Server get a wrong type command from ip:"+ip);
		}
	}
	
	public void addConnection(Socket socket)
	{
		/*
		 * this func is called by server thread
		 * */
		try{
			this.allSocket.add(socket);
			this.executorService.execute(new ProtocolThread(this,socket));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	class ServerThread implements Runnable{
		private ServerSocket serverSocket;
		private Server server;
		public boolean close=false;
		
		public ServerThread(Server server,ServerSocket serverSocket)
		{
			/*
			 * the constrac func
			 * */
			this.serverSocket=serverSocket;
			this.server=server;
		}
		
		public void run(){
			/*
			 * the run func
			 * */
			try{
				while(!this.close){
					Socket socket=null;
					socket=this.serverSocket.accept();
					this.server.addConnection(socket);
				}
			}catch(Exception e){}
		}
	}
}
