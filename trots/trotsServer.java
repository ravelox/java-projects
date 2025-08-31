/*
$Log$
Revision 1.1  2003/10/10 10:16:51  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.31  99/07/23  15:35:08  15:35:08  dkelly (Dave Kelly)
Changed date formatting to be British Summer Time

Revision 1.30  99/07/20  16:57:48  16:57:48  dkelly (Dave Kelly)
Made sure that user list is maintained in ID order

Revision 1.29  99/07/20  14:18:52  14:18:52  dkelly (Dave Kelly)
Changed generation of IDs to be sequential instead of random

Revision 1.28  99/07/20  11:28:13  11:28:13  dkelly (Dave Kelly)
Added SOCKET_TIMEOUT

Revision 1.27  99/07/19  10:47:49  10:47:49  dkelly (Dave Kelly)
Stopped SERVER_VERSION being logged to debug file all the time

Revision 1.26  99/07/19  10:22:51  10:22:51  dkelly (Dave Kelly)
Fixed debug statement

Revision 1.25  99/07/19  10:21:44  10:21:44  dkelly (Dave Kelly)
Added code to retrieve client revision

Revision 1.24  99/07/16  14:11:30  14:11:30  dkelly (Dave Kelly)
Fixed compilation problem after last amendment

Revision 1.23  99/07/16  12:58:30  12:58:30  dkelly (Dave Kelly)
Added ACK response to CLEAN_USERS

Revision 1.22  99/07/15  16:36:44  16:36:44  dkelly (Dave Kelly)
Removed Call Receipt admin commands

Revision 1.21  99/07/14  11:35:10  11:35:10  dkelly (Dave Kelly)
Cleaned up debug statements

Revision 1.20  99/07/14  09:19:53  09:19:53  dkelly (Dave Kelly)
Added call to remove statistics when a queue is deleted

Revision 1.19  99/07/13  14:59:40  14:59:40  dkelly (Dave Kelly)
Removed deregistration of call receipt users. This now happens in the REMOVE_CALL_RECEIPT functionality

Revision 1.18  99/07/13  14:29:51  14:29:51  dkelly (Dave Kelly)
Changed debugging statement

Revision 1.17  99/07/13  13:27:47  13:27:47  dkelly (Dave Kelly)
Removed need for additional data to be sent for call receipt deregister

Revision 1.16  99/07/13  12:53:08  12:53:08  dkelly (Dave Kelly)
Multiple changes to add extra admin functionality

Revision 1.15  99/07/07  11:39:47  11:39:47  dkelly (Dave Kelly)
Removed logging of status to debug file.
Changed reference from SERVER_STATUS to STATUS_HTML

Revision 1.14  99/07/07  11:17:29  11:17:29  dkelly (Dave Kelly)
Added processing to respond to HEARTBEAT request

Revision 1.13  99/07/07  09:19:09  09:19:09  dkelly (Dave Kelly)
Changed shutdown debug logging to always log when shutdown received

Revision 1.12  99/07/05  15:52:12  15:52:12  dkelly (Dave Kelly)
Made queue manipulation case insensitive

Revision 1.11  99/07/02  14:02:18  14:02:18  dkelly (Dave Kelly)
Java 1.2

Changed Properties.save() to Properties.store()

Revision 1.10  99/07/02  10:12:45  10:12:45  dkelly (Dave Kelly)
General code cleanup
Removed privates and statics

Revision 1.9  99/04/30  09:12:15  09:12:15  dkelly (Dave Kelly)
Don't know !

Revision 1.8  99/04/29  14:00:54  14:00:54  dkelly (Dave Kelly)
Corrected typo in debugging statement
,.

Revision 1.7  99/04/29  13:50:59  13:50:59  dkelly (Dave Kelly)
Added code for CLEAN_USERS and HEARTBEAT

Revision 1.6  99/04/22  16:30:13  16:30:13  dkelly (Dave Kelly)
Dunno

Revision 1.5  99/04/21  15:45:35  15:45:35  dkelly (Dave Kelly)
Modified isUserQueue to use StringTokenizer

Revision 1.4  99/04/19  10:31:44  10:31:44  dkelly (Dave Kelly)
Forced

Revision 1.3  99/03/05  14:15:06  14:15:06  dkelly (Dave Kelly)
Added improvements to the statistics

Revision 1.2  99/02/26  12:24:57  12:24:57  dkelly (Dave Kelly)
Added RCS statements

*/

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import java.text.*;

/*---------------------------*/
/* Class for TROTS user item */
/*---------------------------*/
class trotsUser
{
	public int id;
	public String userName;
	public String phoneNo;
	public String queueList;
	public boolean available;
	DataOutputStream dataOut;
	DataInputStream dataIn;
	BufferedReader textIn;
	public long createTime;
	public String hostname;
	public String version;

/*-------------*/
/* Constructor */
/*-------------*/
	public trotsUser()
	{
		id = 0;
		available = true;
		createTime = System.currentTimeMillis() / 1000;
	}

/*-------------------------------------------------------------------*/
/* Indicate if a supplied queue is one that the user is listening to */
/*-------------------------------------------------------------------*/
	public boolean isUserQueue(String queueName)
	{
		String temp = queueList;
		String currentQueue;
		StringTokenizer st = new StringTokenizer(temp, ",");

		while(st.hasMoreTokens())
		{
			currentQueue = st.nextToken();
			if(currentQueue.equalsIgnoreCase(queueName))
			{
				return true;
			}
		}
		return false;
	}
}

/*------------------------------*/
/* Class for TROTS message item */
/*------------------------------*/
class trotsMsgItem
{
	public int id;
	public String queue;
	int	sender;
	public String msgText;
	public int flag;
	public Vector recipients;
	public long createTime;

/*-------------*/
/* Constructor */
/*-------------*/
	public trotsMsgItem()
	{
		id = 0;
		recipients = new Vector();
		createTime = System.currentTimeMillis() / 1000;
	}

/*-------------------------------------------------------*/
/* Add a user to the list of recipients for this message */
/*-------------------------------------------------------*/
	public void addUser(trotsUser user)
	{
		recipients.addElement(user);
	}
}

public class trotsServer
{
	Vector					users, msgs, currentQueues;
	Random					idGenerator = new Random();
	ServerSocket			server;
	Socket					client;
	DataInputStream		dataIn;
	DataOutputStream		dataOut;
	BufferedReader			textIn;
	static Properties		props = new Properties();
	AllStats		serverStats;
	
	static debug DBG;

