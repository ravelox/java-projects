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
Stopped SERVER_VERSION being logged to Debug file all the time

Revision 1.26  99/07/19  10:22:51  10:22:51  dkelly (Dave Kelly)
Fixed Debug statement

Revision 1.25  99/07/19  10:21:44  10:21:44  dkelly (Dave Kelly)
Added code to retrieve client revision

Revision 1.24  99/07/16  14:11:30  14:11:30  dkelly (Dave Kelly)
Fixed compilation problem after last amendment

Revision 1.23  99/07/16  12:58:30  12:58:30  dkelly (Dave Kelly)
Added ACK response to CLEAN_USERS

Revision 1.22  99/07/15  16:36:44  16:36:44  dkelly (Dave Kelly)
Removed Call Receipt admin commands

Revision 1.21  99/07/14  11:35:10  11:35:10  dkelly (Dave Kelly)
Cleaned up Debug statements

Revision 1.20  99/07/14  09:19:53  09:19:53  dkelly (Dave Kelly)
Added call to remove statistics when a queue is deleted

Revision 1.19  99/07/13  14:59:40  14:59:40  dkelly (Dave Kelly)
Removed deregistration of call receipt users. This now happens in the REMOVE_CALL_RECEIPT functionality

Revision 1.18  99/07/13  14:29:51  14:29:51  dkelly (Dave Kelly)
Changed Debugging statement

Revision 1.17  99/07/13  13:27:47  13:27:47  dkelly (Dave Kelly)
Removed need for additional data to be sent for call receipt deregister

Revision 1.16  99/07/13  12:53:08  12:53:08  dkelly (Dave Kelly)
Multiple changes to add extra admin functionality

Revision 1.15  99/07/07  11:39:47  11:39:47  dkelly (Dave Kelly)
Removed logging of status to Debug file.
Changed reference from SERVER_STATUS to STATUS_HTML

Revision 1.14  99/07/07  11:17:29  11:17:29  dkelly (Dave Kelly)
Added processing to respond to HEARTBEAT request

Revision 1.13  99/07/07  09:19:09  09:19:09  dkelly (Dave Kelly)
Changed shutdown Debug logging to always log when shutdown received

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
Corrected typo in Debugging statement
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
import java.text.*;

