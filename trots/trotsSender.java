/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.19  99/07/26  11:33:15  11:33:15  dkelly (Dave Kelly)
Fixed problem where Config option remained disabled

Revision 1.18  99/07/20  17:13:13  17:13:13  dkelly (Dave Kelly)
Stopped status window being cleared too soon after a call has been accepted.

Revision 1.17  99/07/19  10:21:26  10:21:26  dkelly (Dave Kelly)
Added about box.
Added code to send revision

Revision 1.16  99/07/15  16:36:24  16:36:24  dkelly (Dave Kelly)
Removed REMOVE_CALL_RECEIPT

Revision 1.15  99/07/15  09:17:28  09:17:28  dkelly (Dave Kelly)
Added Reset button to clear the fields

Revision 1.14  99/07/13  15:11:53  15:11:53  dkelly (Dave Kelly)
Added check in REMOVE_CALL_RECEIPT for data coming back

Revision 1.13  99/07/13  14:58:41  14:58:41  dkelly (Dave Kelly)
Added REMOVE_CALL_RECEIPT to specifically deregister call receipt client
Set messageID = 0 when call is accepted

Revision 1.12  99/07/13  13:29:08  13:29:08  dkelly (Dave Kelly)
Removed sending of additional call receipt data when deregistering

Revision 1.11  99/07/13  12:52:45  12:52:45  dkelly (Dave Kelly)
Changed title bar to "Trots Sender"

Revision 1.10  99/07/05  15:54:01  15:54:01  dkelly (Dave Kelly)
Forced checkin after benchmarking

Revision 1.9  99/07/02  14:53:46  14:53:46  dkelly (Dave Kelly)
Java 1.2 fix. Can't remember !

Revision 1.8  99/07/02  14:01:53  14:01:53  dkelly (Dave Kelly)
Java 1.2

Clarified List to be java.awt.List

Revision 1.7  99/07/02  10:11:09  10:11:09  dkelly (Dave Kelly)
General code cleanup
Removed privates and statics

Revision 1.6  99/04/30  09:10:54  09:10:54  dkelly (Dave Kelly)
Added deregister when timeout occurs

Revision 1.5  99/04/23  09:45:36  09:45:36  dkelly (Dave Kelly)
Fixed sizing issues

Revision 1.4  99/04/22  16:29:55  16:29:55  dkelly (Dave Kelly)
Added resize of menu for team list

Revision 1.3  99/04/19  10:31:41  10:31:41  dkelly (Dave Kelly)
Forced

Revision 1.2  99/02/26  12:24:56  12:24:56  dkelly (Dave Kelly)
Added RCS statements

*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

