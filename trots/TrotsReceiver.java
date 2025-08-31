/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.14  99/07/19  10:21:05  10:21:05  dkelly (Dave Kelly)
Added About box
Added code to send revision.

Revision 1.13  99/07/15  10:22:31  10:22:31  dkelly (Dave Kelly)
Added code to display OK dialog box when the client has been shutdown

Revision 1.12  99/07/15  09:04:11  09:04:11  dkelly (Dave Kelly)
Dunno

Revision 1.11  99/07/13  13:30:04  13:30:04  dkelly (Dave Kelly)
Removed sending of dummy data for call receipt user

Revision 1.10  99/07/13  12:51:05  12:51:05  dkelly (Dave Kelly)
Multiple changes

Move host / port information to frame from the title bar
Changed most concatenated strings to StringBuilder

Revision 1.9  99/07/05  15:53:19  15:53:19  dkelly (Dave Kelly)
Forced checkin 

Revision 1.8  99/07/01  15:19:53  15:19:53  dkelly (Dave Kelly)
Fixed typo that caused compilation error

Revision 1.7  99/07/01  13:18:34  13:18:34  dkelly (Dave Kelly)
General code cleanup.
Removed privates and statics

Revision 1.6  99/04/30  09:50:40  09:50:40  dkelly (Dave Kelly)
Cleaned up number handling for Debug level
Set title bar to display the current host

Revision 1.5  99/04/29  13:50:44  13:50:44  dkelly (Dave Kelly)
Added code to respond to HEARTBEAT server command

Revision 1.4  99/04/22  16:29:48  16:29:48  dkelly (Dave Kelly)
Dunno

Revision 1.3  99/04/19  10:31:35  10:31:35  dkelly (Dave Kelly)
Forced

Revision 1.2  99/02/26  12:24:55  12:24:55  dkelly (Dave Kelly)
Added RCS statements
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/*------------------------------------*/
/* Class to receive the TROTS message */
/*------------------------------------*/
public class TrotsReceiver implements IniFileListener, WindowListener
{
	List<TrotsMessage> oldMsgs,trotsMessages;
	int clientID, pServerPort;
	TrotsMessage	 	newMessage;
	String				pServer, pQueues, pUserName, pUserPhone;
	Frame 				statusFrame;
	CountdownLabel		lblConnect;
	Label					lblServerInfo;
	boolean				available;
	static Debug		DBG;
	DataConnection		client;
	static IniFileGen	ini;
	OKDialog				dlgAbout;

        @Override
        public void windowOpened(WindowEvent e) {}
        @Override
        public void windowClosing(WindowEvent e)
	{
		DBG.trace(Debug.MAJOR,"Window Closed");
		exitProgram();
	}
        @Override
        public void windowClosed(WindowEvent e) {}
        @Override
        public void windowDeactivated(WindowEvent e) {}
        @Override
        public void windowDeiconified(WindowEvent e) {}
        @Override
        public void windowIconified(WindowEvent e) {}
        @Override
        public void windowActivated(WindowEvent e) {}