/*---------------------------*/
/* Class for TROTS user item */
/*---------------------------*/
class TrotsUser
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
	public TrotsUser()
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
                String[] queues = queueList.split(",");
                for(String currentQueue : queues)
                {
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
class TrotsMsgItem
{
	public int id;
	public String queue;
	int	sender;
	public String msgText;
	public int flag;
	public List<TrotsUser> recipients;
	public long createTime;

/*-------------*/
/* Constructor */
/*-------------*/
	public TrotsMsgItem()
	{
		id = 0;
		recipients = new ArrayList<>();
		createTime = System.currentTimeMillis() / 1000;
	}

/*-------------------------------------------------------*/
/* Add a user to the list of recipients for this message */
/*-------------------------------------------------------*/
	public void addUser(TrotsUser user)
	{
		recipients.add(user);
	}
}

public class TrotsServer
{
	List<TrotsUser> users;
        List<TrotsMsgItem> msgs;
        List<String> currentQueues;
	Random					idGenerator = new Random();
	ServerSocket			server;
	Socket					client;
	DataInputStream		dataIn;
	DataOutputStream		dataOut;
	BufferedReader			textIn;
	static Properties		props = new Properties();
	AllStats		serverStats;
	
	static Debug DBG;

	long startTime;

/*--------------------------------------------------------------*/
/* Utility function to return an integer value from a string or */
/* the supplied default                                         */
/*--------------------------------------------------------------*/
	static int makeInt(String value, int defaultValue)
	{
		int returnValue = defaultValue;

		DBG.trace(Debug.DEBUG, "-->makeInt");
		try
		{
                        returnValue = Integer.parseInt(value);
		}
		catch(NumberFormatException exNumFmt) {}

		DBG.trace(Debug.DEBUG, "<--makeInt");
		return returnValue;
	}

/*---------------------------*/
/* Find the specific user ID */
/*---------------------------*/
	TrotsUser findUser(int id)
	{
		TrotsUser tu;
		DBG.trace(Debug.DEBUG, new StringBuilder("-->findUser (").append(id).append(")").toString());
		for(int i=0; i < users.size(); i++)
		{
			tu = users.get(i);
			if(tu.id == id)
			{
				DBG.trace(Debug.DEBUG,"<--findUser(true)");
				return tu;
			}
		}
		DBG.trace(Debug.DEBUG,"<--findUser(null)");
		return null;
	}

/*------------------------------*/
/* Find the specific message ID */
/*------------------------------*/
	TrotsMsgItem findMsg(int id)
	{
		TrotsMsgItem tm;
		DBG.trace(Debug.DEBUG, new StringBuilder("-->findMsg (").append(id).append(")").toString());
		for(int i=0; i < msgs.size(); i++)
		{
			tm = msgs.get(i);
			if(tm.id == id)
			{
				DBG.trace(Debug.DEBUG, "<--findMsg (true)");
				return tm;
			}
		}
		DBG.trace(Debug.DEBUG, "<--findMsg (null)");
		return null;
	}

/*-----------------------------------------------*/
/* Check to see if the message ID already exists */
/*-----------------------------------------------*/
	boolean isDuplicateMsg(int id)
	{
		TrotsMsgItem tm;
		DBG.trace(Debug.DEBUG, new StringBuilder("-->isDuplicateMsg (").append(id).append(")").toString());
		if(id==0) return true;
		for(int i=0; i < msgs.size(); i++)
		{
			tm = msgs.get(i);
			if(tm.id == id)
			{
				DBG.trace(Debug.DEBUG, "<--isDuplicateMsg(true)");
				return true;
			}
		}
		DBG.trace(Debug.DEBUG, "<--isDuplicateMsg(false)");
		return false;
	}

/*--------------------------------------------*/
/* Check to see if the user ID already exists */
/*--------------------------------------------*/
	boolean isDuplicateUser(int id)
	{
		TrotsUser tu;
		DBG.trace(Debug.DEBUG, new StringBuilder("-->isDuplicateUser (").append(id).append(")").toString());
		if(id==0) return true;
		for(int i=0; i < users.size(); i++)
		{
			tu = users.get(i);
			if(tu.id == id)
			{
				DBG.trace(Debug.DEBUG, "<--isDuplicateUser (true)");
				return true;
			}
		}
		DBG.trace(Debug.DEBUG, "<--isDuplicateUser (false)");
		return false;
	}

/*----------------------------------------------------------*/
/* Convert the vector of queues into a comma separated list */
/*----------------------------------------------------------*/
	String queue_vector_to_list()
	{
		DBG.trace(Debug.DEBUG,"-->queue_vector_to_list");
		String output = new String("");

		for(int i=0; i < currentQueues.size(); i++)
		{
			if(i > 0) output = output.concat(",");
			output = output.concat(currentQueues.get(i));
		}
		DBG.trace(Debug.DEBUG, "<--queue_vector_to_list");
		return output;
	}

/*----------------------------------------------------------*/
/* Convert the comma separated list of queues into a vector */
/*----------------------------------------------------------*/
	void queue_list_to_vector()
	{
                String[] queues = props.getProperty("queues", "").split(",");
                String this_queue;

                DBG.trace(Debug.DEBUG, "-->queue_list_to_vector");

                for(String queue : queues)
                {
                        this_queue = queue;
			if(! currentQueues.contains(this_queue) )
			{
				currentQueues.add(this_queue.toUpperCase());
				serverStats.add(this_queue.toUpperCase());
			}
		}
		DBG.trace(Debug.DEBUG, "<--queue_list_to_vector");
	}

/*---------------------------------------------------------------------*/
/* Update the recipients of a message to say who got assigned the call */
/*---------------------------------------------------------------------*/
	void tellRecipients(TrotsMsgItem tm, TrotsUser tu)
	{
		int numRecipients;
		TrotsUser recip;

		DBG.trace(Debug.DEBUG, "--> tellRecipients");

		numRecipients = tm.recipients.size();

		DBG.trace(Debug.MINOR,new StringBuilder("[").append(numRecipients).append("] recipients to update").toString());

		try
		{
			for(int i=0; i<numRecipients; i++)
			{
				recip = tm.recipients.get(i);

				DBG.trace(Debug.MINOR,"Sending TROTS_ASSIGN_SEND");
				DBG.trace(Debug.DEBUG,"To " + recip.userName);
				recip.dataOut.writeInt(Constants.TROTS_ASSIGN_SEND);
				recip.dataOut.writeInt(tm.id);

				if(recip.id == tu.id)
				{
					recip.dataOut.writeBytes("Call is assigned to you\n");
				}
				else
				{
					recip.dataOut.writeBytes(new StringBuilder("Call is assigned to ").append(tu.userName).append("\n").toString());
				}
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR,"Unable to update client ("+exIO.getMessage()+")");
		}
		DBG.trace(Debug.DEBUG, "<--tellRecipients");
	}

/*-----------------------------------------------------*/
/* Remove the client from the list of registered users */
/*-----------------------------------------------------*/
	void deregisterClient()
	{
		int userID;
		TrotsUser tu;
		DBG.trace(Debug.DEBUG, "--> deregisterClient");

		try
		{
			DBG.trace(Debug.MINOR,"Waiting for User ID");
			userID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("User ID [").append(userID).append("]").toString());
			DBG.trace(Debug.MINOR,"Locating user ID");
			tu = findUser(userID);

			if(tu==null)
			{
				DBG.trace(Debug.MAJOR,"Unable to locate user ID");
			}
			else
			{
				DBG.trace(Debug.MINOR,"Closing down data streams");
				tu.textIn.close();
				tu.dataIn.close();
				tu.dataOut.close();
				users.remove(tu);
				DBG.trace(Debug.MAJOR,"Client deregistered");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR,"Can't deregister client ("+exIO.getMessage()+")");
		}
		DBG.trace(Debug.DEBUG, "<-- deregisterClient");
	}

/*------------------------------------------------*/
/* Add the client to the list of registered users */
/*------------------------------------------------*/
	void registerClient()
	{
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> registerClient");

		DBG.trace(Debug.MINOR, "Creating new trots user");
		tu = new TrotsUser();

		DBG.trace(Debug.MINOR, "Generating client ID");
		while(isDuplicateUser(tu.id)) tu.id++;

		DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(tu.id).toString());

		try
		{
			DBG.trace(Debug.MINOR,"Sending ID to client");
			dataOut.writeInt(tu.id);

			DBG.trace(Debug.MINOR,"Waiting for user name");
			tu.userName = textIn.readLine();
			DBG.trace(Debug.DEBUG,new StringBuilder("User Name = ").append(tu.userName).toString());

			DBG.trace(Debug.MINOR,"Waiting for phone number");
			tu.phoneNo = textIn.readLine();
			DBG.trace(Debug.DEBUG,new StringBuilder("Phone No = ").append(tu.phoneNo).toString());

			DBG.trace(Debug.MINOR,"Waiting for queue list");
			tu.queueList = textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("Queue List = ").append(tu.queueList).toString());

			DBG.trace(Debug.MINOR,"Waiting for Client version");
			tu.version = textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client Version = ").append(tu.version).toString());

			DBG.trace(Debug.MINOR,"Assigning input/output streams");
			tu.dataIn = dataIn;
			tu.dataOut = dataOut;
			tu.textIn = textIn;

			DBG.trace(Debug.MINOR, "Obtaining client host name");
			tu.hostname = client.getInetAddress().getHostName();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client Host Name = ").append(tu.hostname).toString());

			DBG.trace(Debug.MINOR,"Adding user to list of clients");
                    users.add(tu.id - 1, tu);
			DBG.trace(Debug.MAJOR,"Client registered");

		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR,"Unable to process CLIENT_REGISTER (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG,"<--registerClient");
	}