/*---------------------------------*/
/* Class to send the TROTS message */
/*---------------------------------*/
public class trotsSender implements Observer,WindowListener
{
	Socket client;
	DataOutputStream	clientDataOut;
	DataInputStream	clientDataIn;
	BufferedReader		clientTextIn;
	static iniFileGen ini;
	Frame sendFrame;
	java.awt.List queueList;
	countdownLabel timer;
	Button btnCancel, btnSend, btnReset;
	Panel	pnlButtons, pnlTextAndList;
	TextArea messageInput;
	MenuItem configOption, exitOption;
	String pServer, pUserName, pUserPhone;
	int pServerPort, pDebug, pTimeout, messageID = 0;
	static debug DBG;
	Thread queueThread;
	int listSelectedItem = -2, clientID = 0;
	OKDialog dlgAbout;
	boolean noQueueUpdate;

	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e)
	{
		DBG.trace(debug.MAJOR, "Window closed");
		exitProgram();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}

	void exitProgram()
	{
		timer.stop();
		sendTrotsCancel(messageID);
		sendDeregister();
		sendFrame.dispose();
		DBG.trace(debug.MAJOR, "Stopping queue name updater thread");
		if(queueThread != null) queueThread.interrupt();
		DBG.trace(debug.ERROR, "Normal termination");
		System.exit(0);
	}

	ActionListener buttonListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			String actionCommand;
			actionCommand = e.getActionCommand();
			DBG.trace(debug.MINOR, new StringBuffer("Event ").append(actionCommand).append(" occurred").toString());
			if(actionCommand.equals("Config"))
			{
				DBG.trace(debug.MAJOR,"Displaying INI file");
				ini.show();
			}
			if(actionCommand.equals("Send"))
			{
				DBG.trace(debug.DEBUG, new StringBuffer("Starting timer [").append(pTimeout).append("] seconds").toString());
				timer.reset(pTimeout);
				timer.go();
				DBG.trace(debug.MINOR, "Disabling Send/Reset");
				btnSend.setEnabled(false);
				btnReset.setEnabled(false);
				DBG.trace(debug.MINOR, "Enabling Cancel");
				btnCancel.setEnabled(true);

				DBG.trace(debug.MINOR, "Disabling CONFIG option on menu");
				configOption.setEnabled(false);
				Thread thread = new Thread()
				{
					public void run()
					{
						sendTrotsMessage();
					}
				};
				DBG.trace(debug.MINOR, "Starting send thread");
				thread.start();
				return;
			}
			if(actionCommand.equals("Cancel"))
			{
				DBG.trace(debug.DEBUG, "Stopping timer");
				timer.stop();
				if(messageID != 0)
				{
					sendTrotsCancel(messageID);
				}
				DBG.trace(debug.MINOR, "Enabling Send/Reset");
				btnSend.setEnabled(true);
				btnReset.setEnabled(true);

				DBG.trace(debug.MINOR, "Disabling Cancel");
				btnCancel.setEnabled(false);

				DBG.trace(debug.MINOR, "Enabling CONFIG option on menu");
				configOption.setEnabled(true);
				timer.setText("");
				return;
			}
			if(actionCommand.equals("Exit"))
			{
				DBG.trace(debug.MAJOR, "Exit selected");
				exitProgram();
			}

			if(actionCommand.equals("Reset"))
			{
				DBG.trace(debug.MINOR, "Resetting controls");
				timer.setText("");
				queueList.deselect(queueList.getSelectedIndex());
				listSelectedItem = -2;
				messageInput.setText("");
				messageID = 0;
				return;
			}

			if(actionCommand.equals("About"))
			{
				dlgAbout.setVisible(true);
			}

			return;
		}
	};

	ItemListener listListener = new ItemListener()
	{
		public void itemStateChanged(ItemEvent e)
		{
			String text = messageInput.getText();
			int colonPos;

			if(queueList.getSelectedIndex() == listSelectedItem)
			{
				queueList.deselect(queueList.getSelectedIndex());
				colonPos = text.indexOf(":");
				text = text.substring(colonPos + 1);
				messageInput.setText(text);
			}
			else
			{
				colonPos = text.indexOf(":");
				text = text.substring(colonPos + 1);
				messageInput.setText(new StringBuffer(queueList.getSelectedItem()).append(":").append(text).toString());
			}
			listSelectedItem = queueList.getSelectedIndex();
		}
	};

	public void reloadIniProps()
	{
		int tempDebug = 0, tempTimeout = 0;

		DBG.trace(debug.ERROR,"Ini File Settings");
		DBG.trace(debug.ERROR,"-----------------");
		DBG.trace(debug.ERROR,new StringBuffer("Server Name :").append(ini.getProperty("server")).toString());
		DBG.trace(debug.ERROR,new StringBuffer("Port        :").append(ini.getProperty("port")).toString());
		DBG.trace(debug.ERROR,new StringBuffer("User Name   :").append(ini.getProperty("user")).toString());
		DBG.trace(debug.ERROR,new StringBuffer("Timeout     :").append(ini.getProperty("msgtimeout")).toString());
		DBG.trace(debug.ERROR,new StringBuffer("Debug Level :").append(ini.getProperty("debug")).toString());

		pServer = ini.getProperty("server");
		pServerPort = new Integer(ini.getProperty("port")).intValue();
		pUserName = ini.getProperty("user");
		pUserPhone = ini.getProperty("phone");

		if(ini.getProperty("debug") != null)
		{
			try
			{
				tempDebug = new Integer(ini.getProperty("debug")).intValue();
			}
			catch(NumberFormatException exNumFmt)
			{
				DBG.trace(debug.ERROR, "Debug Level is not a number, setting to 0");
				tempDebug = 0;
			}
		}

		if(ini.getProperty("msgtimeout") != null)
		{
			try
			{
				tempTimeout = new Integer(ini.getProperty("msgtimeout")).intValue();
			}
			catch(NumberFormatException exNumFmt)
			{
				DBG.trace(debug.ERROR, new StringBuffer("Message Timeout is not a number, setting to ").append(Constants.SENDER_TIMEOUT).toString());
				tempTimeout = Constants.SENDER_TIMEOUT;
			}
		}

		pDebug = tempDebug;
		pTimeout = tempTimeout;

		DBG.setLevel(pDebug);
	}


