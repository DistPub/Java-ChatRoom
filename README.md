Java-ChatRoom
=============

This is a help for my classmate J.F. Cao, also i think this code can help others come to understand multi-thread program and the socket program!

How to use
=============

You can add the src to a Java project

How to Run
=============

run the MainClass.java

Ok,you can see the usage for more help.

Use different args to lanch different Application Model

Also the ags you can find in the usage

The Server Model
==============

Support two command in this modl.

1.@YesTo|sb.name,sb.name:the message

this command is server send message to sb you specified

2.@NotTo|sb.name,sb.name:the message

this command is server send message to all user except the sb you specified

The Client Model
===============

Support 

1.@showAllUser

the server will return all username

2.@showGrayWord

the server will return gray word

3.@exit

the server will close the socket to which client send this command

4.@sb.name,sb.name|the message

client send the message to sb the client specified

5.end

server will close all socket and close server self

6.the message

client send a pub message, the server will send to all client