	void exitProgram()
	{
		deregisterClient();
		DBG.trace(Debug.ERROR,"Normal Termination");
		System.exit(0);
	}
	public void reloadIniProps()
	{
		int pDebug;
		DBG.trace(Debug.ERROR,"Ini File Settings");
		DBG.trace(Debug.ERROR,"-----------------");
		DBG.trace(Debug.ERROR,new StringBuilder("Server Name :").append(ini.getProperty("server")).toString());
		DBG.trace(Debug.ERROR,new StringBuilder("Port        :").append(ini.getProperty("port")).toString());
		DBG.trace(Debug.ERROR, new StringBuilder("User Name   :").append(ini.getProperty("user")).toString());
		DBG.trace(Debug.ERROR, new StringBuilder("Phone No    :").append(ini.getProperty("phone")).toString());
		DBG.trace(Debug.ERROR, new StringBuilder("Debug Level :").append(ini.getProperty("debug")).toString());

		pServer = ini.getProperty("server");
                pServerPort = Integer.parseInt(ini.getProperty("port"));
		pUserName = ini.getProperty("user");
		pUserPhone = ini.getProperty("phone");
		pQueues = ini.getProperty("queues");
		try
		{
                        pDebug = Integer.parseInt(ini.getProperty("debug"));
		}
		catch(NumberFormatException exNumFmt)
		{
			DBG.trace(Debug.ERROR, "Debug Level is not a number, setting to 0");
			pDebug = 0;
		}
		DBG.setLevel(pDebug);
	}
		
/*-------------------------------------------------------------------*/
/* The ini file has been changed so we need to re-set the connection */
/*-------------------------------------------------------------------*/
        @Override
        public void iniFileUpdated()
        {
		DataConnection d;
		String oldServer = pServer, oldUser = pUserName, oldQueues = pQueues;
		String oldPhone = pUserPhone;
		int oldPort = pServerPort;

		DBG.trace(Debug.MAJOR, "Ini file has changed");

/*---------------------------------*/
/* Re-read the ini file properties */
/*---------------------------------*/
		DBG.trace(Debug.MINOR, "Reloading INI properties");
		reloadIniProps();

/*----------------------------------------------------------------------*/
/* Because the receiver is listening on another channel, we need to get */
/* the server to tell it to break the connection and try again          */
/*----------------------------------------------------------------------*/
		try
		{
			DBG.trace(Debug.MINOR, new StringBuilder("Creating connection to [").append(oldServer).append(":").append(oldPort).append("]").toString());
			d = new DataConnection(oldServer, oldPort);
			
			DBG.trace(Debug.MINOR, "Sending CLIENT_RESET");
			d.dataOut.writeInt(Constants.CLIENT_RESET);
			DBG.trace(Debug.MINOR, "Sending client id");
			d.dataOut.writeInt(clientID);
	
			DBG.trace(Debug.MINOR, "Waiting for ACK response");
			DBG.trace(Debug.DEBUG, new StringBuilder("Server responded with ").append(d.dataIn.readInt()).toString());

			d.close();
			lblServerInfo.setText( new StringBuilder(pServer).append(":").append(pServerPort).toString());
			statusFrame.repaint();
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send CLIENT_RESET");
		}
		
	}

/*--------------------------------------------------*/
/* Checks to see if we have a duplicate Instance ID */
/*--------------------------------------------------*/
        boolean isIDExists(int id)
        {
                List<TrotsMessage> copy = new ArrayList<>(trotsMessages);

                DBG.trace(Debug.DEBUG, new StringBuilder("--> isIDExists(").append(id).append(")").toString());
                if(id == 0) return true;
                for(TrotsMessage currentMessage : copy)
                {
                        if(currentMessage.instanceID == id)
                                return true;
                }
                return false;
        }

/*--------------------------------------------------*/
/* Send the CLIENT_DEREGISTER message to the server */
/*--------------------------------------------------*/
	void deregisterClient()
	{
		DataConnection d;

		DBG.trace(Debug.DEBUG, "--> deregisterClient");
		DBG.trace(Debug.MAJOR, "Deregistering client");
		try
		{
			DBG.trace(Debug.MINOR, "Making connection to host");
			d = new DataConnection(pServer, pServerPort);

			DBG.trace(Debug.MINOR, "Sending CLIENT_DEREGISTER");
			d.dataOut.writeInt(Constants.CLIENT_DEREGISTER);
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending User ID [").append(clientID).append("]").toString());
			d.dataOut.writeInt(clientID);

			DBG.trace(Debug.MINOR, "Closing down socket connection");
			d.close();
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send CLIENT_DEREGISTER (" + exIO.getMessage() + ")");
		}
	}

/*----------------------------------------------*/
/* Send the CLIENT_STATUS message to the server */
/*----------------------------------------------*/
	void sendStatus(int userID, boolean state)
	{
		DataConnection d;

		DBG.trace(Debug.DEBUG, "--> sendStatus");
		DBG.trace(Debug.MAJOR, "Sending client status");

		try
		{
			DBG.trace(Debug.MINOR, "Making connection to host");
			d = new DataConnection(pServer, pServerPort);

			DBG.trace(Debug.MINOR, "Sending CLIENT_STATUS");
			d.dataOut.writeInt(Constants.CLIENT_STATUS);
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending User ID [").append(userID).append("]").toString());
			d.dataOut.writeInt(userID);
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending Status [").append(state).append("]").toString());
			d.dataOut.writeInt((state ? 0 : 1));

			DBG.trace(Debug.MINOR, "Waiting for server response");
			DBG.trace(Debug.DEBUG, new StringBuilder("Server responded with [").append(d.dataIn.readInt()).append("]").toString());

			DBG.trace(Debug.MINOR, "Closing down socket connection");
			d.close();
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send CLIENT_STATUS ("+exIO.getMessage()+")");
		}
	}

/*---------------------------------------------*/
/* Send the TROTS_ASSIGN message to the server */
/*---------------------------------------------*/
	void sendAssign(int msgID, int userID)
	{
		Socket s;
		DataOutputStream send;
		DataInputStream recv;

		DBG.trace(Debug.DEBUG, "-->sendAssign");
		DBG.trace(Debug.MAJOR, "Assign to Me");

		try
		{
			DBG.trace(Debug.MINOR, "Making connection to host");
			s = new Socket(pServer, pServerPort);

			DBG.trace(Debug.MINOR, "Creating data input/output streams");
			send = new DataOutputStream(s.getOutputStream());
			recv = new DataInputStream(s.getInputStream());

			DBG.trace(Debug.MINOR, "Sending TROTS_ASSIGN_RECV");
			send.writeInt(Constants.TROTS_ASSIGN_RECV);
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending Message ID [").append(msgID).append("]").toString());
			send.writeInt(msgID);
			DBG.trace(Debug.DEBUG, new StringBuilder("Sending User ID [").append(userID).append("]").toString());
			send.writeInt(userID);

			DBG.trace(Debug.MINOR, "Waiting for server response");
			DBG.trace(Debug.DEBUG, new StringBuilder("Server responded with [").append(recv.readInt()).append("]").toString());
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to send TROTS_ASSIGN ("+exIO.getMessage()+")");
		}
	}

/*--------------------------------------------------------------------*/
/* Define ActionListener to handle all events from the trots messages */
/*--------------------------------------------------------------------*/
        ActionListener buttonListener = new ActionListener()
        {
                @Override
                public void actionPerformed(ActionEvent e)
		{
			String actionCommand = e.getActionCommand();
			String actionType, IDString;
			int commandLen = actionCommand.length();
			int trotsID;

			actionType = actionCommand.substring(0,1);
			IDString = actionCommand.substring(1);
                    trotsID = Integer.parseInt(IDString);

/*---------------------------------------------------------------*/
/* If the dismiss button has been pressed then remove the window */
/*---------------------------------------------------------------*/
			if(actionType.equals("D"))
			{
				DBG.trace(Debug.MAJOR, new StringBuilder("Dismiss button pressed [").append(trotsID).append("]").toString());
				if(isIDExists(trotsID)) killTrots(trotsID);
			}
			if(actionType.equals("A"))
			{
				DBG.trace(Debug.MAJOR, new StringBuilder("Assign button pressed [").append(trotsID).append("]").toString());
				sendAssign(trotsID, clientID);
			}
		}
	};

/*-----------------------------------*/
/* Action Listener for status dialog */
/*-----------------------------------*/
        ActionListener menuListener = new ActionListener()
        {
                @Override
                public void actionPerformed(ActionEvent e)
		{
			String command = e.getActionCommand();
			
			DBG.trace(Debug.DEBUG, new StringBuilder("Action [").append(command).append("]").toString());
			if(command.equalsIgnoreCase("Exit"))
			{
				DBG.trace(Debug.DEBUG,"Exit option selected");
				exitProgram();
			}

			if(command.equalsIgnoreCase("Config"))
			{
				ini.show();
			}

			if(command.equalsIgnoreCase("About"))
			{
				dlgAbout.setVisible(true);
			}
		}
	};

/*---------------------------------*/
/* Item Listener for status dialog */
/*---------------------------------*/
        ItemListener statusItemListener = new ItemListener()
        {
                @Override
                public void itemStateChanged(ItemEvent e)
		{
			DBG.trace(Debug.MAJOR, "Status change");
			DBG.trace(Debug.DEBUG, new StringBuilder("Change status from ").append(available).append(" to ").append(!available).toString());
			available = !available;
			sendStatus(clientID, available);
			lblConnect.setText((available ? Constants.LISTENING : Constants.NOT_LISTENING));
		}
	};

/*----------------------------------------------*/
/* Build the basic control box for the receiver */
/*----------------------------------------------*/
	void buildControlBox(String title)
	{
		MenuBar	optMenuBar = new MenuBar();
		Menu		fileMenu = new Menu("File");
		MenuItem	configOption, exitOption;
		MenuItem aboutOption;
		CheckboxMenuItem availableOption;



		DBG.trace(Debug.DEBUG, "--> buildControlBox");
		DBG.trace(Debug.MAJOR, "Creating receiver box");
		statusFrame = new Frame("Trots Receiver");
		statusFrame.addWindowListener(this);

		statusFrame.setLayout(new GridLayout(2,1));

		statusFrame.setForeground(Color.black);
		statusFrame.setBackground(Color.lightGray);


		optMenuBar.add(fileMenu);

		configOption = new MenuItem("Config");
		aboutOption = new MenuItem("About Trots Receiver...");
		aboutOption.setActionCommand("About");
		exitOption = new MenuItem("Exit");
		availableOption = new CheckboxMenuItem("Available", true);

		fileMenu.add(configOption);
		fileMenu.add(availableOption);
		fileMenu.addSeparator();
		fileMenu.add(aboutOption);
		fileMenu.addSeparator();
		fileMenu.add(exitOption);

		statusFrame.setMenuBar(optMenuBar);
		
		dlgAbout = new OKDialog(statusFrame,"About Trots Receiver",true,"Trots Receiver $Revision: 6 $\nWritten by Dave Kelly\n(C)Hewlett-Packard 1999");

		lblServerInfo = new Label(title);
		lblServerInfo.setAlignment(Label.CENTER);

		lblConnect = new CountdownLabel(30);
		lblConnect.setAlignment(Label.CENTER);

		statusFrame.add(lblServerInfo);
		statusFrame.add(lblConnect);

		availableOption.addItemListener(statusItemListener);
		configOption.addActionListener(menuListener);
		aboutOption.addActionListener(menuListener);
		exitOption.addActionListener(menuListener);

		statusFrame.setSize(500,150);
		statusFrame.setLocation(new Point());

            statusFrame.setVisible(true);
	}

		
/*----------------------------*/
/* Find the specified message */
/*----------------------------*/
        TrotsMessage findMsg(int id)
        {
                List<TrotsMessage> copy = new ArrayList<>(trotsMessages);

                DBG.trace(Debug.DEBUG, new StringBuilder("--> findMsg(").append(id).append(")").toString());

                for(TrotsMessage t : copy)
                {
                        if(t.instanceID == id)
                        {
                                return t;
                        }
                }
                return null;
        }

/*------------------------------------------------------------------*/
/* Kill the appropriate trots message and remove it from the vector */
/*------------------------------------------------------------------*/
	void killTrots(int id)
	{
		TrotsMessage t;

		DBG.trace(Debug.DEBUG, new StringBuilder("--> killTrots(").append(id).append(")").toString());
		DBG.trace(Debug.MAJOR, "Killing trots message");
		DBG.trace(Debug.MINOR, new StringBuilder("Locating Message Id [").append(id).append("]").toString());
		t = findMsg(id);

		if(t == null)
		{
			DBG.trace(Debug.MAJOR, "Unable to locate message ID");
			return;
		}

		t.kill();
		DBG.trace(Debug.MINOR, "Removing the message from the vector");
                trotsMessages.remove(t);
                oldMsgs.add(t);
	}

/*------------------*/
/* Reset the client */
/*------------------*/
	void processClientReset()
	{
		DBG.trace(Debug.DEBUG, "--> processClientReset");
		DBG.trace(Debug.MINOR, "Processing CLIENT_RESET");

		try
		{
			DBG.trace(Debug.MINOR, "Sending ACK");
			client.dataOut.writeInt(Constants.ACK);

			DBG.trace(Debug.MAJOR, "Closing client connection");
			client.close();

			DBG.trace(Debug.MAJOR, "Making new connection");
			lblServerInfo.setText(new StringBuilder(pServer).append(":").append(pServerPort).toString());
			statusFrame.repaint();
			makeServerConnection(pUserName, pUserPhone, pQueues);
			
			sendStatus(clientID, available);


			DBG.trace(Debug.DEBUG, "<-- processClientReset");

		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process CLIENT_RESET");
		}

		DBG.trace(Debug.DEBUG, "<-- processClientReset");
	}

/*---------------------------------------------------*/
/* Update the trots message with the new information */
/*---------------------------------------------------*/
	void processTrotsAssign()
	{
		TrotsMessage tm;
		String extraText;
		int id;
	
		DBG.trace(Debug.DEBUG, "--> processTrotsAssign");
		DBG.trace(Debug.MAJOR, "Processing trots assign message");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for message ID");
			id = client.dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Message ID = ").append(id).toString());

			DBG.trace(Debug.MINOR, "Waiting for extra text");
			extraText = client.textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("Extra Text [").append(extraText).append("]").toString());
			