/*-------------------------------------------------------------------*/
/* The ini file has been changed so we need to re-set the connection */
/*-------------------------------------------------------------------*/
	public void update(Observable o, Object arg)
	{
		DBG.trace(debug.MAJOR, "Ini file has changed");

/*---------------------------------*/
/* Re-read the ini file properties */
/*---------------------------------*/
		DBG.trace(debug.MINOR, "Reloading INI properties");
		reloadIniProps();
	}

	void buildWindow()
	{

		MenuBar mb;
		Menu fileMenu;
		MenuItem aboutOption;

		DBG.trace(debug.DEBUG, "--> buildWindow");
		DBG.trace(debug.MAJOR, "Creating send window");
		sendFrame = new Frame("Trots Sender");
		
		sendFrame.addWindowListener(this);

		sendFrame.setLayout(new BorderLayout());

		sendFrame.setForeground(Color.black);
		sendFrame.setBackground(Color.lightGray);

		mb = new MenuBar();
		fileMenu = new Menu("File");

		mb.add(fileMenu);
		
		configOption = new MenuItem("Config");
		configOption.addActionListener(buttonListener);
		aboutOption = new MenuItem("About Trots Sender...");
		aboutOption.addActionListener(buttonListener);
		aboutOption.setActionCommand("About");
		exitOption = new MenuItem("Exit");
		exitOption.addActionListener(buttonListener);

		dlgAbout = new OKDialog(sendFrame,"About Trots Sender",true,"Trots Sender $Revision: 6 $\nWritten by Dave Kelly\n(C)Hewlett-Packard 1999");

		fileMenu.add(configOption);
		fileMenu.addSeparator();
		fileMenu.add(aboutOption);
		fileMenu.addSeparator();
		fileMenu.add(exitOption);

		sendFrame.setMenuBar(mb);
		pnlButtons = new Panel();
		pnlTextAndList = new Panel();

		btnCancel = new Button("Cancel");
		btnReset = new Button("Reset");
		btnSend = new Button("Send");

		btnCancel.addActionListener(buttonListener);
		btnReset.addActionListener(buttonListener);
		btnSend.addActionListener(buttonListener);

		timer = new countdownLabel(20);
		timer.setPrefix("Time remaining: ");

		pnlButtons.add(btnSend);
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnReset);

		btnCancel.setEnabled(false);

		queueList = new java.awt.List();
		queueList.setMultipleMode(false);
		queueList.addItemListener(listListener);
		queueList.setForeground(Color.black);
		queueList.setBackground(Color.white);

		messageInput = new TextArea("", 7,50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		messageInput.setForeground(Color.black);
		messageInput.setBackground(Color.white);

		pnlTextAndList.setLayout(new BorderLayout());
		pnlTextAndList.add("Center", messageInput);
		pnlTextAndList.add("East", queueList);
		sendFrame.add("North", pnlTextAndList);
		sendFrame.add("Center", pnlButtons);
		sendFrame.add("South", timer);

		timer.setAlignment(Label.CENTER);
		timer.setForeground(Color.white);
		timer.setBackground(Color.darkGray);

		getQueueNames();

		sendFrame.pack();
		sendFrame.show();
	}

	void getQueueNames()
	{
		String queues;

		DBG.trace(debug.DEBUG, "--> getQueueNames");
		DBG.trace(debug.MAJOR, "Getting queue names from server");

		try
		{
			DBG.trace(debug.MINOR, "Creating socket connection");
			client = new Socket(pServer, pServerPort);

			DBG.trace(debug.MINOR, "Creating data streams");
			clientDataOut = new DataOutputStream(client.getOutputStream());
			clientTextIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

			DBG.trace(debug.MINOR, "Sending QUEUE_NAMES");
			clientDataOut.writeInt(Constants.QUEUE_NAMES);
			queues = clientTextIn.readLine();
			DBG.trace(debug.DEBUG, new StringBuffer("Queue Names = ").append(queues).toString());

			if(queues != null)
			{
				updateQueueList(queues);
				timer.setText("");
			}
			else
			{
				timer.setText("No queues were supplied from the server");
				DBG.trace(debug.ERROR, "No queues were supplied from the server");
			}
		}
		catch(IOException exIO)
		{
			timer.setText("Unable to obtain queue names from server");
			DBG.trace(debug.ERROR, "Cannot send QUEUE_NAMES ("+exIO.getMessage()+")");
		}
	}