/*----------------------------------------------*/
/* Send the TROTS message to the specified user */
/*----------------------------------------------*/
	public void sendMessage(TrotsUser user, TrotsMsgItem msg)
	{
		TrotsUser sender;
		String from;

		DBG.trace(Debug.DEBUG, "--> sendMessage");

		try
		{

			sender = findUser(msg.sender);
			if(sender == null)
			{
				from = new String("Unknown");
			}
			else
			{
				from = new StringBuilder(sender.userName).append(" - ").append(sender.phoneNo).toString();
			}

			DBG.trace(Debug.MINOR,"Sending TROTS_MESSAGE_RECV");
			user.dataOut.writeInt(Constants.TROTS_MESSAGE_RECV);
			DBG.trace(Debug.DEBUG,new StringBuilder("Sending Message ID [").append(msg.id).append("]").toString());
			user.dataOut.writeInt(msg.id);
			DBG.trace(Debug.DEBUG,new StringBuilder("Sending From [").append(from).append("]").toString());
			user.dataOut.writeBytes(new StringBuilder(from).append("\n").toString());
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending Message Text [").append(msg.msgText).append("]").toString());
			user.dataOut.writeBytes(new StringBuilder(msg.msgText).append("\n").toString());

			DBG.trace(Debug.MINOR,"Waiting for client response");
			DBG.trace(Debug.DEBUG,new StringBuilder("Response [").append(user.dataIn.readInt()).append("]").toString());

			DBG.trace(Debug.MAJOR, "TROTS_MESSAGE_RECV sent");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send TROTS_MESSAGE_RECV to " + user.userName + "(" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- sendMessage");
	}

/*----------------------------------------*/
/* Process the TROTS_MESSAGE_SEND details */
/*----------------------------------------*/
	public void processTrotsMessage()
	{
		TrotsMsgItem tm;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processTrotsMessage");

		DBG.trace(Debug.MAJOR,"Creating new trots message");
		tm = new TrotsMsgItem();

		DBG.trace(Debug.MINOR,"Generating message ID");
		while(isDuplicateMsg(tm.id)) tm.id++;

		DBG.trace(Debug.DEBUG, "Message ID = " + tm.id);

		try
		{

			DBG.trace(Debug.MINOR, "Waiting for user ID");
			tm.sender = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("User ID = ").append(tm.sender).toString());

			DBG.trace(Debug.MINOR, "Waiting for queue name");
			tm.queue = textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("Queue Name = ").append(tm.queue).toString());

			DBG.trace(Debug.DEBUG, "Updating statistics");
			serverStats.add(tm.queue);
			serverStats.sent(tm.queue);

			DBG.trace(Debug.MINOR, "Waiting for message text");
			tm.msgText = textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("Message Text = ").append(tm.msgText).toString());

			DBG.trace(Debug.MINOR, "Sending message ID");
			dataOut.writeInt(tm.id);

			DBG.trace(Debug.MINOR, "Finding recipients");
			for(int i = 0; i < users.size(); i++)
			{
				tu = users.get(i);
				DBG.trace(Debug.DEBUG, new StringBuilder("Checking [").append(tu.userName).append("] Queues [").append(tu.queueList).append("]").toString());
				if(tu.isUserQueue(tm.queue) && tu.available)
				{
					DBG.trace(Debug.DEBUG, new StringBuilder("Adding [").append(tu.userName).append("]").toString());
					tm.addUser(tu);
					sendMessage(tu,tm);
				}
			}
			DBG.trace(Debug.MINOR,"Adding message to list of messages");
			msgs.add(tm);

		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_MESSAGE_SEND (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processTrotsMessage");
	}