			tm = findMsg(id);

			if(tm == null)
			{
				DBG.trace(Debug.MAJOR, "Unable to locate message");
				return;
			}

			DBG.trace(Debug.MINOR, "Updating trots message");
			tm.update(extraText);

			class killThread extends Thread
			{
				int msgID;
				public killThread(int msg_to_kill)
				{
					this.msgID = msg_to_kill;
				}

				public void run()
				{
					DBG.trace(Debug.MINOR, new StringBuilder("Will kill message in ").append(Constants.MESSAGE_TIMEOUT).append(" seconds").toString());
					try
					{
						Thread.sleep(Constants.MESSAGE_TIMEOUT * 1000);
					} catch (InterruptedException exInt) {}
					DBG.trace(Debug.MINOR, "Killing Trots message from thread");
					killTrots(msgID);
				}
			};
			killThread th = new killThread(id);
			th.start();
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_ASSIGN ("+exIO.getMessage()+")");
		}
	}

	void createTrotsMessage(int ID, String strUser, String strMessage)
	{

		DBG.trace(Debug.DEBUG, "--> createTrotsMessage");
		DBG.trace(Debug.MAJOR, "Creating trots message");
                newMessage.setDetails(ID, strUser, strMessage);
                trotsMessages.add(newMessage);
                newMessage.show();

                if(!oldMsgs.isEmpty())
                {
                        newMessage = oldMsgs.get(0);
                        newMessage.reset();
                        oldMsgs.remove(newMessage);
                }
                else
                {
                        newMessage = new TrotsMessage(buttonListener);
                }

                newMessage.hide();

	}

	void processTrotsMessage()
	{
		String userName, messageText;
		int msgID;

		DBG.trace(Debug.DEBUG, "--> processTrotsMessage");
		DBG.trace(Debug.MAJOR, "Processing trots message");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for message ID");
			msgID = client.dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("MSG ID = ").append(msgID).toString());

			DBG.trace(Debug.MINOR, "Waiting for user name");
			userName = client.textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("USER NAME = ").append(userName).toString());

			DBG.trace(Debug.MINOR, "Waiting for message Text");
			messageText = client.textIn.readLine();
			DBG.trace(Debug.DEBUG, new StringBuilder("MESSAGE TEXT = ").append(messageText).toString());

			DBG.trace(Debug.MINOR, "Sending ACK response back to server");
			client.dataOut.writeInt(Constants.ACK);

			createTrotsMessage(msgID, userName, messageText);

		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_MESSAGE_RECV ("+exIO.getMessage()+")");
		}
	}

	void processTrotsCancel()
	{
		int msgID;
	
		DBG.trace(Debug.DEBUG, "--> processTrotsCancel");
		DBG.trace(Debug.MAJOR, "Processing trots cancel");

		try
		{
			DBG.trace(Debug.MINOR, "Waiting for message ID");
			msgID = client.dataIn.readInt();
			DBG.trace(Debug.DEBUG, new StringBuilder("Message ID = ").append(msgID).toString());

			killTrots(msgID);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process TROTS_CANCEL ("+ exIO.getMessage() + ")");
		}
	}

	public void processHeartbeat()
	{
		DBG.trace(Debug.DEBUG, "-->processHeartbeat");
		DBG.trace(Debug.MAJOR, "Responding to server heartbeat");

		try
		{
			DBG.trace(Debug.DEBUG, "Sending ACK");
			client.dataOut.writeInt(Constants.ACK);
		}
		catch(IOException exIO)
		{
			DBG.trace(Debug.ERROR, "Unable to process HEARTBEAT ("+exIO.getMessage()+")");
		}
	}

	public void makeServerConnection(String u, String p, String q)
	{
		boolean stopProcessing = false;

		while(!stopProcessing)
		{
			DBG.trace(Debug.MAJOR, new StringBuilder("Making server connection to [").append(pServer).append(":").append(pServerPort).append("]").toString());;
			lblServerInfo.setText(new StringBuilder(pServer).append(":").append(pServerPort).toString());
			statusFrame.repaint();
			try
			{
				lblConnect.setText(Constants.CONNECTING);
				DBG.trace(Debug.MINOR, "Making server connection");
				client = new DataConnection(pServer, pServerPort);

				DBG.trace(Debug.MINOR, "Registering client");
				client.dataOut.writeInt(Constants.CLIENT_REGISTER);
				clientID = client.dataIn.readInt();
				DBG.trace(Debug.DEBUG, "Server assigned ID = " + clientID);
				DBG.trace(Debug.MINOR, "Sending User Name");
				DBG.trace(Debug.DEBUG, "User Name = " + u);
				client.dataOut.writeBytes(u + "\n");
				DBG.trace(Debug.MINOR, "Sending Phone Number");
				DBG.trace(Debug.DEBUG, "Phone Number = " + p);
				client.dataOut.writeBytes(p + "\n");
				DBG.trace(Debug.MINOR, "Sending Queue List");
				DBG.trace(Debug.DEBUG, "Queue List = " + q);
				client.dataOut.writeBytes(q + "\n");
				DBG.trace(Debug.MINOR, "Sending Client Version");
				DBG.trace(Debug.DEBUG, "Client Version = $Revision: 6 $");
				client.dataOut.writeBytes("$Revision: 6 $\n");

				stopProcessing = true;
			}
			catch(IOException exIO)
			{
				DBG.trace(Debug.ERROR, "Unable to make server connection ("+exIO.getMessage()+")");
				try
				{
					statusFrame.getToolkit().beep();
					lblConnect.setPrefix(Constants.LOST_CONNECTION);
					lblConnect.reset(Constants.CONNECTION_RETRY_TIME);
					lblConnect.go();
					Thread.sleep(Constants.CONNECTION_RETRY_TIME * 1000);
					lblConnect.stop();
				}
				catch(InterruptedException exInt) {}
			}
		}
		DBG.trace(Debug.MAJOR, "Connection made");
	}

	public TrotsReceiver()
	{
		int serverCommand;

		DBG.trace(Debug.DEBUG, "--> TrotsReceiver (Constructor)");

		available = true;

                DBG.trace(Debug.DEBUG, "Creating new messages vector");
                trotsMessages = new ArrayList<>();

                DBG.trace(Debug.DEBUG, "Creating old messages vector");
                oldMsgs = new ArrayList<>();

		DBG.trace(Debug.DEBUG, "Adding observer to ini file generator");
		reloadIniProps();
            ini.addListener(this);

/*-----------------------*/
/* Build the control box */
/*-----------------------*/
		buildControlBox(new StringBuilder(pServer).append(":").append(pServerPort).toString());

		newMessage = new TrotsMessage(buttonListener);
		newMessage.hide();

/*--------------------------------------------------------------------*/
/* This routine will not return until a successful connection is made */
/*--------------------------------------------------------------------*/
		makeServerConnection(pUserName, pUserPhone,  pQueues);
		sendStatus(clientID, available);

		serverCommand = Constants.CLIENT_NULL;
		while(serverCommand != Constants.CLIENT_SHUTDOWN)
		{
			lblConnect.setText((available ? Constants.LISTENING : Constants.NOT_LISTENING));
			try
			{
				DBG.trace(Debug.MAJOR, "Waiting for server command");
				serverCommand = client.dataIn.readInt();

				switch(serverCommand)
				{
					case Constants.TROTS_MESSAGE_RECV:
						DBG.trace(Debug.MAJOR, "TROTS_MESSAGE_RECV");
						processTrotsMessage();
						break;
					case Constants.TROTS_ASSIGN_SEND:
						DBG.trace(Debug.MAJOR, "TROTS_ASSIGN_SEND");
						processTrotsAssign();
						break;
					case Constants.CLIENT_SHUTDOWN:
						DBG.trace(Debug.MAJOR, "CLIENT_SHUTDOWN");
						break;
					case Constants.TROTS_CANCEL:
						DBG.trace(Debug.MAJOR, "TROTS_CANCEL");
						processTrotsCancel();
						break;
					case Constants.CLIENT_RESET:
						DBG.trace(Debug.MAJOR, "CLIENT_RESET");
						processClientReset();
						break;
					case Constants.HEARTBEAT:
						DBG.trace(Debug.MAJOR, "HEARTBEAT");
						processHeartbeat();
						break;
				}
			}
			catch(IOException exIO)
			{
				lblServerInfo.setText(new StringBuilder(pServer).append(":").append(pServerPort).toString());
				statusFrame.repaint();
				makeServerConnection(pUserName, pUserPhone, pQueues);
				sendStatus(clientID, available);
			}
			// System.gc();
		}
		OKDialog okd = new OKDialog(statusFrame, "Shutdown", true, "Trots Receiver has been shutdown from the server");
		statusFrame.setState(Frame.NORMAL);
                okd.setVisible(true);
		DBG.trace(Debug.ERROR,"Normal termination");
		System.exit(0);
	}

	public static void main(String argv[])
	{
		String iniFileName =	System.getProperty("user.home") +
									System.getProperty("file.separator") +
									Constants.TROTS_INI_FILE;
		String propServerName, propServerPort, propUserName, propQueues;
		String propArray[][]={
                     		   {"Server Name", "server"},
               		         {"Port Number", "port"},
										{"User Name", "user"},
										{"Phone No", "phone"},
										{"Queues", "queues"},
										{"Debug Level", "debug"}
									};
		int tempDebug = 0;

		DBG = new Debug(0, "recv.dbg", "TROTS RECEIVER ($Revision: 6 $) DEBUG TRACE");

		ini = new IniFileGen("Receiver Information", iniFileName, propArray);

/*---------------------------------------------------------------*/
/* If the INI file did not exist in the first place then show it */
/*---------------------------------------------------------------*/
		if(! ini.OK) ini.show();

		if(! ini.OK)
		{
			for(int i = 0; i < propArray.length; i++)
			{
				if(ini.getProperty(propArray[i][1]) == null)
				{
					DBG.trace(Debug.ERROR, "Property ["+propArray[i][1]+"] is not set");	
				}
			}
			DBG.trace(Debug.ERROR, "Properties were not set. Abnormal termination");
			System.exit(1);
		}

		if(ini.getProperty("debug") != null)
		{
			try
			{
                            tempDebug = Integer.parseInt(ini.getProperty("debug"));
			}
			catch(NumberFormatException exNumFmt)
			{
				DBG.trace(Debug.ERROR, "Debug Level is not a number, setting to 0");
				tempDebug = 0;
			}
			DBG.setLevel(tempDebug);
		}

		TrotsReceiver tr = new TrotsReceiver();
	}
}