	long startTime;

/*--------------------------------------------------------------*/
/* Utility function to return an integer value from a string or */
/* the supplied default                                         */
/*--------------------------------------------------------------*/
	static int makeInt(String value, int defaultValue)
	{
		int returnValue = defaultValue;

		DBG.trace(debug.DEBUG, "-->makeInt");
		try
		{
			returnValue = new Integer(value).intValue();
		}
		catch(NumberFormatException exNumFmt) {}

		DBG.trace(debug.DEBUG, "<--makeInt");
		return returnValue;
	}

/*---------------------------*/
/* Find the specific user ID */
/*---------------------------*/
	trotsUser findUser(int id)
	{
		trotsUser tu;
		DBG.trace(debug.DEBUG, new StringBuffer("-->findUser (").append(id).append(")").toString());
		for(int i=0; i < users.size(); i++)
		{
			tu = (trotsUser)users.elementAt(i);
			if(tu.id == id)
			{
				DBG.trace(debug.DEBUG,"<--findUser(true)");
				return tu;
			}
		}
		DBG.trace(debug.DEBUG,"<--findUser(null)");
		return null;
	}

/*------------------------------*/
/* Find the specific message ID */
/*------------------------------*/
	trotsMsgItem findMsg(int id)
	{
		trotsMsgItem tm;
		DBG.trace(debug.DEBUG, new StringBuffer("-->findMsg (").append(id).append(")").toString());
		for(int i=0; i < msgs.size(); i++)
		{
			tm = (trotsMsgItem)msgs.elementAt(i);
			if(tm.id == id)
			{
				DBG.trace(debug.DEBUG, "<--findMsg (true)");
				return tm;
			}
		}
		DBG.trace(debug.DEBUG, "<--findMsg (null)");
		return null;
	}

/*-----------------------------------------------*/
/* Check to see if the message ID already exists */
/*-----------------------------------------------*/
	boolean isDuplicateMsg(int id)
	{
		trotsMsgItem tm;
		DBG.trace(debug.DEBUG, new StringBuffer("-->isDuplicateMsg (").append(id).append(")").toString());
		if(id==0) return true;
		for(int i=0; i < msgs.size(); i++)
		{
			tm = (trotsMsgItem)msgs.elementAt(i);
			if(tm.id == id)
			{
				DBG.trace(debug.DEBUG, "<--isDuplicateMsg(true)");
				return true;
			}
		}
		DBG.trace(debug.DEBUG, "<--isDuplicateMsg(false)");
		return false;
	}

/*--------------------------------------------*/
/* Check to see if the user ID already exists */
/*--------------------------------------------*/
	boolean isDuplicateUser(int id)
	{
		trotsUser tu;
		DBG.trace(debug.DEBUG, new StringBuffer("-->isDuplicateUser (").append(id).append(")").toString());
		if(id==0) return true;
		for(int i=0; i < users.size(); i++)
		{
			tu = (trotsUser)users.elementAt(i);
			if(tu.id == id)
			{
				DBG.trace(debug.DEBUG, "<--isDuplicateUser (true)");
				return true;
			}
		}
		DBG.trace(debug.DEBUG, "<--isDuplicateUser (false)");
		return false;
	}

/*----------------------------------------------------------*/
/* Convert the vector of queues into a comma separated list */
/*----------------------------------------------------------*/
	String queue_vector_to_list()
	{
		DBG.trace(debug.DEBUG,"-->queue_vector_to_list");
		String output = new String("");

		for(int i=0; i < currentQueues.size(); i++)
		{
			if(i > 0) output = output.concat(",");
			output = output.concat((String)currentQueues.elementAt(i));
		}
		DBG.trace(debug.DEBUG, "<--queue_vector_to_list");
		return output;
	}

/*----------------------------------------------------------*/
/* Convert the comma separated list of queues into a vector */
/*----------------------------------------------------------*/
	void queue_list_to_vector()
	{
		StringTokenizer st = new StringTokenizer(props.getProperty("queues"),",");
		String this_queue;

		DBG.trace(debug.DEBUG, "-->queue_list_to_vector");

		while(st.hasMoreTokens())
		{
			this_queue = st.nextToken();
			if(! currentQueues.contains(this_queue) )
			{
				currentQueues.addElement(this_queue.toUpperCase());
				serverStats.add(this_queue.toUpperCase());
			}
		}
		DBG.trace(debug.DEBUG, "<--queue_list_to_vector");
	}

/*---------------------------------------------------------------------*/
/* Update the recipients of a message to say who got assigned the call */
/*---------------------------------------------------------------------*/
	void tellRecipients(trotsMsgItem tm, trotsUser tu)
	{
		int numRecipients;
		trotsUser recip;

		DBG.trace(debug.DEBUG, "--> tellRecipients");

		numRecipients = tm.recipients.size();

		DBG.trace(debug.MINOR,new StringBuffer("[").append(numRecipients).append("] recipients to update").toString());

		try
		{
			for(int i=0; i<numRecipients; i++)
			{
				recip = (trotsUser)tm.recipients.elementAt(i);

				DBG.trace(debug.MINOR,"Sending TROTS_ASSIGN_SEND");
				DBG.trace(debug.DEBUG,"To " + recip.userName);
				recip.dataOut.writeInt(Constants.TROTS_ASSIGN_SEND);
				recip.dataOut.writeInt(tm.id);

				if(recip.id == tu.id)
				{
					recip.dataOut.writeBytes("Call is assigned to you\n");
				}
				else
				{
					recip.dataOut.writeBytes(new StringBuffer("Call is assigned to ").append(tu.userName).append("\n").toString());
				}
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR,"Unable to update client ("+exIO.getMessage()+")");
		}
		DBG.trace(debug.DEBUG, "<--tellRecipients");
	}

/*-----------------------------------------------------*/
/* Remove the client from the list of registered users */
/*-----------------------------------------------------*/
	void deregisterClient()
	{
		int userID;
		trotsUser tu;
		DBG.trace(debug.DEBUG, "--> deregisterClient");

		try
		{
			DBG.trace(debug.MINOR,"Waiting for User ID");
			userID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("User ID [").append(userID).append("]").toString());
			DBG.trace(debug.MINOR,"Locating user ID");
			tu = findUser(userID);

			if(tu==null)
			{
				DBG.trace(debug.MAJOR,"Unable to locate user ID");
			}
			else
			{
				DBG.trace(debug.MINOR,"Closing down data streams");
				tu.textIn.close();
				tu.dataIn.close();
				tu.dataOut.close();
				users.removeElement(tu);
				DBG.trace(debug.MAJOR,"Client deregistered");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR,"Can't deregister client ("+exIO.getMessage()+")");
		}
		DBG.trace(debug.DEBUG, "<-- deregisterClient");
	}