/*-------------------------------------------------*/
/* Parse the list of queue names (comma-separated) */
/*-------------------------------------------------*/
	void updateQueueList(String qlist)
	{
		int commaPos;
		String temp = qlist;
		String queueName;

		DBG.trace(debug.DEBUG, new StringBuffer("--> updateQueueList(").append(qlist).append(")").toString());
		DBG.trace(debug.MAJOR, "Parsing queue list");

		if(queueList.getItemCount() > 0)
		{
			DBG.trace(debug.MINOR, "Clearing existing items");
			queueList.removeAll();
		}

		while(temp.length() > 0)
		{

			commaPos = temp.indexOf(",");
			if(commaPos < 0)
			{
				queueName = temp;
				temp = new String("");
			}
			else
			{
				queueName = temp.substring(0,commaPos);
				temp = temp.substring(commaPos + 1);
			}
			DBG.trace(debug.DEBUG, new StringBuffer("Adding queue (").append(queueName).append(")").toString());
			queueList.add(queueName);
		}

		sendFrame.repaint();
	}

/*-------------------------------------------------------*/
/* Send a TROTS_CANCEL message for the specified message */
/*-------------------------------------------------------*/
	public void sendTrotsCancel(int msgID)
	{
		dataConnection d;

		DBG.trace(debug.DEBUG, "--> sendTrotsCancel");
		DBG.trace(debug.MAJOR, "Sending trots cancel");

		try
		{
			DBG.trace(debug.MINOR, "Creating socket connection");
			d = new dataConnection(pServer, pServerPort);

			DBG.trace(debug.MINOR, "Sending TROTS_CANCEL");
			d.dataOut.writeInt(Constants.TROTS_CANCEL);
			DBG.trace(debug.DEBUG, new StringBuffer("Sending Message ID [").append(msgID).append("]").toString());
			d.dataOut.writeInt(msgID);
			DBG.trace(debug.MINOR, "Waiting for server response");
			DBG.trace(debug.DEBUG, new StringBuffer("Server responded with ").append(d.dataIn.readInt()).toString());
		
			DBG.trace(debug.MINOR, "Closing connection");
			d.close();
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to send TROTS_CANCEL ("+exIO.getMessage()+")");
		}
	}

/*---------------------------------------*/
/* Deregister the client from the server */
/*---------------------------------------*/
	void sendDeregister()
	{
		dataConnection d;

		try
		{
			DBG.trace(debug.MINOR,"Creating secondary socket");
			d = new dataConnection(pServer, pServerPort);

			DBG.trace(debug.MAJOR, "Sending CLIENT_DEREGISTER");
			d.dataOut.writeInt(Constants.CLIENT_DEREGISTER);
			DBG.trace(debug.DEBUG, new StringBuffer("Sending Client ID [").append(clientID).append("]").toString());
			d.dataOut.writeInt(clientID);
			DBG.trace(debug.MINOR, "Closing connection");
			d.close();
		}
		catch(IOException exIO)
		{
			DBG.trace(debug.ERROR, "Unable to send CLIENT_DEREGISTER ("+exIO.getMessage()+")");
		}
	}
