package com.smitechow.www.chatroom;

public class Command {
	/*
	 * this class is the command class
	 * this is the standard protocol of the server and client
	 * */
	enum CommandType{
		CLIENT_LOGIN,
		CLIENT_EXIT,
		CLIENT_PUB_MSG,
		CLIENT_PRI_MSG,
		CLIENT_GET_USERNAMELIST,
		CLIENT_GET_GRAYWORD,
		CLIENT_CONTROL_SERVER_EXIT,
		
		SERVER_PRI_MSG,
		SERVER_PRI_EXCEPT_MSG,
		SERVER_CONTROL_CLIENT_EXIT,
		SERVER_CLIENT_MSG
	}
	private CommandType type;
	private String username;
	private String message;
	private String[] usernames;
	
	public CommandType getType() {
		return type;
	}
	public void setType(CommandType type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String[] getUsernames() {
		return usernames;
	}
	public void setUsernames(String[] usernames) {
		this.usernames = usernames;
	}
	public String SerializeToString(){
		String command="";
		if(this.type==CommandType.CLIENT_LOGIN)
		{
			command=String.format(".login %s", this.username);
		}
		else if(this.type==CommandType.CLIENT_PUB_MSG)
		{
			command=String.format(".pub %s",this.message);
		}
		else if(this.type==CommandType.CLIENT_PRI_MSG)
		{
			command=String.format(".pri %s|%s", Util.strJoin(this.usernames, ","),this.message);
		}
		else if(this.type==CommandType.SERVER_PRI_EXCEPT_MSG)
		{
			command=String.format(".server_pri_except %s|%s", Util.strJoin(this.usernames, ","),this.message);
		}
		else if(this.type==CommandType.SERVER_PRI_MSG)
		{
			command=String.format(".server_pri %s|%s", Util.strJoin(this.usernames, ","),this.message);
		}
		else if(this.type==CommandType.CLIENT_GET_USERNAMELIST)
		{
			command=".getusernamelist";
		}
		else if(this.type==CommandType.CLIENT_GET_GRAYWORD)
		{
			command=".getgrayword";
		}
		else if(this.type==CommandType.CLIENT_EXIT)
		{
			command=".exit";
		}
		else if(this.type==CommandType.SERVER_CLIENT_MSG)
		{
			command=String.format(".showmsg %s", this.message);
		}
		else if(this.type==CommandType.SERVER_CONTROL_CLIENT_EXIT)
		{
			command=String.format(".exitsignaltoclient %s",this.message);
		}
		else if(this.type==CommandType.CLIENT_CONTROL_SERVER_EXIT)
		{
			command=".exitsignaltoserver";
		}
		return command;
	}
	public boolean parseFromString(String str){
		if(str.substring(0,1).equals("."))
		{
			int index=str.indexOf(" ");
			String type="";
			String body="";
			if(index!=-1)
			{
				type=str.substring(0,index);
				body=str.substring(index+1);
			}
			else
				type=str;
			if(type.equals(".login"))
			{
				this.type=CommandType.CLIENT_LOGIN;
				this.username=body;
			}
			else if(type.equals(".pub"))
			{
				this.type=CommandType.CLIENT_PUB_MSG;
				this.message=body;
			}
			else if(type.equals(".pri"))
			{
				this.type=CommandType.CLIENT_PRI_MSG;
				String usernamesstr=body.substring(0,body.indexOf("|"));
				this.usernames=usernamesstr.split(",");
				this.message=body.substring(body.indexOf("|")+1);
			}
			else if(type.equals(".server_pri_except"))
			{
				this.type=CommandType.SERVER_PRI_EXCEPT_MSG;
				String usernamesstr=body.substring(0,body.indexOf("|"));
				this.usernames=usernamesstr.split(",");
				this.message=body.substring(body.indexOf("|")+1);
			}
			else if(type.equals(".server_pri"))
			{
				this.type=CommandType.SERVER_PRI_MSG;
				String usernamesstr=body.substring(0,body.indexOf("|"));
				this.usernames=usernamesstr.split(",");
				this.message=body.substring(body.indexOf("|")+1);
			}
			else if(type.equals(".getusernamelist"))
			{
				this.type=CommandType.CLIENT_GET_USERNAMELIST;
			}
			else if(type.equals(".getgrayword"))
			{
				this.type=CommandType.CLIENT_GET_GRAYWORD;
			}
			else if(type.equals(".exit"))
			{
				this.type=CommandType.CLIENT_EXIT;
			}
			else if(type.equals(".exitsignaltoclient"))
			{
				this.type=CommandType.SERVER_CONTROL_CLIENT_EXIT;
				this.message=body;
			}
			else if(type.equals(".showmsg"))
			{
				this.type=CommandType.SERVER_CLIENT_MSG;
				this.message=body;
			}
			else if(type.equals(".exitsignaltoserver"))
			{
				this.type=CommandType.CLIENT_CONTROL_SERVER_EXIT;
			}
			else
			{
				//get wrong type
				return false;
			}
			return true;
		}
		else
		{
			//not a command
			return false;
		}
	}
}