/*--------------------------------*/
/* Process a TROTS_CANCEL message */
/*--------------------------------*/
	void processTrotsCancel()
	{
		int msgID;
		TrotsMsgItem tm;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processTrotsCancel");
		DBG.trace(Debug.MAJOR, "Cancelling trots message");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for message ID");
			msgID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Message ID = ").append(msgID).toString());
			DBG.trace(Debug.MINOR, "Sending ACK");
			dataOut.writeInt((Constants.ACK));


			DBG.trace(Debug.MINOR,"Locating message");
			tm = findMsg(msgID);

			if(tm == null)
			{
				DBG.trace(Debug.MAJOR, "Unable to find message (Non-fatal)");
				DBG.trace(Debug.DEBUG, "<--processTrotsCancel");
				return;
			}
			
			serverStats.cancelled(tm.queue);

/*---------------------------------------------------------------------------*/
/* Because the TROTS_CANCEL came from another connection we need to tell the */
/* sender to stop listening to the main connection for a response            */
/*---------------------------------------------------------------------------*/

			DBG.trace(Debug.MINOR,"Locating sender");
			tu = findUser(tm.sender);

			if(tu != null)
			{
				try
				{
					DBG.trace(Debug.MINOR, "Sending TROTS_CANCEL to sender");
					tu.dataOut.writeInt(Constants.TROTS_CANCEL);
					DBG.trace(Debug.MINOR, new StringBuilder("Sending message ID [").append(tm.id).append("] to sender").toString());
					tu.dataOut.writeInt(tm.id);
				}
				catch(IOException exIO)
				{
					DBG.trace(Debug.ERROR, "Unable to send TROTS_CANCEL to sender (" + exIO.getMessage() + ")");
				}
			}
			else
			{
				DBG.trace(Debug.MAJOR, "Unable to locate sender (Non-fatal)");
			}

			DBG.trace(Debug.MINOR, "Processing recipients");
			for(int i = 0 ; i < tm.recipients.size(); i++)
			{
				tu = tm.recipients.get(i);
				DBG.trace(Debug.DEBUG, new StringBuilder("Processing [").append(tu.userName).append("]").toString());
				try
				{
					DBG.trace(Debug.DEBUG, "Sending TROTS_CANCEL");
					tu.dataOut.writeInt(Constants.TROTS_CANCEL);
					DBG.trace(Debug.DEBUG, "Sending message id");
					tu.dataOut.writeInt(msgID);
				}
				catch(IOException exIO)
				{
					DBG.trace(Debug.ERROR, "Unable to send TROTS_CANCEL (" + exIO.getMessage() + ")");
				}
			}

			DBG.trace(Debug.MINOR, "Removing message from list of messages");
			msgs.remove(tm);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_CANCEL (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processTrotsCancel");
	}

/*-------------------------------------------------*/
/* Tell the message sender who picked this call up */
/*-------------------------------------------------*/
	void tellSender(TrotsMsgItem tm, TrotsUser tu)
	{
		TrotsUser sender;

		DBG.trace(Debug.DEBUG, "--> tellSender");

		try
		{
			DBG.trace(Debug.MINOR,"Locating sender details");
			sender = findUser(tm.sender);

			if(sender==null)
			{
				DBG.trace(Debug.MINOR,new StringBuilder("Unable to locate User ID [").append(+tm.sender).append("]").toString());
				DBG.trace(Debug.DEBUG, "<--tellSender");
				return;
			}

			DBG.trace(Debug.DEBUG,new StringBuilder("Sender is [").append(sender.userName).append("]").toString());

			DBG.trace(Debug.MINOR,"Sending TROTS_ASSIGN_SEND to sender");
			sender.dataOut.writeInt(Constants.TROTS_ASSIGN_SEND);
			DBG.trace(Debug.MINOR,new StringBuilder("Sending message id [").append(tm.id).append("]").toString());
			sender.dataOut.writeInt(tm.id);
			DBG.trace(Debug.MINOR, new StringBuilder("Sending [").append(tu.userName).append("] to sender").toString());
			sender.dataOut.writeBytes(new StringBuilder("Call accepted by ").append(tu.userName).append(" - ").append(tu.phoneNo).append("\n").toString());

			DBG.trace(Debug.MINOR, "Waiting for server response");
			DBG.trace(Debug.DEBUG, new StringBuilder("Server response [").append(sender.dataIn.readInt()).append("]").toString());
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send TROTS_ASSIGN_SEND to sender (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<--tellSender");
	}

/*--------------------------------*/
/* Process a CLIENT_RESET message */
/*--------------------------------*/
	public void processClientReset()
	{
		int clientID;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processClientReset");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for client ID");
			clientID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(clientID).toString());

			DBG.trace(Debug.MINOR, "Sending ACK response");
			dataOut.writeInt(Constants.ACK);

			DBG.trace(Debug.MINOR, "Locating client");
			tu = findUser(clientID);

			if(tu == null)
			{
				DBG.trace(Debug.MAJOR, "Unable to locate client ID (Non-fatal)");
				DBG.trace(Debug.DEBUG, "<-- processClientReset");
				return;
			}

			DBG.trace(Debug.DEBUG, new StringBuilder("User Name = ").append(tu.userName).toString());
			DBG.trace(Debug.MINOR, "Sending CLIENT_RESET to client");
			
			tu.dataOut.writeInt(Constants.CLIENT_RESET);

			DBG.trace(Debug.MINOR, "Waiting for ACK");
			DBG.trace(Debug.DEBUG, new StringBuilder("Client responded with ").append(tu.dataIn.readInt()).toString());

			DBG.trace(Debug.MINOR, "De-registering client");
			tu.textIn.close();
			tu.dataIn.close();
			tu.dataOut.close();
			users.remove(tu);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process CLIENT_RESET (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processClientReset");
	}

	void shutdownClient(TrotsUser tu)
	{
		DBG.trace(Debug.DEBUG, "-->shutdownClient");
		
		if(tu != null)
		{
			try
			{
				DBG.trace(Debug.MINOR, "Sending CLIENT_SHUTDOWN");
				tu.dataOut.writeInt(Constants.CLIENT_SHUTDOWN);
				DBG.trace(Debug.MINOR, "Closing input/output streams");
				tu.dataOut.close();
				tu.dataIn.close();
				tu.textIn.close();
			}
			catch(IOException exIO)
			{
				DBG.trace(Debug.ERROR, "Unable to close client");
			}
			DBG.trace(Debug.MINOR, "Removing client entry");
			users.remove(tu);
		}

		DBG.trace(Debug.DEBUG, "<--shutdownClient");
	}

/*-----------------------------------*/
/* Process a CLIENT_SHUTDOWN request */
/*-----------------------------------*/
	void processClientShutdown()
	{
		int userID;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "-->processClientShutdown");
		try
		{
			DBG.trace(Debug.MINOR, "Waiting for client ID");
			userID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(userID).toString());
			
			DBG.trace(Debug.MINOR, "Locating client ID");
			tu = findUser(userID);

			if(tu != null)
			{
				if(tu.queueList.equalsIgnoreCase(Constants.CALL_RECEIPT_QUEUE))
				{
					DBG.trace(Debug.MAJOR, "User is a call receipt user. Will not send shutdown");
				}
				else
				{
					shutdownClient(tu);
				}
			}
			else
			{
				DBG.trace(Debug.MINOR, "Unable to locate client ID");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process CLIENT_SHUTDOWN");
		}
		DBG.trace(Debug.DEBUG, "<--processClientShutdown");
	}

/*---------------------------------*/
/* Process a CLIENT_STATUS message */
/*---------------------------------*/
	public void processClientStatus()
	{
		int clientID, status;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processClientStatus");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for client ID");
			clientID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(clientID).toString());

			DBG.trace(Debug.MINOR, "Waiting for client status");
			status = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client Status = ").append(status).toString());

			DBG.trace(Debug.MINOR, "Sending ACK response");
			dataOut.writeInt((Constants.ACK));

			DBG.trace(Debug.MINOR, "Locating client");
			tu = findUser(clientID);

			if(tu == null)
			{
				DBG.trace(Debug.MAJOR, "Unable to locate client ID (Non-fatal)");
				DBG.trace(Debug.DEBUG, "<--processClientStatus");
				return;
			}

			DBG.trace(Debug.DEBUG, new StringBuilder("User Name = ").append(tu.userName).toString());
			DBG.trace(Debug.MAJOR, "Setting user availability");
			tu.available = (status > 0 ? false : true);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process CLIENT_STATUS (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG,"<--processClientStatus");
	}