/*------------------------------------------------*/
/* Add the client to the list of registered users */
/*------------------------------------------------*/
	void registerClient()
	{
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> registerClient");

		DBG.trace(debug.MINOR, "Creating new trots user");
		tu = new trotsUser();

		DBG.trace(debug.MINOR, "Generating client ID");
		while(isDuplicateUser(tu.id)) tu.id++;

		DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(tu.id).toString());

		try
		{
			DBG.trace(debug.MINOR,"Sending ID to client");
			dataOut.writeInt(tu.id);

			DBG.trace(debug.MINOR,"Waiting for user name");
			tu.userName = textIn.readLine();
			DBG.trace(debug.DEBUG,new StringBuffer("User Name = ").append(tu.userName).toString());

			DBG.trace(debug.MINOR,"Waiting for phone number");
			tu.phoneNo = textIn.readLine();
			DBG.trace(debug.DEBUG,new StringBuffer("Phone No = ").append(tu.phoneNo).toString());

			DBG.trace(debug.MINOR,"Waiting for queue list");
			tu.queueList = textIn.readLine();
			DBG.trace(debug.DEBUG, new StringBuffer("Queue List = ").append(tu.queueList).toString());

			DBG.trace(debug.MINOR,"Waiting for Client version");
			tu.version = textIn.readLine();
			DBG.trace(debug.DEBUG, new StringBuffer("Client Version = ").append(tu.version).toString());

			DBG.trace(debug.MINOR,"Assigning input/output streams");
			tu.dataIn = dataIn;
			tu.dataOut = dataOut;
			tu.textIn = textIn;

			DBG.trace(debug.MINOR, "Obtaining client host name");
			tu.hostname = client.getInetAddress().getHostName();
			DBG.trace(debug.DEBUG, new StringBuffer("Client Host Name = ").append(tu.hostname).toString());

			DBG.trace(debug.MINOR,"Adding user to list of clients");
			users.insertElementAt(tu, tu.id - 1);
			DBG.trace(debug.MAJOR,"Client registered");

		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR,"Unable to process CLIENT_REGISTER (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG,"<--registerClient");
	}

