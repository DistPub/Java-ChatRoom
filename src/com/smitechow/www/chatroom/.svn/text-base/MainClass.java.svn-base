package com.smitechow.www.chatroom;

public class MainClass {
	public static void main(String[] args){
		if(args.length==0)
		{
			System.err.println("Empty args!");
			useage();
		}
		else{
			Server server=null;
			Client client=null;
			boolean startServer=false;
			boolean startClient=false;
			String ip="";
			int port=-1;
			int perCPUThreadNumber=-1;
			
			for(int i=0;i<args.length;i++)
			{
				if(args[i].equals("-s"))
					startServer=true;
				else if(args[i].equals("-i"))
				{
					ip=args[i+1];
					i++;
				}
				else if(args[i].equals("-p"))
				{
					port=Integer.parseInt(args[i+1]);
					i++;
				}
				else if(args[i].equals("-c"))
					startClient=true;
				else if(args[i].equals("-n"))
				{
					perCPUThreadNumber=Integer.parseInt(args[i+1]);
					i++;
				}
				else{
					System.err.println("Wrong args");
					useage();
					return;
				}
			}
			
			if(startServer&&startClient){
				System.err.println("Can't run server and client at the same time!");
				useage();
				return;
			}
			else if(startServer||startClient){
				try{
				if(startServer){
					if(port==-1 || perCPUThreadNumber==-1)
					{
						System.err.println("Wrong port or threadnumber!");
						useage();
						return;
					}
					else{
						server=new Server(port,perCPUThreadNumber);
						//set the database
						if(!server.initSQLDataBase("192.168.0.36:1521:Test", "root", ""))
						{
							System.err.print("Init DB ERROR!");
							return;
						}
						server.start();
					}
				}
				else{
					if(ip=="" || port==-1)
					{
						System.err.println("Wrong ip or port!");
						useage();
						return;
					}
					else{
						client=new Client(ip,port);
						client.connection();
					}
				}
				}catch(Exception e){
					System.err.println("Get a Excption:");
					e.printStackTrace();
					return;
				}
			}
			else{
				System.err.println("Please choose a mode!");
				useage();
				return;
			}
		}
	}
	public static void useage(){
		System.out.println("Useage:\n" +
						   "You can start a server or a client!\n" +
						   "1.to Start a Server, please use below args to run the application\n" +
						   "	-s -p port -n perCPUThreadNumber\n" +
						   "-s means start a server\n" +
						   "-i set the server ip\n" +
						   "-n set the server message handle thread number\n" +
						   "2.to Start a Client, please use below args to run the application\n" +
						   "	-c -i ip -p port\n" +
						   "-c means start a client\n" +
						   "-i set the server ip\n" +
						   "-p set teh server port\n" +
						   "\n" +
						   "For example:" +
						   "-s -p 1101 -n 10\n" +
						   "-c -i localhost -p 1101\n");
	}
}