/*------------------------------------*/
/* Send a TROTS_MESSAGE communication */
/*------------------------------------*/
	public void sendTrotsMessage()
	{
		String queue, message;
		Socket primaryConnection;

		DataOutputStream tempOut;
		DataInputStream tempIn;
		BufferedReader textIn;
		
		int serverCommand;
		String serverText;
		int serverData;

		DBG.trace(debug.DEBUG, "--> sendTrotsMessage");
		DBG.trace(debug.MAJOR, "Sending trots message");

/*----------------------*/
/* Get the message text */
/*----------------------*/
		message = messageInput.getText();

/*--------------------*/
/* Get the queue name */
/*--------------------*/
		queue = queueList.getSelectedItem();

		if(queue == null)
		{
			DBG.trace(debug.MINOR, "A queue name must be selected");
			timer.stop();
			timer.setText("A queue name must be selected");
			btnCancel.setEnabled(false);
			btnSend.setEnabled(true);
			btnReset.setEnabled(true);
			configOption.setEnabled(true);
			return;
		}

		try
		{
			DBG.trace(debug.MINOR, "Creating socket connection");
			primaryConnection = new Socket(pServer, pServerPort);

			DBG.trace(debug.MINOR, new StringBuffer("Setting timeout to ").append(pTimeout).append(" seconds").toString());
			primaryConnection.setSoTimeout(pTimeout * 1000);

			DBG.trace(debug.MINOR, "Creating data input/output stream");
			clientDataOut = new DataOutputStream(primaryConnection.getOutputStream());
			clientDataIn = new DataInputStream(primaryConnection.getInputStream());
			clientTextIn = new BufferedReader(new InputStreamReader(primaryConnection.getInputStream()));

			DBG.trace(debug.MINOR, "CLIENT_REGISTER");
			clientDataOut.writeInt(Constants.CLIENT_REGISTER);

			DBG.trace(debug.MINOR, "Waiting for client ID");
			clientID = clientDataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Client ID = ").append(clientID).toString());

			DBG.trace(debug.DEBUG, new StringBuffer("Sending User Name [").append(pUserName).append("]").toString());
			clientDataOut.writeBytes(pUserName + "\n");

			DBG.trace(debug.DEBUG, new StringBuffer("Sending Phone Number [").append(pUserPhone).append("]").toString());
			clientDataOut.writeBytes(pUserPhone + "\n");

			DBG.trace(debug.DEBUG, new StringBuffer("Sending Queue List as '").append(Constants.CALL_RECEIPT_QUEUE).append("'").toString());
			clientDataOut.writeBytes(Constants.CALL_RECEIPT_QUEUE+"\n");

			DBG.trace(debug.DEBUG, new StringBuffer("Sending Client Revision: $Revision: 6 $").toString());
			clientDataOut.writeBytes("$Revision: 6 $\n");

			DBG.trace(debug.MINOR, "Creating secondary socket");
			client = new Socket(pServer, pServerPort);

			DBG.trace(debug.MINOR, "Creating secondary input/output streams");
			tempOut = new DataOutputStream(client.getOutputStream());
			tempIn = new DataInputStream(client.getInputStream());

			DBG.trace(debug.MINOR, "Sending TROTS_MESSAGE_SEND");
			tempOut.writeInt(Constants.TROTS_MESSAGE_SEND);
			DBG.trace(debug.DEBUG, new StringBuffer("Sending user ID [").append(clientID).append("]").toString());
			tempOut.writeInt(clientID);
			DBG.trace(debug.DEBUG, new StringBuffer("Sending queue name [").append(queue).append("]").toString());
			tempOut.writeBytes(queue + "\n");
			DBG.trace(debug.DEBUG, new StringBuffer("Sending Message text [").append(message).append("]").toString());
			tempOut.writeBytes(message + "\n");
			DBG.trace(debug.MINOR, "Waiting for message iD");
			messageID = tempIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Message ID = ").append(messageID).toString());

			DBG.trace(debug.MINOR, "Closing secondary input/output");
			tempOut.close();
			tempIn.close();
			client.close();

			DBG.trace(debug.MINOR, "Waiting for server response");
			serverCommand = clientDataIn.readInt();
			DBG.trace(debug.DEBUG, new StringBuffer("Server command = ").append(serverCommand).toString());

			timer.stop();
			switch(serverCommand)
			{
				case Constants.TROTS_ASSIGN_SEND:
					DBG.trace(debug.MAJOR, "TROTS_ASSIGN_SEND");
					DBG.trace(debug.DEBUG, new StringBuffer("Ignoring message id [").append(clientDataIn.readInt()).append("]").toString());
					serverText = clientTextIn.readLine();
					DBG.trace(debug.DEBUG, new StringBuffer("Server Message = ").append(serverText).toString());
					timer.setText(serverText);
					queueList.deselect(queueList.getSelectedIndex());
					listSelectedItem = -2;
					messageInput.setText("");
					messageID = 0;
					noQueueUpdate = true;
					break;
				case Constants.TROTS_CANCEL:
					DBG.trace(debug.MAJOR, "TROTS_CANCEL");
/*-----------------------*/
/* Ignore the message ID */
/*-----------------------*/
					messageID = clientDataIn.readInt();
					DBG.trace(debug.DEBUG, new StringBuffer("Message number [").append(messageID).append("]").toString());
					messageID = 0;
					break;
			}

			DBG.trace(debug.MINOR, "Sending ACK response");
			clientDataOut.writeInt(Constants.ACK);

			sendDeregister();
			primaryConnection.close();
		}
		catch(InterruptedIOException exInt)
		{
			DBG.trace(debug.MAJOR, "No response from recipients");
			timer.stop();
			timer.setText("No response from anyone");
			sendTrotsCancel(messageID);
			messageID = 0;
			sendDeregister();
		}
		catch(IOException exIO)
		{
			timer.stop();
			timer.setText("Unable to communicate with server");
			DBG.trace(debug.ERROR, "Unable to send trots message ("+exIO.getMessage()+")");
		}
		btnCancel.setEnabled(false);
		btnSend.setEnabled(true);
		btnReset.setEnabled(true);
		configOption.setEnabled(true);
	}

	public trotsSender()
	{
		DBG.trace(debug.DEBUG, "--> trotsSender (Constructor)");
		DBG.trace(debug.MAJOR, "Creating new trotsSender");
		reloadIniProps();
		DBG.trace(debug.MINOR, "Adding observer to ini file");
		ini.addObserver(this);
		buildWindow();

		noQueueUpdate = false;

		DBG.trace(debug.MAJOR, "Starting queue name updater thread");
		queueThread = new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						this.sleep(30 * 1000);
						if(queueList.getSelectedIndex() == -1 && !noQueueUpdate)
						{
							getQueueNames();
						}
						else
						{
							DBG.trace(debug.MINOR, "The list is being used at the moment. Will update later");
						}
						noQueueUpdate=false;
					}
					catch(InterruptedException exInt) {}
				}
			}
		};

		queueThread.start();
	}

	public static void main(String argv[])
	{
		String iniFileName =	System.getProperty("user.home") +
							System.getProperty("file.separator") +
							Constants.SENDER_INI_FILE;
	
		int tempDebug, tempTimeout;
		boolean iniFileOK = false;

		String propArray[][]={
                     		   {"Server Name", "server"},
               		         {"Port Number", "port"},
										{"User Name", "user"},
										{"Phone Number", "phone"},
										{"Message Timeout", "msgtimeout"},
										{"Debug Level", "debug"}
									};

		DBG = new debug(0, "sender.dbg", "TROTS SENDER ($Revision: 6 $) DEBUG TRACE");

		iniFileOK = new File(iniFileName).exists();
		ini = new iniFileGen("Sender Information", iniFileName, propArray);

/*---------------------------------------------------------------*/
/* If the INI file did not exist in the first place then show it */
/*---------------------------------------------------------------*/
		if(!iniFileOK || ! ini.OK ) ini.show();

		if(! ini.OK)
		{
			for(int i = 0; i < propArray.length; i++)
			{
				if(ini.getProperty(propArray[i][1]) == null)
				{
					DBG.trace(debug.ERROR, "Property ["+propArray[i][1]+"] is not set");	
					iniFileOK = false;
				}
			}
			DBG.trace(debug.ERROR, "Properties were not set. Abnormal termination");
			System.exit(1);
		}


		if(ini.getProperty("debug") != null)
		{
			try
			{
				tempDebug = new Integer(ini.getProperty("debug")).intValue();
			}
			catch(NumberFormatException exNumFmt)
			{
				DBG.trace(debug.ERROR, "Debug Level is not a number, setting to 0");
				tempDebug = 0;
			}
			DBG.setLevel(tempDebug);
		}

		if(ini.getProperty("msgtimeout") != null)
		{
			try
			{
				tempTimeout = new Integer(ini.getProperty("msgtimeout")).intValue();
			}
			catch(NumberFormatException exNumFmt)
			{
				DBG.trace(debug.ERROR, "msgtimeout is not a number, setting to " + Constants.SENDER_TIMEOUT);
				tempTimeout = Constants.SENDER_TIMEOUT;
			}
		}

		trotsSender ts = new trotsSender();
	}
}