/*----------------------------------------------*/
/* Send the TROTS message to the specified user */
/*----------------------------------------------*/
	public void sendMessage(trotsUser user, trotsMsgItem msg)
	{
		trotsUser sender;
		String from;

		DBG.trace(debug.DEBUG, "--> sendMessage");

		try
		{

			sender = findUser(msg.sender);
			if(sender == null)
			{
				from = new String("Unknown");
			}
			else
			{
				from = new StringBuffer(sender.userName).append(" - ").append(sender.phoneNo).toString();
			}

			DBG.trace(debug.MINOR,"Sending TROTS_MESSAGE_RECV");
			user.dataOut.writeInt(Constants.TROTS_MESSAGE_RECV);
			DBG.trace(debug.DEBUG,new StringBuffer("Sending Message ID [").append(msg.id).append("]").toString());
			user.dataOut.writeInt(msg.id);
			DBG.trace(debug.DEBUG,new StringBuffer("Sending From [").append(from).append("]").toString());
			user.dataOut.writeBytes(new StringBuffer(from).append("\n").toString());
			DBG.trace(debug.DEBUG, new StringBuffer("Sending Message Text [").append(msg.msgText).append("]").toString());
			user.dataOut.writeBytes(new StringBuffer(msg.msgText).append("\n").toString());

			DBG.trace(debug.MINOR,"Waiting for client response");
			DBG.trace(debug.DEBUG,new StringBuffer("Response [").append(user.dataIn.readInt()).append("]").toString());

			DBG.trace(debug.MAJOR, "TROTS_MESSAGE_RECV sent");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to send TROTS_MESSAGE_RECV to " + user.userName + "(" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- sendMessage");
	}

/*----------------------------------------*/
/* Process the TROTS_MESSAGE_SEND details */
/*----------------------------------------*/
	public void processTrotsMessage()
	{
		trotsMsgItem tm;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> processTrotsMessage");

		DBG.trace(debug.MAJOR,"Creating new trots message");
		tm = new trotsMsgItem();

		DBG.trace(debug.MINOR,"Generating message ID");
		while(isDuplicateMsg(tm.id)) tm.id++;

		DBG.trace(debug.DEBUG, "Message ID = " + tm.id);

		try
		{

			DBG.trace(debug.MINOR, "Waiting for user ID");
			tm.sender = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("User ID = ").append(tm.sender).toString());

			DBG.trace(debug.MINOR, "Waiting for queue name");
			tm.queue = textIn.readLine();
			DBG.trace(debug.DEBUG, new StringBuffer("Queue Name = ").append(tm.queue).toString());

			DBG.trace(debug.DEBUG, "Updating statistics");
			serverStats.add(tm.queue);
			serverStats.sent(tm.queue);

			DBG.trace(debug.MINOR, "Waiting for message text");
			tm.msgText = textIn.readLine();
			DBG.trace(debug.DEBUG, new StringBuffer("Message Text = ").append(tm.msgText).toString());

			DBG.trace(debug.MINOR, "Sending message ID");
			dataOut.writeInt(tm.id);

			DBG.trace(debug.MINOR, "Finding recipients");
			for(int i = 0; i < users.size(); i++)
			{
				tu = (trotsUser)users.elementAt(i);
				DBG.trace(debug.DEBUG, new StringBuffer("Checking [").append(tu.userName).append("] Queues [").append(tu.queueList).append("]").toString());
				if(tu.isUserQueue(tm.queue) && tu.available)
				{
					DBG.trace(debug.DEBUG, new StringBuffer("Adding [").append(tu.userName).append("]").toString());
					tm.addUser(tu);
					sendMessage(tu,tm);
				}
			}
			DBG.trace(debug.MINOR,"Adding message to list of messages");
			msgs.addElement(tm);

		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process TROTS_MESSAGE_SEND (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processTrotsMessage");
	}

/*--------------------------------*/
/* Process a TROTS_CANCEL message */
/*--------------------------------*/
	void processTrotsCancel()
	{
		int msgID;
		trotsMsgItem tm;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> processTrotsCancel");
		DBG.trace(debug.MAJOR, "Cancelling trots message");

		try
		{
			DBG.trace(debug.MINOR, "Waiting for message ID");
			msgID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Message ID = ").append(msgID).toString());
			DBG.trace(debug.MINOR, "Sending ACK");
			dataOut.writeInt((Constants.ACK));


			DBG.trace(debug.MINOR,"Locating message");
			tm = findMsg(msgID);

			if(tm == null)
			{
				DBG.trace(debug.MAJOR, "Unable to find message (Non-fatal)");
				DBG.trace(debug.DEBUG, "<--processTrotsCancel");
				return;
			}
			
			serverStats.cancelled(tm.queue);

/*---------------------------------------------------------------------------*/
/* Because the TROTS_CANCEL came from another connection we need to tell the */
/* sender to stop listening to the main connection for a response            */
/*---------------------------------------------------------------------------*/

			DBG.trace(debug.MINOR,"Locating sender");
			tu = findUser(tm.sender);

			if(tu != null)
			{
				try
				{
					DBG.trace(debug.MINOR, "Sending TROTS_CANCEL to sender");
					tu.dataOut.writeInt(Constants.TROTS_CANCEL);
					DBG.trace(debug.MINOR, new StringBuffer("Sending message ID [").append(tm.id).append("] to sender").toString());
					tu.dataOut.writeInt(tm.id);
				}
				catch(IOException exIO)
				{
					DBG.trace(debug.ERROR, "Unable to send TROTS_CANCEL to sender (" + exIO.getMessage() + ")");
				}
			}
			else
			{
				DBG.trace(debug.MAJOR, "Unable to locate sender (Non-fatal)");
			}

			DBG.trace(debug.MINOR, "Processing recipients");
			for(int i = 0 ; i < tm.recipients.size(); i++)
			{
				tu = (trotsUser)tm.recipients.elementAt(i);
				DBG.trace(debug.DEBUG, new StringBuffer("Processing [").append(tu.userName).append("]").toString());
				try
				{
					DBG.trace(debug.DEBUG, "Sending TROTS_CANCEL");
					tu.dataOut.writeInt(Constants.TROTS_CANCEL);
					DBG.trace(debug.DEBUG, "Sending message id");
					tu.dataOut.writeInt(msgID);
				}
				catch(IOException exIO)
				{
					DBG.trace(debug.ERROR, "Unable to send TROTS_CANCEL (" + exIO.getMessage() + ")");
				}
			}

			DBG.trace(debug.MINOR, "Removing message from list of messages");
			msgs.removeElement(tm);
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process TROTS_CANCEL (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processTrotsCancel");
	}

/*-------------------------------------------------*/
/* Tell the message sender who picked this call up */
/*-------------------------------------------------*/
	void tellSender(trotsMsgItem tm, trotsUser tu)
	{
		trotsUser sender;

		DBG.trace(debug.DEBUG, "--> tellSender");

		try
		{
			DBG.trace(debug.MINOR,"Locating sender details");
			sender = findUser(tm.sender);

			if(sender==null)
			{
				DBG.trace(debug.MINOR,new StringBuffer("Unable to locate User ID [").append(+tm.sender).append("]").toString());
				DBG.trace(debug.DEBUG, "<--tellSender");
				return;
			}

			DBG.trace(debug.DEBUG,new StringBuffer("Sender is [").append(sender.userName).append("]").toString());

			DBG.trace(debug.MINOR,"Sending TROTS_ASSIGN_SEND to sender");
			sender.dataOut.writeInt(Constants.TROTS_ASSIGN_SEND);
			DBG.trace(debug.MINOR,new StringBuffer("Sending message id [").append(tm.id).append("]").toString());
			sender.dataOut.writeInt(tm.id);
			DBG.trace(debug.MINOR, new StringBuffer("Sending [").append(tu.userName).append("] to sender").toString());
			sender.dataOut.writeBytes(new StringBuffer("Call accepted by ").append(tu.userName).append(" - ").append(tu.phoneNo).append("\n").toString());

			DBG.trace(debug.MINOR, "Waiting for server response");
			DBG.trace(debug.DEBUG, new StringBuffer("Server response [").append(sender.dataIn.readInt()).append("]").toString());
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to send TROTS_ASSIGN_SEND to sender (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<--tellSender");
	}

/*--------------------------------*/
/* Process a CLIENT_RESET message */
/*--------------------------------*/
	public void processClientReset()
	{
		int clientID;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> processClientReset");

		try
		{
			DBG.trace(debug.MINOR, "Waiting for client ID");
			clientID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(clientID).toString());

			DBG.trace(debug.MINOR, "Sending ACK response");
			dataOut.writeInt(Constants.ACK);

			DBG.trace(debug.MINOR, "Locating client");
			tu = findUser(clientID);

			if(tu == null)
			{
				DBG.trace(debug.MAJOR, "Unable to locate client ID (Non-fatal)");
				DBG.trace(debug.DEBUG, "<-- processClientReset");
				return;
			}

			DBG.trace(debug.DEBUG, new StringBuffer("User Name = ").append(tu.userName).toString());
			DBG.trace(debug.MINOR, "Sending CLIENT_RESET to client");
			
			tu.dataOut.writeInt(Constants.CLIENT_RESET);

			DBG.trace(debug.MINOR, "Waiting for ACK");
			DBG.trace(debug.DEBUG, new StringBuffer("Client responded with ").append(tu.dataIn.readInt()).toString());

			DBG.trace(debug.MINOR, "De-registering client");
			tu.textIn.close();
			tu.dataIn.close();
			tu.dataOut.close();
			users.removeElement(tu);
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process CLIENT_RESET (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processClientReset");
	}

	void shutdownClient(trotsUser tu)
	{
		DBG.trace(debug.DEBUG, "-->shutdownClient");
		
		if(tu != null)
		{
			try
			{
				DBG.trace(debug.MINOR, "Sending CLIENT_SHUTDOWN");
				tu.dataOut.writeInt(Constants.CLIENT_SHUTDOWN);
				DBG.trace(debug.MINOR, "Closing input/output streams");
				tu.dataOut.close();
				tu.dataIn.close();
				tu.textIn.close();
			}
			catch(IOException exIO)
			{
				DBG.trace(debug.ERROR, "Unable to close client");
			}
			DBG.trace(debug.MINOR, "Removing client entry");
			users.removeElement(tu);
		}

		DBG.trace(debug.DEBUG, "<--shutdownClient");
	}

/*-----------------------------------*/
/* Process a CLIENT_SHUTDOWN request */
/*-----------------------------------*/
	void processClientShutdown()
	{
		int userID;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "-->processClientShutdown");
		try
		{
			DBG.trace(debug.MINOR, "Waiting for client ID");
			userID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(userID).toString());
			
			DBG.trace(debug.MINOR, "Locating client ID");
			tu = findUser(userID);

			if(tu != null)
			{
				if(tu.queueList.equalsIgnoreCase(Constants.CALL_RECEIPT_QUEUE))
				{
					DBG.trace(debug.MAJOR, "User is a call receipt user. Will not send shutdown");
				}
				else
				{
					shutdownClient(tu);
				}
			}
			else
			{
				DBG.trace(debug.MINOR, "Unable to locate client ID");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process CLIENT_SHUTDOWN");
		}
		DBG.trace(debug.DEBUG, "<--processClientShutdown");
	}

/*---------------------------------*/
/* Process a CLIENT_STATUS message */
/*---------------------------------*/
	public void processClientStatus()
	{
		int clientID, status;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> processClientStatus");

		try
		{
			DBG.trace(debug.MINOR, "Waiting for client ID");
			clientID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(clientID).toString());

			DBG.trace(debug.MINOR, "Waiting for client status");
			status = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client Status = ").append(status).toString());

			DBG.trace(debug.MINOR, "Sending ACK response");
			dataOut.writeInt((Constants.ACK));

			DBG.trace(debug.MINOR, "Locating client");
			tu = findUser(clientID);

			if(tu == null)
			{
				DBG.trace(debug.MAJOR, "Unable to locate client ID (Non-fatal)");
				DBG.trace(debug.DEBUG, "<--processClientStatus");
				return;
			}

			DBG.trace(debug.DEBUG, new StringBuffer("User Name = ").append(tu.userName).toString());
			DBG.trace(debug.MAJOR, "Setting user availability");
			tu.available = (status > 0 ? false : true);
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process CLIENT_STATUS (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG,"<--processClientStatus");
	}

/*--------------------------------*/
/* Process a TROTS_ASSIGN_RECV message */
/*--------------------------------*/
	public void processTrotsAssign()
	{
		int msgID, userID;
		trotsMsgItem tm;
		trotsUser tu;
		DBG.trace(debug.DEBUG, "--> processTrotsAssign");
		DBG.trace(debug.MAJOR, "Processing TROTS_ASSIGN_RECV");

		try
		{
			DBG.trace(debug.MINOR,"Waiting for Message ID");
			msgID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Message ID = ").append(msgID).toString());

			DBG.trace(debug.MINOR, "Waiting for User ID");
			userID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("User ID = ").append(userID).toString());

			DBG.trace(debug.MINOR, "Sending ACK response");
			dataOut.writeInt((Constants.ACK));
			dataOut.flush();

			DBG.trace(debug.MINOR, new StringBuffer("Locating Message ID [").append(msgID).append("]").toString());

			tm = findMsg(msgID);

			if(tm == null)
			{
				DBG.trace(debug.MAJOR, new StringBuffer("Message ID [").append(msgID).append("] could not be found (Non-fatal)").toString());
				DBG.trace(debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			if(tm.flag == Constants.MSG_PROCESSED)
			{
				DBG.trace(debug.MINOR, new StringBuffer("Message [").append(msgID).append("] already processed").toString());
				DBG.trace(debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			DBG.trace(debug.MINOR, "Setting message to processed");
			tm.flag = Constants.MSG_PROCESSED;

			DBG.trace(debug.MINOR, new StringBuffer("Locating User ID [").append(userID).append("]").toString());
			tu = findUser(userID);

			if(tu == null)
			{
				DBG.trace(debug.MAJOR, new StringBuffer("User [").append(userID).append("] could not be found").toString());
				DBG.trace(debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			DBG.trace(debug.DEBUG, new StringBuffer("User ID [").append(userID).append("] is [").append(tu.userName).append("]").toString());

			serverStats.accepted(tm.queue);

			DBG.trace(debug.MINOR, "Telling sender who picked this up");
			tellSender(tm, tu);

			DBG.trace(debug.MINOR, "Telling other recipients who picked this up");
			tellRecipients(tm, tu);

			DBG.trace(debug.MINOR, "Removing message from list of messages");
			msgs.removeElement(tm);

		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process TROTS_ASSIGN (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processTrotsAssign");
	}
	
/*------------------------------*/
/* Process a LIST_USERS request */
/*------------------------------*/
	public void processListUsers()
	{
		Vector userCopy;
		trotsUser tu;
		int userCount;

		DBG.trace(debug.DEBUG, "--> processListUsers");
		DBG.trace(debug.MAJOR, "Sending list of user names");
		userCopy = (Vector)users.clone();
		try
		{
			userCount = 0;
			for(int i=0; i< userCopy.size(); i++)
			{
				tu = (trotsUser)userCopy.elementAt(i);
				if(i > 0) dataOut.writeBytes(",");
				dataOut.writeBytes(new StringBuffer("").append(tu.id).append("#").append(tu.userName).toString());
			}
			dataOut.writeBytes("\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process LIST_USERS");
		}
		DBG.trace(debug.DEBUG, "<-- processListUsers");
	}

	
/*---------------------------------------------------------------------*/
/* Check that all users are still talking to us by sending a heartbeat */
/* if not then we need to clean them up                                */
/*---------------------------------------------------------------------*/
	public void processCleanUsers()
	{
		Vector userCopy;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> processCleanUsers");
		DBG.trace(debug.MAJOR, "Checking all user connections");
		userCopy = (Vector)users.clone();
		for(int i=0; i< userCopy.size(); i++)
		{
			tu = (trotsUser)userCopy.elementAt(i);

			try
			{
				DBG.trace(debug.DEBUG, "Checking " + tu.userName + " ("+tu.id+")");
				tu.dataOut.writeInt(Constants.HEARTBEAT);
				tu.dataIn.readInt();
			}
			catch(IOException exIO)
			{
				DBG.trace(debug.ERROR, "Unable to talk to " + tu.userName + " ("+tu.id+") [" + exIO.getMessage() +"] - Removing connection");
				try
				{
					tu.dataIn.close();
					tu.dataOut.close();
					tu.textIn.close();
				}
				catch(Exception ex) {}
				users.removeElement(tu);
			}
		}
		try
		{
			DBG.trace(debug.DEBUG, "Sending ACK to client");
			dataOut.writeInt(Constants.ACK);
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to send ACK in response to CLEAN_USERS");
		}
		DBG.trace(debug.DEBUG, "<-- processCleanUsers");
	}

/*-------------------------------------*/
/* Respond to a SERVER_VERSION command */
/*-------------------------------------*/
	public void processServerVersion()
	{
		DBG.trace(debug.DEBUG, "--> processServerVersion");
		DBG.trace(debug.MAJOR, "Sending server version");
		try
		{
			DBG.trace(debug.MINOR, "Sending revision");
			DBG.trace(debug.DEBUG, "$Revision: 6 $");
			dataOut.writeBytes("$Revision: 6 $\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process SERVER_VERSION");
		}
		DBG.trace(debug.DEBUG, "<-- processServerVersion");
	}

/*-------------------------------------*/
/* Detail the current server operation */
/*-------------------------------------*/
	public void processServerStatusHTML()
	{
		Vector userCopy, messageCopy;
		trotsUser tu;
		trotsMsgItem tm;
		BSumTime bst = new BSumTime();
		Date date;
		String queueName;


		DBG.trace(debug.DEBUG, "--> processServerStatusHTML");

		DBG.trace(debug.MAJOR, "Dumping server status as HTML");

		DBG.trace(debug.DEBUG, "Setting GMT0BST time zone");
		TimeZone.setDefault( bst );
		SimpleDateFormat dtf = bst.getDateTimeFormatter();

		userCopy = (Vector)users.clone();
		messageCopy = (Vector)msgs.clone();

		try
		{
			dataOut.writeBytes("<HTML>\n");
			dataOut.writeBytes("<TITLE>Server Status</TITLE>\n");
			dataOut.writeBytes("<BODY BGCOLOR=#FFFFFF>");
			dataOut.writeBytes("<CENTER><H1>TROTS statistics</H1></CENTER><BR>\n");
			dataOut.writeBytes("<HR>\n");
			dataOut.writeBytes("<H2>Server Status</H2>\n");

			date = new Date(startTime * 1000);

			dataOut.writeBytes("<H3>Start Time: " + dtf.format(date) + "</H3>\n");
			dataOut.writeBytes("<HR>\n");

			dataOut.writeBytes("<H2>Queues</H2>\n");

			if(currentQueues.size() > 0)
			{
				for(int i=0; i < currentQueues.size(); i++)
				{
					queueName = (String)currentQueues.elementAt(i);
					dataOut.writeBytes("<LI><H3>"+queueName+"</H3>\n");
				}
			}
			else
			{
				dataOut.writeBytes("<H3>None available</H3>\n");
			}
		
			dataOut.writeBytes("<HR>\n");

			dataOut.writeBytes("<H2>Call Statistics</H2>\n");

			dataOut.writeBytes("<H3>Total messages: " + serverStats.msgTotal +"</H3><BR>\n");
			if(serverStats.size() != 0)
			{
				dataOut.writeBytes("<CENTER>\n");
				dataOut.writeBytes("<TABLE BORDER=1>\n");
				dataOut.writeBytes("<TR>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Queue</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Sent</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Accepted</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Cancelled</TH></TR>\n");
				for(int i=0; i< serverStats.size(); i++)
				{
					dataOut.writeBytes("<TR>");
					dataOut.writeBytes("<TD ALIGN=CENTER>"+serverStats.getQueue(i)+"</TD>\n");
					dataOut.writeBytes("<TD ALIGN=CENTER>"+serverStats.getSent(i)+"</TD>\n");
					dataOut.writeBytes("<TD ALIGN=CENTER>" + serverStats.getAccepted(i)+"</TD>\n");
					dataOut.writeBytes("<TD ALIGN=CENTER>" + serverStats.getCancelled(i) +"</TD>\n");
					dataOut.writeBytes("</TR>\n");
				}
				dataOut.writeBytes("</TABLE>\n");
				dataOut.writeBytes("</CENTER>\n");
			}
			else
			{
				dataOut.writeBytes("<H3>No Call Statistics available</H3>\n");
			}
	
			dataOut.writeBytes("<HR>\n");
	
			dataOut.writeBytes("<H2>Registered Clients</H2>\n");
			if(userCopy.size() == 0)
			{
				dataOut.writeBytes("<H3>No Registered Clients</H3>");
			}
			else
			{
				dataOut.writeBytes("<CENTER>\n");
				dataOut.writeBytes("<TABLE BORDER=1>\n");
				dataOut.writeBytes("<TR>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Active</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Connection Time</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Connection ID</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>User Name</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Queues</TH>\n");
				dataOut.writeBytes("</TR>");
				for(int i=0; i< userCopy.size(); i++)
				{
					tu = (trotsUser)userCopy.elementAt(i);
					date = new Date( tu.createTime * 1000 );
					dataOut.writeBytes("<TR>\n");
					dataOut.writeBytes("<TD>" + (tu.available ? "Yes" : "NO") + "</TD>\n");
					dataOut.writeBytes("<TD>"+dtf.format(date) + "</TD>\n");
					dataOut.writeBytes("<TD ALIGN=CENTER>"+tu.id+"</TD>\n");
					dataOut.writeBytes("<TD>"+tu.userName+"</TD>\n");
					dataOut.writeBytes("<TD>"+tu.queueList+"</TD>\n");
					dataOut.writeBytes("</TR>\n");
				}
				dataOut.writeBytes("</TABLE>\n");
				dataOut.writeBytes("</CENTER>\n");
			}
			dataOut.writeBytes("<HR>\n");
			
			dataOut.writeBytes("<H2>Current Messages</H2>\n");
			if(messageCopy.size() == 0)
			{
				dataOut.writeBytes("<H3>No current messages</H3>");
			}
			else
			{
				dataOut.writeBytes("<CENTER>\n");
				dataOut.writeBytes("<TABLE BORDER=1>\n");
				dataOut.writeBytes("<TR>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Create Time</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Message ID</TH>\n");
				dataOut.writeBytes("<TH ALIGN=CENTER>Queue</TH>\n");
				dataOut.writeBytes("</TR>");
				for(int i=0; i< messageCopy.size(); i++)
				{
					tm = (trotsMsgItem)messageCopy.elementAt(i);
					date = new Date( tm.createTime * 1000 );
					dataOut.writeBytes("<TR>\n");
					dataOut.writeBytes("<TD>"+dtf.format(date) + "</TD>\n");
					dataOut.writeBytes("<TD>"+tm.id+"</TD>\n");
					dataOut.writeBytes("<TD>"+tm.queue +"</TD>\n" );
					dataOut.writeBytes("</TR>\n");
				}
				dataOut.writeBytes("</TABLE>\n");
				dataOut.writeBytes("</CENTER>\n");
			}
			dataOut.writeBytes("<HR>\n");
	
			dataOut.writeBytes("</BODY>\n</HTML>\n");
		}
		catch(IOException exIO) {}
		DBG.trace(debug.DEBUG, "<-- processServerStatusHTML");
	}

/*-------------------------------*/
/* Process a QUEUE_NAMES request */
/*-------------------------------*/
	public void processQueueNames()
	{
		DBG.trace(debug.DEBUG, "--> processQueueNames");
		DBG.trace(debug.MAJOR, "Sending queue names");
		try
		{
			dataOut.writeBytes(queue_vector_to_list()+"\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process QUEUE_NAMES (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<--processQueueNames");
	}

/*---------------------------------*/
/* Process a QUEUE_DETAILS command */
/*---------------------------------*/
	public void processQueueDetails()
	{
		String queueName;
		int queuePos;

		DBG.trace(debug.DEBUG, "-->processQueueDetails");
		try
		{
			DBG.trace(debug.MINOR, "Waiting for queue name");
			queueName = textIn.readLine();
			queueName = queueName.toUpperCase();
			DBG.trace(debug.DEBUG, new StringBuffer("Queue Name = ").append(queueName).toString());

			if(currentQueues.contains(queueName))
			{
				dataOut.writeBytes(serverStats.getQueueStats(queueName));
			}
			
			dataOut.writeBytes("\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process QUEUE_DETAILS (" + exIO.getMessage() +")");
		}
		DBG.trace(debug.DEBUG, "<--processQueueDetails");
	}

/*------------------------------*/
/* Process an ADD_QUEUE command */
/*------------------------------*/
	public void processAddQueue()
	{
		String new_queue;
		DBG.trace(debug.DEBUG, "--> processAddQueue");

		try
		{
			new_queue = textIn.readLine();
			new_queue = new_queue.toUpperCase();
			DBG.trace(debug.MAJOR, new StringBuffer("Adding queue [").append(new_queue).append("]").toString());
			if( ! currentQueues.contains(new_queue) )
			{
				currentQueues.addElement(new_queue);
				serverStats.add(new_queue);
				props.put("queues",queue_vector_to_list());
				props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on ADD_QUEUE");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process ADD_QUEUE (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processAddQueue");
	}

/*--------------------------------*/
/* Process a USER_DETAILS command */
/*--------------------------------*/
	public void processUserDetails()
	{
		int userID;
		trotsUser tu;
		BSumTime bst = new BSumTime();

		DBG.trace(debug.DEBUG, "--> processUserDetails");
		DBG.trace(debug.MAJOR, "Listing user details");

		DBG.trace(debug.DEBUG, "Setting GMT0BST time zone");
		TimeZone.setDefault( bst );

		SimpleDateFormat dtf = bst.getDateTimeFormatter();

		try
		{
			DBG.trace(debug.MINOR, "Waiting for user ID");
			userID = dataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(userID).toString());

			tu = findUser(userID);

			if(tu == null) userID = 0;

			DBG.trace(debug.MINOR, "Sending user ID back as acknowledgment");
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(userID).toString());
			dataOut.writeInt(userID);

			if(userID > 0)
			{
				Date date = new Date( tu.createTime * 1000 );
				DBG.trace(debug.MINOR, "Sending User Name");
				DBG.trace(debug.DEBUG, new StringBuffer("User Name = ").append(tu.userName).toString());
				dataOut.writeBytes(tu.userName);
				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Phone Number");
				DBG.trace(debug.DEBUG, new StringBuffer("Phone Number = ").append(tu.phoneNo).toString());
				dataOut.writeBytes(tu.phoneNo);
				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Connection Time");
				DBG.trace(debug.DEBUG, dtf.format(date) );
				dataOut.writeBytes(dtf.format(date) );
				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Availability");
				DBG.trace(debug.DEBUG, "Availability = " + (tu.available ? "Yes" : "NO"));
				dataOut.writeBytes((tu.available ? "Yes" : "NO"));
				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Queue List");
				DBG.trace(debug.DEBUG, tu.queueList);
				dataOut.writeBytes(tu.queueList);

				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Client Host Name");
				DBG.trace(debug.DEBUG, tu.hostname);
				dataOut.writeBytes(tu.hostname);

				dataOut.writeBytes("#");
				DBG.trace(debug.MINOR, "Sending Client Version");
				DBG.trace(debug.DEBUG, tu.version);
				dataOut.writeBytes(tu.version);

				dataOut.writeBytes("\n");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process USER_DETAILS (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processUserDetails");
	}

/*-----------------------------*/
/* Process a DEL_QUEUE command */
/*-----------------------------*/
	public void processDelQueue()
	{
		String del_queue;
		DBG.trace(debug.DEBUG, "--> processDelQueue");

		try
		{
			DBG.trace(debug.MINOR, "Waiting for queue name");
			del_queue = textIn.readLine();
			del_queue = del_queue.toUpperCase();
			DBG.trace(debug.DEBUG, new StringBuffer("Queue Name = ").append(del_queue).toString());
			if( currentQueues.contains(del_queue) )
			{
				DBG.trace(debug.MAJOR, new StringBuffer("Deleting queue [").append(del_queue).append("]").toString());
				DBG.trace(debug.MINOR, "Removing statistics");
				serverStats.remove(del_queue);
				DBG.trace(debug.MINOR, "Removing queue from list");
				currentQueues.removeElement(del_queue);
				DBG.trace(debug.MINOR, "Updating queue property in ini file");
				props.put("queues",queue_vector_to_list());
				props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEL_QUEUE");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to process DEL_QUEUE (" + exIO.getMessage() + ")");
		}
		DBG.trace(debug.DEBUG, "<-- processDelQueue");
	}

/*-------------*/
/* Constructor */
/*-------------*/
	public trotsServer(int port)
	{
		int command = Constants.CLIENT_NULL;
		trotsUser tu;

		DBG.trace(debug.DEBUG, "--> trotsServer (Constructor)");

		users = new Vector();
		msgs = new Vector();
		currentQueues = new Vector();

		DBG.trace(debug.MAJOR, "Initialising counters");
		serverStats = new AllStats();

		queue_list_to_vector();

		startTime = System.currentTimeMillis() / 1000;

		try
		{
/*--------------------------*/
/* Create the server socket */
/*--------------------------*/
			DBG.trace(debug.MAJOR, new StringBuffer("Creating socket on port ").append(port).toString());
			server = new ServerSocket(port);

			while(command != Constants.SERVER_SHUTDOWN && command != Constants.ALL_SHUTDOWN)
			{
				try
				{
/*-------------------------*/
/* Wait for client command */
/*-------------------------*/
					DBG.trace(debug.MINOR, "Waiting for client connection");
					client = server.accept();

/*----------------------------*/
/* Build input/output streams */
/*----------------------------*/
					DBG.trace(debug.MINOR, "Creating input/output streams");
					dataIn = new DataInputStream(client.getInputStream());
					dataOut = new DataOutputStream(client.getOutputStream());
					textIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

/*--------------------*/
/* Set socket timeout */
/*--------------------*/
					client.setSoTimeout(Constants.SOCKET_TIMEOUT * 1000);

/*------------------------*/
/* Get the client command */
/*------------------------*/
					DBG.trace(debug.MINOR, "Waiting for client command");
					command = dataIn.readInt();

					DBG.trace(debug.DEBUG, new StringBuffer("Received command [").append(command).append("]").toString());

					switch(command)
					{
						case Constants.CLIENT_REGISTER:
							DBG.trace(debug.MAJOR,"CLIENT_REGISTER");
							registerClient();
							break;
						case Constants.CLIENT_DEREGISTER:
							DBG.trace(debug.MAJOR, "CLIENT_DEREGISTER");
							deregisterClient();
							client.close();
							break;
						case Constants.SERVER_SHUTDOWN:
							DBG.trace(debug.ERROR, "SERVER_SHUTDOWN");
							break;
						case Constants.ALL_SHUTDOWN:
							DBG.trace(debug.ERROR, "ALL_SHUTDOWN");
							break;
						case Constants.SERVER_VERSION:
							DBG.trace(debug.MAJOR, "SERVER_VERSION");
							processServerVersion();
							break;
						case Constants.TROTS_MESSAGE_SEND:
							DBG.trace(debug.MAJOR,"TROTS_MESSAGE_SEND");
							processTrotsMessage();
							break;
						case Constants.TROTS_ASSIGN_RECV:
							DBG.trace(debug.MAJOR, "TROTS_ASSIGN_RECV");
							processTrotsAssign();
							break;
						case Constants.CLIENT_STATUS:
							DBG.trace(debug.MAJOR, "CLIENT_STATUS");
							processClientStatus();
							break;
						case Constants.TROTS_CANCEL:
							DBG.trace(debug.MAJOR, "TROTS_CANCEL");
							processTrotsCancel();
							break;
						case Constants.QUEUE_NAMES:
							DBG.trace(debug.MAJOR, "QUEUE_NAMES");
							processQueueNames();
							break;
						case Constants.DEBUG_MAJOR:
							DBG.trace(debug.ERROR, "Debug level set to MAJOR");
							DBG.setLevel(debug.MAJOR);
							props.put("debug",""+debug.MAJOR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_MAJOR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_MINOR:
							DBG.trace(debug.ERROR, "Debug level set to MINOR");
							DBG.setLevel(debug.MINOR);
							props.put("debug",""+debug.MINOR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_MINOR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_DEBUG:
							DBG.trace(debug.ERROR, "Debug level set to DEBUG");
							DBG.setLevel(debug.DEBUG);
							props.put("debug",""+debug.DEBUG);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_DEBUG");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_ERROR:
							DBG.trace(debug.ERROR, "Debug level set to ERROR");
							DBG.setLevel(debug.ERROR);
							props.put("debug",""+debug.ERROR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_ERROR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.STATUS_HTML:
							DBG.trace(debug.MAJOR, "STATUS_HTML");
							processServerStatusHTML();
							break;
						case Constants.CLIENT_RESET:
							DBG.trace(debug.MAJOR, "CLIENT_RESET");
							processClientReset();
							break;
						case Constants.ADD_QUEUE:
							DBG.trace(debug.MAJOR, "ADD_QUEUE");
							processAddQueue();
							break;
						case Constants.DEL_QUEUE:
							DBG.trace(debug.MAJOR, "DEL_QUEUE");
							processDelQueue();
							break;
						case Constants.CLEAN_USERS:
							DBG.trace(debug.MAJOR, "CLEAN_USERS");
							processCleanUsers();
							break;
						case Constants.HEARTBEAT:
							DBG.trace(debug.MAJOR, "HEARTBEAT");
							dataOut.writeInt(Constants.ACK);
							break;
						case Constants.LIST_USERS:
							DBG.trace(debug.MAJOR, "LIST_USERS");
							processListUsers();
							break;
						case Constants.DEBUG_DETAILS:
							DBG.trace(debug.MAJOR, "DEBUG_DETAILS");
							dataOut.writeInt(DBG.getLevel());
							break;
						case Constants.USER_DETAILS:
							DBG.trace(debug.MAJOR, "USER_DETAILS");
							processUserDetails();
							break;
						case Constants.CLIENT_SHUTDOWN:
							DBG.trace(debug.MAJOR, "CLIENT_SHUTDOWN");
							processClientShutdown();
							break;
						case Constants.QUEUE_DETAILS:
							DBG.trace(debug.MAJOR, "QUEUE_DETAILS");
							processQueueDetails();
							break;
					}
					System.gc();
				}
				catch(IOException exIO)
				{
					DBG.trace(debug.ERROR, "Unable to maintain connection with client ("+exIO.getMessage()+")");
				}
			}

/*-------------------------------------------------------------------*/
/* Close the client connections cleanly if we are closing everything */
/*-------------------------------------------------------------------*/
			if(command == Constants.ALL_SHUTDOWN)
			{
				int numUsers = users.size();
				DBG.trace(debug.MINOR, "Closing down ["+numUsers+"] users");

				for(int i=0;i < numUsers; i++)
				{
					tu = (trotsUser)users.firstElement();
					DBG.trace(debug.DEBUG, "Closing down [ " + tu.userName + "]");
					shutdownClient(tu);
				}
			}

			DBG.trace(DBG.ERROR, "Normal Completion");
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to listen to client connections ("+exIO.getMessage()+")");
			DBG.trace(debug.ERROR, "Abnormal termination");
		}
	}

	public static void main(String argv[])
	{

		String propServerPort, propQueues;
		DBG = new debug(0, "server.dbg", "TROTS SERVER ($Revision: 6 $) DEBUG TRACE");

		try
		{
			props.load(new FileInputStream(Constants.SERVER_INI_FILE));

			propServerPort = props.getProperty("port");
			propQueues = props.getProperty("queues");

			if(propServerPort == null)
			{
				System.out.println("port property is not in " + Constants.SERVER_INI_FILE);
				System.exit(1);
			}
			if(propQueues == null)
			{
				System.out.println("queues property is not in " + Constants.SERVER_INI_FILE);
				System.exit(1);
			}

			if(props.getProperty("debug") != null)
			{
				DBG.setLevel(makeInt(props.getProperty("debug"), 0));
			}

			DBG.trace(debug.MAJOR,"Properties loaded");
			DBG.trace(debug.DEBUG,"Port = " + propServerPort);
			DBG.trace(debug.DEBUG,"Queues = " + propQueues);
			DBG.trace(debug.DEBUG,"Debug Level = " + props.getProperty("debug"));

			trotsServer ts = new trotsServer(new Integer(propServerPort).intValue());

		}
		catch(FileNotFoundException exFile)
		{
			System.out.println(Constants.SERVER_INI_FILE +" not found");
		}
		catch(IOException exIO)
		{
			System.out.println("Problems loading properties from " + Constants.SERVER_INI_FILE);
			System.out.println("Error: " + exIO.getMessage());
		}
	}
}