/*--------------------------------*/
/* Process a TROTS_ASSIGN_RECV message */
/*--------------------------------*/
	public void processTrotsAssign()
	{
		int msgID, userID;
		TrotsMsgItem tm;
		TrotsUser tu;
		DBG.trace(Debug.DEBUG, "--> processTrotsAssign");
		DBG.trace(Debug.MAJOR, "Processing TROTS_ASSIGN_RECV");

		try
		{
			DBG.trace(Debug.MINOR,"Waiting for Message ID");
			msgID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Message ID = ").append(msgID).toString());

			DBG.trace(Debug.MINOR, "Waiting for User ID");
			userID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("User ID = ").append(userID).toString());

			DBG.trace(Debug.MINOR, "Sending ACK response");
			dataOut.writeInt((Constants.ACK));
			dataOut.flush();

			DBG.trace(Debug.MINOR, new StringBuilder("Locating Message ID [").append(msgID).append("]").toString());

			tm = findMsg(msgID);

			if(tm == null)
			{
				DBG.trace(Debug.MAJOR, new StringBuilder("Message ID [").append(msgID).append("] could not be found (Non-fatal)").toString());
				DBG.trace(Debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			if(tm.flag == Constants.MSG_PROCESSED)
			{
				DBG.trace(Debug.MINOR, new StringBuilder("Message [").append(msgID).append("] already processed").toString());
				DBG.trace(Debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			DBG.trace(Debug.MINOR, "Setting message to processed");
			tm.flag = Constants.MSG_PROCESSED;

			DBG.trace(Debug.MINOR, new StringBuilder("Locating User ID [").append(userID).append("]").toString());
			tu = findUser(userID);

			if(tu == null)
			{
				DBG.trace(Debug.MAJOR, new StringBuilder("User [").append(userID).append("] could not be found").toString());
				DBG.trace(Debug.DEBUG, "<-- processTrotsAssign");
				return;
			}

			DBG.trace(Debug.DEBUG, new StringBuilder("User ID [").append(userID).append("] is [").append(tu.userName).append("]").toString());

			serverStats.accepted(tm.queue);

			DBG.trace(Debug.MINOR, "Telling sender who picked this up");
			tellSender(tm, tu);

			DBG.trace(Debug.MINOR, "Telling other recipients who picked this up");
			tellRecipients(tm, tu);

			DBG.trace(Debug.MINOR, "Removing message from list of messages");
			msgs.remove(tm);

		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_ASSIGN (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processTrotsAssign");
	}
	
/*------------------------------*/
/* Process a LIST_USERS request */
/*------------------------------*/
	public void processListUsers()
	{
		List<TrotsUser> userCopy;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processListUsers");
		DBG.trace(Debug.MAJOR, "Sending list of user names");
		userCopy = new ArrayList<>(users);
		try
		{
			for(int i=0; i< userCopy.size(); i++)
			{
				tu = userCopy.get(i);
				if(i > 0) dataOut.writeBytes(",");
				dataOut.writeBytes(new StringBuilder("").append(tu.id).append("#").append(tu.userName).toString());
			}
			dataOut.writeBytes("\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process LIST_USERS");
		}
		DBG.trace(Debug.DEBUG, "<-- processListUsers");
	}

	
/*---------------------------------------------------------------------*/
/* Check that all users are still talking to us by sending a heartbeat */
/* if not then we need to clean them up                                */
/*---------------------------------------------------------------------*/
	public void processCleanUsers()
	{
		List<TrotsUser> userCopy;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> processCleanUsers");
		DBG.trace(Debug.MAJOR, "Checking all user connections");
		userCopy = new ArrayList<>(users);
		for(int i=0; i< userCopy.size(); i++)
		{
			tu = userCopy.get(i);

			try
			{
				DBG.trace(Debug.DEBUG, "Checking " + tu.userName + " ("+tu.id+")");
				tu.dataOut.writeInt(Constants.HEARTBEAT);
				tu.dataIn.readInt();
			}
			catch(IOException exIO)
			{
				DBG.trace(Debug.ERROR, "Unable to talk to " + tu.userName + " ("+tu.id+") [" + exIO.getMessage() +"] - Removing connection");
				try
				{
					tu.dataIn.close();
					tu.dataOut.close();
					tu.textIn.close();
				}
				catch(Exception ex) {}
				users.remove(tu);
			}
		}
		try
		{
			DBG.trace(Debug.DEBUG, "Sending ACK to client");
			dataOut.writeInt(Constants.ACK);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send ACK in response to CLEAN_USERS");
		}
		DBG.trace(Debug.DEBUG, "<-- processCleanUsers");
	}

/*-------------------------------------*/
/* Respond to a SERVER_VERSION command */
/*-------------------------------------*/
	public void processServerVersion()
	{
		DBG.trace(Debug.DEBUG, "--> processServerVersion");
		DBG.trace(Debug.MAJOR, "Sending server version");
		try
		{
			DBG.trace(Debug.MINOR, "Sending revision");
			DBG.trace(Debug.DEBUG, "$Revision: 6 $");
			dataOut.writeBytes("$Revision: 6 $\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process SERVER_VERSION");
		}
		DBG.trace(Debug.DEBUG, "<-- processServerVersion");
	}

/*-------------------------------------*/
/* Detail the current server operation */
/*-------------------------------------*/
	public void processServerStatusHTML()
	{
		List<TrotsUser> userCopy;
            List<TrotsMsgItem> messageCopy;
		TrotsUser tu;
		TrotsMsgItem tm;
		BSumTime bst = new BSumTime();
		Date date;
		String queueName;


		DBG.trace(Debug.DEBUG, "--> processServerStatusHTML");

		DBG.trace(Debug.MAJOR, "Dumping server status as HTML");

		DBG.trace(Debug.DEBUG, "Setting GMT0BST time zone");
		TimeZone.setDefault( bst );
		SimpleDateFormat dtf = bst.getDateTimeFormatter();

		userCopy = new ArrayList<>(users);
		messageCopy = new ArrayList<>(msgs);

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
					queueName = currentQueues.get(i);
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
					tu = userCopy.get(i);
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
					tm = messageCopy.get(i);
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
		DBG.trace(Debug.DEBUG, "<-- processServerStatusHTML");
	}

/*-------------------------------*/
/* Process a QUEUE_NAMES request */
/*-------------------------------*/
	public void processQueueNames()
	{
		DBG.trace(Debug.DEBUG, "--> processQueueNames");
		DBG.trace(Debug.MAJOR, "Sending queue names");
		try
		{
			dataOut.writeBytes(queue_vector_to_list()+"\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process QUEUE_NAMES (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<--processQueueNames");
	}

/*---------------------------------*/
/* Process a QUEUE_DETAILS command */
/*---------------------------------*/
	public void processQueueDetails()
	{
		String queueName;

		DBG.trace(Debug.DEBUG, "-->processQueueDetails");
		try
		{
			DBG.trace(Debug.MINOR, "Waiting for queue name");
			queueName = textIn.readLine();
			queueName = queueName.toUpperCase();
			DBG.trace(Debug.DEBUG, new StringBuilder("Queue Name = ").append(queueName).toString());

			if(currentQueues.contains(queueName))
			{
				dataOut.writeBytes(serverStats.getQueueStats(queueName));
			}
			
			dataOut.writeBytes("\n");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process QUEUE_DETAILS (" + exIO.getMessage() +")");
		}
		DBG.trace(Debug.DEBUG, "<--processQueueDetails");
	}

/*------------------------------*/
/* Process an ADD_QUEUE command */
/*------------------------------*/
	public void processAddQueue()
	{
		String new_queue;
		DBG.trace(Debug.DEBUG, "--> processAddQueue");

		try
		{
			new_queue = textIn.readLine();
			new_queue = new_queue.toUpperCase();
			DBG.trace(Debug.MAJOR, new StringBuilder("Adding queue [").append(new_queue).append("]").toString());
			if( ! currentQueues.contains(new_queue) )
			{
				currentQueues.add(new_queue);
				serverStats.add(new_queue);
				props.put("queues",queue_vector_to_list());
				props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on ADD_QUEUE");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process ADD_QUEUE (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processAddQueue");
	}

/*--------------------------------*/
/* Process a USER_DETAILS command */
/*--------------------------------*/
	public void processUserDetails()
	{
		int userID;
		TrotsUser tu;
		BSumTime bst = new BSumTime();

		DBG.trace(Debug.DEBUG, "--> processUserDetails");
		DBG.trace(Debug.MAJOR, "Listing user details");

		DBG.trace(Debug.DEBUG, "Setting GMT0BST time zone");
		TimeZone.setDefault( bst );

		SimpleDateFormat dtf = bst.getDateTimeFormatter();

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for user ID");
			userID = dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(userID).toString());

			tu = findUser(userID);

			if(tu == null) userID = 0;

			DBG.trace(Debug.MINOR, "Sending user ID back as acknowledgment");
			DBG.trace(Debug.DEBUG, new StringBuilder("Client ID = ").append(userID).toString());
			dataOut.writeInt(userID);

			if(userID > 0)
			{
				Date date = new Date( tu.createTime * 1000 );
				DBG.trace(Debug.MINOR, "Sending User Name");
				DBG.trace(Debug.DEBUG, new StringBuilder("User Name = ").append(tu.userName).toString());
				dataOut.writeBytes(tu.userName);
				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Phone Number");
				DBG.trace(Debug.DEBUG, new StringBuilder("Phone Number = ").append(tu.phoneNo).toString());
				dataOut.writeBytes(tu.phoneNo);
				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Connection Time");
				DBG.trace(Debug.DEBUG, dtf.format(date) );
				dataOut.writeBytes(dtf.format(date) );
				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Availability");
				DBG.trace(Debug.DEBUG, "Availability = " + (tu.available ? "Yes" : "NO"));
				dataOut.writeBytes((tu.available ? "Yes" : "NO"));
				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Queue List");
				DBG.trace(Debug.DEBUG, tu.queueList);
				dataOut.writeBytes(tu.queueList);

				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Client Host Name");
				DBG.trace(Debug.DEBUG, tu.hostname);
				dataOut.writeBytes(tu.hostname);

				dataOut.writeBytes("#");
				DBG.trace(Debug.MINOR, "Sending Client Version");
				DBG.trace(Debug.DEBUG, tu.version);
				dataOut.writeBytes(tu.version);

				dataOut.writeBytes("\n");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process USER_DETAILS (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processUserDetails");
	}

/*-----------------------------*/
/* Process a DEL_QUEUE command */
/*-----------------------------*/
	public void processDelQueue()
	{
		String del_queue;
		DBG.trace(Debug.DEBUG, "--> processDelQueue");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for queue name");
			del_queue = textIn.readLine();
			del_queue = del_queue.toUpperCase();
			DBG.trace(Debug.DEBUG, new StringBuilder("Queue Name = ").append(del_queue).toString());
			if( currentQueues.contains(del_queue) )
			{
				DBG.trace(Debug.MAJOR, new StringBuilder("Deleting queue [").append(del_queue).append("]").toString());
				DBG.trace(Debug.MINOR, "Removing statistics");
				serverStats.remove(del_queue);
				DBG.trace(Debug.MINOR, "Removing queue from list");
				currentQueues.remove(del_queue);
				DBG.trace(Debug.MINOR, "Updating queue property in ini file");
				props.put("queues",queue_vector_to_list());
				props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEL_QUEUE");
			}
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process DEL_QUEUE (" + exIO.getMessage() + ")");
		}
		DBG.trace(Debug.DEBUG, "<-- processDelQueue");
	}

/*-------------*/
/* Constructor */
/*-------------*/
	public TrotsServer(int port)
	{
		int command = Constants.CLIENT_NULL;
		TrotsUser tu;

		DBG.trace(Debug.DEBUG, "--> TrotsServer (Constructor)");

		users = new ArrayList<>();
		msgs = new ArrayList<>();
		currentQueues = new ArrayList<>();

		DBG.trace(Debug.MAJOR, "Initialising counters");
		serverStats = new AllStats();

		queue_list_to_vector();

		startTime = System.currentTimeMillis() / 1000;

		try
		{
/*--------------------------*/
/* Create the server socket */
/*--------------------------*/
			DBG.trace(Debug.MAJOR, new StringBuilder("Creating socket on port ").append(port).toString());
			server = new ServerSocket(port);

			while(command != Constants.SERVER_SHUTDOWN && command != Constants.ALL_SHUTDOWN)
			{
				try
				{
/*-------------------------*/
/* Wait for client command */
/*-------------------------*/
					DBG.trace(Debug.MINOR, "Waiting for client connection");
					client = server.accept();

/*----------------------------*/
/* Build input/output streams */
/*----------------------------*/
					DBG.trace(Debug.MINOR, "Creating input/output streams");
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
					DBG.trace(Debug.MINOR, "Waiting for client command");
					command = dataIn.readInt();

					DBG.trace(Debug.DEBUG, new StringBuilder("Received command [").append(command).append("]").toString());

					switch(command)
					{
						case Constants.CLIENT_REGISTER:
							DBG.trace(Debug.MAJOR,"CLIENT_REGISTER");
							registerClient();
							break;
						case Constants.CLIENT_DEREGISTER:
							DBG.trace(Debug.MAJOR, "CLIENT_DEREGISTER");
							deregisterClient();
							client.close();
							break;
						case Constants.SERVER_SHUTDOWN:
							DBG.trace(Debug.ERROR, "SERVER_SHUTDOWN");
							break;
						case Constants.ALL_SHUTDOWN:
							DBG.trace(Debug.ERROR, "ALL_SHUTDOWN");
							break;
						case Constants.SERVER_VERSION:
							DBG.trace(Debug.MAJOR, "SERVER_VERSION");
							processServerVersion();
							break;
						case Constants.TROTS_MESSAGE_SEND:
							DBG.trace(Debug.MAJOR,"TROTS_MESSAGE_SEND");
							processTrotsMessage();
							break;
						case Constants.TROTS_ASSIGN_RECV:
							DBG.trace(Debug.MAJOR, "TROTS_ASSIGN_RECV");
							processTrotsAssign();
							break;
						case Constants.CLIENT_STATUS:
							DBG.trace(Debug.MAJOR, "CLIENT_STATUS");
							processClientStatus();
							break;
						case Constants.TROTS_CANCEL:
							DBG.trace(Debug.MAJOR, "TROTS_CANCEL");
							processTrotsCancel();
							break;
						case Constants.QUEUE_NAMES:
							DBG.trace(Debug.MAJOR, "QUEUE_NAMES");
							processQueueNames();
							break;
						case Constants.DEBUG_MAJOR:
							DBG.trace(Debug.ERROR, "Debug level set to MAJOR");
							DBG.setLevel(Debug.MAJOR);
							props.put("debug",""+Debug.MAJOR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_MAJOR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(Debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_MINOR:
							DBG.trace(Debug.ERROR, "Debug level set to MINOR");
							DBG.setLevel(Debug.MINOR);
							props.put("debug",""+Debug.MINOR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_MINOR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(Debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_DEBUG:
							DBG.trace(Debug.ERROR, "Debug level set to DEBUG");
							DBG.setLevel(Debug.DEBUG);
							props.put("debug",""+Debug.DEBUG);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_DEBUG");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(Debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.DEBUG_ERROR:
							DBG.trace(Debug.ERROR, "Debug level set to ERROR");
							DBG.setLevel(Debug.ERROR);
							props.put("debug",""+Debug.ERROR);
							try
							{
								props.store(new FileOutputStream(Constants.SERVER_INI_FILE), "Updated automatically on DEBUG_ERROR");
							}
							catch(IOException exPropPut)
							{
								DBG.trace(Debug.ERROR, "Unable to update properties file");
							}
							break;
						case Constants.STATUS_HTML:
							DBG.trace(Debug.MAJOR, "STATUS_HTML");
							processServerStatusHTML();
							break;
						case Constants.CLIENT_RESET:
							DBG.trace(Debug.MAJOR, "CLIENT_RESET");
							processClientReset();
							break;
						case Constants.ADD_QUEUE:
							DBG.trace(Debug.MAJOR, "ADD_QUEUE");
							processAddQueue();
							break;
						case Constants.DEL_QUEUE:
							DBG.trace(Debug.MAJOR, "DEL_QUEUE");
							processDelQueue();
							break;
						case Constants.CLEAN_USERS:
							DBG.trace(Debug.MAJOR, "CLEAN_USERS");
							processCleanUsers();
							break;
						case Constants.HEARTBEAT:
							DBG.trace(Debug.MAJOR, "HEARTBEAT");
							dataOut.writeInt(Constants.ACK);
							break;
						case Constants.LIST_USERS:
							DBG.trace(Debug.MAJOR, "LIST_USERS");
							processListUsers();
							break;
						case Constants.DEBUG_DETAILS:
							DBG.trace(Debug.MAJOR, "DEBUG_DETAILS");
							dataOut.writeInt(DBG.getLevel());
							break;
						case Constants.USER_DETAILS:
							DBG.trace(Debug.MAJOR, "USER_DETAILS");
							processUserDetails();
							break;
						case Constants.CLIENT_SHUTDOWN:
							DBG.trace(Debug.MAJOR, "CLIENT_SHUTDOWN");
							processClientShutdown();
							break;
						case Constants.QUEUE_DETAILS:
							DBG.trace(Debug.MAJOR, "QUEUE_DETAILS");
							processQueueDetails();
							break;
					}
					System.gc();
				}
				catch(IOException exIO)
				{
					DBG.trace(Debug.ERROR, "Unable to maintain connection with client ("+exIO.getMessage()+")");
				}
			}

/*-------------------------------------------------------------------*/
/* Close the client connections cleanly if we are closing everything */
/*-------------------------------------------------------------------*/
			if(command == Constants.ALL_SHUTDOWN)
			{
				int numUsers = users.size();
				DBG.trace(Debug.MINOR, "Closing down ["+numUsers+"] users");

				for(int i=0;i < numUsers; i++)
				{
                                   tu = users.get(0);
					DBG.trace(Debug.DEBUG, "Closing down [ " + tu.userName + "]");
					shutdownClient(tu);
				}
			}

			DBG.trace(Debug.ERROR, "Normal Completion");
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to listen to client connections ("+exIO.getMessage()+")");
			DBG.trace(Debug.ERROR, "Abnormal termination");
		}
	}

	public static void main(String argv[])
	{

		String propServerPort, propQueues;
		DBG = new Debug(0, "server.dbg", "TROTS SERVER ($Revision: 6 $) DEBUG TRACE");

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

			DBG.trace(Debug.MAJOR,"Properties loaded");
			DBG.trace(Debug.DEBUG,"Port = " + propServerPort);
			DBG.trace(Debug.DEBUG,"Queues = " + propQueues);
			DBG.trace(Debug.DEBUG,"Debug Level = " + props.getProperty("debug"));

            new TrotsServer(Integer.parseInt(propServerPort));

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
