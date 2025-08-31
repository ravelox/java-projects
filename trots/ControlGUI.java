/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.5  99/07/19  10:09:27  10:09:27  dkelly (Dave Kelly)
Added code to display Server and Client versions
Added about box

Revision 1.4  99/07/16  14:09:37  14:09:37  dkelly (Dave Kelly)
Clean Users and Stop buttons are disabled when clean users is hit
All buttons (apart from Stop) are re-enabled when the function finishes.

Revision 1.3  99/07/15  16:35:39  16:35:39  dkelly (Dave Kelly)
Removed Call Receipt Admin commands

Revision 1.2  99/07/15  11:06:49  11:06:49  dkelly (Dave Kelly)
Re-added the updater thread and replaced Refresh with Start and Stop

Revision 1.1  99/07/13  12:44:34  12:44:34  dkelly (Dave Kelly)
Initial revision

*/

import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ControlGUI implements WindowListener
{
	TextField txtServer, txtPort, txtNewQueue;
	Label txtStatus;
	java.awt.List lstQueues, lstUsers;
	CheckboxGroup cbgDebug;
	Checkbox cbError, cbMajor, cbMinor, cbDebug;
	Button btnStart, btnStop, btnCleanUsers;
	Button btnShutdownServer, btnShutdownAll;
	Frame guiFrame;
	YesNoDialog dlgShutdownServer, dlgShutdownAll;
	DetailDialog dlgUserDetails = null, dlgQueueDetails = null;
	Dialog dlgAddQueue;
	OKDialog dlgAbout;
	Properties props;
	String iniFileName;

/*-----------------------*/
/* WindowListener events */
/*-----------------------*/
        @Override
        public void windowOpened(WindowEvent e) {}
        @Override
        public void windowClosing(WindowEvent e) { System.exit(0);}
        @Override
        public void windowClosed(WindowEvent e) {}
        @Override
        public void windowDeactivated(WindowEvent e) {}
        @Override
        public void windowDeiconified(WindowEvent e) { guiFrame.repaint(); }
        @Override
        public void windowIconified(WindowEvent e) {}
        @Override
        public void windowActivated(WindowEvent e) {}

/*----------------------------------------*/
/* Display red text message in status bar */
/*----------------------------------------*/
	void statusError(String message)
	{
		txtStatus.setForeground(Color.red);
		txtStatus.setText(message);
	}

/*------------------------------------------*/
/* Display green text message in status bar */
/*------------------------------------------*/
	void statusInfo(String message)
	{
		txtStatus.setForeground(Color.green);
		txtStatus.setText(message);
	}

/*--------------------------------*/
/* Listener for menus and buttons */
/*--------------------------------*/
	ActionListener eventListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			String actionCommand = e.getActionCommand();

/*-------------*/
/* Exit Option */
/*-------------*/
			if(actionCommand.equalsIgnoreCase("Exit"))
			{
				System.exit(0);
			}

/*--------------*/
/* About Dialog */
/*--------------*/
			if(actionCommand.equalsIgnoreCase("About"))
			{
				dlgAbout.setVisible(true);
				return;
			}
/*------------------*/
/* Add Queue Option */
/*------------------*/
			if(actionCommand.equalsIgnoreCase("Add Queue"))
			{
				displayAddQueue();
				return;
			}

/*--------------*/
/* Start option */
/*--------------*/
			if(actionCommand.equalsIgnoreCase("Start"))
			{
				startMonitorThread();
				return;
			}
			
/*-------------*/
/* Stop option */
/*-------------*/
			if(actionCommand.equalsIgnoreCase("Stop"))
			{
				stopMonitorThread();
				return;
			}
			
/*------------------------*/
/* Shutdown Server option */
/*------------------------*/
			if(actionCommand.equalsIgnoreCase("Shutdown Server"))
			{
				if(!serverNamePresent() ) return;
				dlgShutdownServer.setVisible(true);
				return;
			}

/*----------------------------*/
/* Shutdown Everything option */
/*----------------------------*/
			if(actionCommand.equalsIgnoreCase("Shutdown Everything"))
			{
				if(!serverNamePresent() ) return;
				dlgShutdownAll.setVisible(true);
				return;
			}

/*--------------------*/
/* Clean Users option */
/*--------------------*/
			if(actionCommand.equalsIgnoreCase("Clean Users"))
			{
				if(!serverNamePresent() ) return;
				cleanUsers();
				return;
			}

/*-------------------------------------------------------------------------*/
/* The next actions are based on the user pressing a special action button */
/* within the dialog                                                       */
/*-------------------------------------------------------------------------*/
			String name = ((Button)e.getSource()).getName();

/*-------------------------------------------------------*/
/* Shutdown the server without shutting down the clients */
/*-------------------------------------------------------*/
			if(name.equalsIgnoreCase("dlgShutdownServer"))
			{
				dlgShutdownServer.setVisible(false);
				shutdownServer();
				return;
			}

/*-------------------------------------*/
/* Shutdown the server and the clients */
/*-------------------------------------*/
			if(name.equalsIgnoreCase("dlgShutdownAll"))
			{
				dlgShutdownAll.setVisible(false);
				shutdownAll();
				return;
			}

/*------------------------------*/
/* Shutdown the selected client */
/*------------------------------*/
			if(name.equalsIgnoreCase("dlgUserDetails"))
			{
				dlgUserDetails.setVisible(false);
				dlgUserDetails.dispose();
				dlgUserDetails = null;
				shutdownClient(actionCommand);
				return;
			}

/*---------------------------*/
/* Remove the selected queue */
/*---------------------------*/
			if(name.equalsIgnoreCase("dlgQueueDetails"))
			{
				dlgQueueDetails.setVisible(false);
				dlgQueueDetails.dispose();
				dlgQueueDetails = null;
				removeQueue(actionCommand);
				return;
			}

/*-------------------------*/
/* Add the specified queue */
/*-------------------------*/
			if(name.equalsIgnoreCase("dlgAddQueue"))
			{
				dlgAddQueue.setVisible(false);
				if(actionCommand.equalsIgnoreCase("Add"))
				{
					addQueue(txtNewQueue.getText());
				}
				return;
			}
		}
	};

/*-----------------------------*/
/* Listener for Checkbox items */
/*-----------------------------*/
	ItemListener checkboxListener = new ItemListener()
	{
		public void itemStateChanged(ItemEvent e)
		{
			setDebugLevel(cbgDebug.getSelectedCheckbox());
		}
	};

/*-------------------------*/
/* Listener for list items */
/*-------------------------*/
	ActionListener listItemListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
                        String menuItem, menuName;
                        String itemID = "";

/*-------------------*/
/* Get the menu name */
/*-------------------*/
			menuItem = e.getActionCommand();
			menuName = ((java.awt.List)e.getSource()).getName();

/*-------------------------------------------------------------------------*/
/* If the menu is Users or Call Receipt then get the ID from the menu item */
/*-------------------------------------------------------------------------*/
			if(menuName.equalsIgnoreCase("Users") || menuName.equalsIgnoreCase("Call Receipt"))
			{
                                String[] parts = menuItem.split("#");
                                if(parts.length > 0) itemID = parts[0];
			}

			if(menuName.equalsIgnoreCase("Users"))
			{
				displayUserDetails(itemID);
			}

			if(menuName.equalsIgnoreCase("Queues"))
			{
				displayQueueDetails(menuItem);
			}
		}
	};

/*--------------------------------------------------------------------------*/
/* Utility function to get an integer from a string or the provided default */
/*--------------------------------------------------------------------------*/
	int makeInt(String value, int defaultValue)
	{
		int returnValue = defaultValue;

		try
		{
			returnValue = Integer.parseInt(value);
		}
		catch(NumberFormatException exNumFmt) {}

		return returnValue;
	}

/*-----------------------------------*/
/* Display the selected user details */
/*-----------------------------------*/
	void displayUserDetails(String id)
	{
		int port;
		DataConnection server;
		String userDetails;
		String detailTitles[] = {"User Name", "Phone Number", "Connection Time", "Available ?", "Queues", "Host", "Client Version"};

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.USER_DETAILS);
			server.dataOut.writeInt(makeInt(id,0));
			if(server.dataIn.readInt() > 0)
			{
				userDetails = server.textIn.readLine();
				server.close();
				dlgUserDetails = new DetailDialog(guiFrame, "User Details", detailTitles, userDetails, "Remove", "Cancel");
				dlgUserDetails.setName("dlgUserDetails", id);
				dlgUserDetails.addSpecialListener(eventListener);
				dlgUserDetails.setVisible(true);
			}
			else
			{
				statusError("The selected user does not exist on the server");
			}
		}
		catch(Exception ex)
		{
			statusError("Unable to obtain user details");
		}
	}

/*------------------------------------*/
/* Display the selected Queue details */
/*------------------------------------*/
	void displayQueueDetails(String id)
	{
		int port;
		DataConnection server;
		String queueDetails;
		String detailTitles[] = {"Queue Name", "Sent", "Accepted", "Cancelled"};

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.QUEUE_DETAILS);
			server.dataOut.writeBytes(id + "\n");
			queueDetails = server.textIn.readLine();
			server.close();
			if(queueDetails.length() > 0)
			{
				dlgQueueDetails = new DetailDialog(guiFrame, "Queue Details", detailTitles, queueDetails, "Remove", "Cancel");
				dlgQueueDetails.setName("dlgQueueDetails", id);
				dlgQueueDetails.addSpecialListener(eventListener);
				dlgQueueDetails.setVisible(true);
			}
			else
			{
				statusError("Unable to obtain queue details");
			}
		}
		catch(Exception ex)
		{
			statusError("Unable to obtain queue details");
		}
	}

/*------------------------------*/
/* Display the Add Queue dialog */
/*------------------------------*/
	void displayAddQueue()
	{
		Dimension s,d;
		Button btnAdd = new Button("Add"), btnCancel = new Button("Cancel");
		Panel pnlTop = new Panel(), pnlBottom = new Panel();

		dlgAddQueue = new Dialog(guiFrame, "Add a new queue");
		dlgAddQueue.setLayout(new BorderLayout());
		dlgAddQueue.add("North",pnlTop);
		dlgAddQueue.add("South", pnlBottom);

		txtNewQueue = new TextField(20);

		pnlTop.add( new Label("Queue Name") );
		pnlTop.add( txtNewQueue );
		pnlBottom.add(btnAdd);
		pnlBottom.add(btnCancel);

		btnAdd.addActionListener(eventListener);
		btnCancel.addActionListener(eventListener);
		btnAdd.setName("dlgAddQueue");
		btnCancel.setName("dlgAddQueue");

		dlgAddQueue.pack();
		s = Toolkit.getDefaultToolkit().getScreenSize();
		d = dlgAddQueue.getSize();
		dlgAddQueue.setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
		txtNewQueue.requestFocus();
		dlgAddQueue.setVisible(true);
	}

/*---------------------------*/
/* Remove the selected Queue */
/*---------------------------*/
	void removeQueue(String queueName)
	{
		int port;
		DataConnection server;

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);

		try
		{
			server = makeServerConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.DEL_QUEUE);
			server.dataOut.writeBytes(queueName + "\n");
			server.close();
			refreshDisplay();
		}
		catch(Exception ex)
		{
			statusError("Unable to remove queue");
		}
	}

/*------------------------------*/
/* Shutdown the selected client */
/*------------------------------*/
	void shutdownClient(String id)
	{
		int port;
		DataConnection server;

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.CLIENT_SHUTDOWN);
			server.dataOut.writeInt(makeInt(id,0));
			server.close();
			refreshDisplay();
		}
		catch(Exception exIO)
		{
			statusError("Unable to shut client down");
		}
	}

/*----------------------------*/
/* Send a CLEAN_USERS command */
/*----------------------------*/
	void cleanUsers()
	{
		int port;
		DataConnection server;

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		btnStart.setEnabled(false);
		btnCleanUsers.setEnabled(false);
		btnShutdownServer.setEnabled(false);
		btnShutdownAll.setEnabled(false);
		txtServer.setEnabled(false);
		txtPort.setEnabled(false);
		btnStop.setEnabled(false);

		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.CLEAN_USERS);
			server.dataIn.readInt();
			server.close();
			refreshDisplay();
		}
		catch(Exception exIO)
		{
			statusError("Unable to clean users");
		}

		btnStart.setEnabled(true);
		btnCleanUsers.setEnabled(true);
		btnShutdownServer.setEnabled(true);
		btnShutdownAll.setEnabled(true);
		txtServer.setEnabled(true);
		txtPort.setEnabled(true);
		btnStop.setEnabled(false);
	}

/*----------------------------------------------------------*/
/* Check to see if the server name field has any data in it */
/*----------------------------------------------------------*/
	boolean serverNamePresent()
	{
		if(txtServer.getText().length() == 0)
		{
			OKDialog noName = new OKDialog(guiFrame,"Trots Control Interface",true,"A server name must be supplied");
			noName.setVisible(true);
			return false;
		}
		return true;
	}

/*--------------------------------*/
/* Shutdown the server on its own */
/*--------------------------------*/
	void shutdownServer()
	{
		int port;
		DataConnection server;
		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.SERVER_SHUTDOWN);
		}
		catch(Exception ex)
		{
			statusError("Unable to shut server down");
		}
	}

/*------------------------------------------*/
/* Shutdown everything (clients and server) */
/*------------------------------------------*/
	void shutdownAll()
	{
		int port;
		DataConnection server;
		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		
		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.ALL_SHUTDOWN);
		}
		catch(Exception ex)
		{
			statusError("Unable to shut everything down");
		}
	}

/*-------------------------*/
/* Add the specified queue */
/*-------------------------*/
	void addQueue(String queueName)
	{
		int port;
		DataConnection server;

		if(queueName.length() == 0) return;

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);

		try
		{
			server = makeServerConnection(txtServer.getText(), port);
			server.dataOut.writeInt(Constants.ADD_QUEUE);
			server.dataOut.writeBytes(queueName + "\n");
			server.close();
			refreshDisplay();
		}
		catch(Exception ex)
		{	
			statusError("Unable to add a new queue name");
		}
	}

/*----------------*/
/* Create the GUI */
/*----------------*/
	void buildGUI()
	{
		Panel pnlTop = new Panel(), pnlBottom = new Panel();
		// Panel pnlDialogTop = new Panel(), pnlDialogBottom = new Panel();
		Panel pnlServer,pnlQueues, pnlDebug, pnlUsers;
		Panel pnlServerTop = new Panel(), pnlServerBottom = new Panel();
		MenuBar	optMenuBar = new MenuBar();
		Menu		fileMenu = new Menu("File");
		MenuItem addQueueOption,exitOption, aboutOption;
		Label lblServer = new Label("Server"), lblPort = new Label("Port");
		// NoFocusButton btnYes, btnNo;
		// Dimension s, d;
		String hostname, portNumber;
		Label lblQueues = new Label("Queues");
		Label lblUsers = new Label("Users");
		Label lblDebugging = new Label("Debugging Level");
		btnCleanUsers = new Button("Clean Users");

		btnCleanUsers.addActionListener(eventListener);

		lblQueues.setAlignment(Label.CENTER);
		lblUsers.setAlignment(Label.CENTER);
		lblDebugging.setAlignment(Label.CENTER);

		lblQueues.setBackground(Color.black);
		lblUsers.setBackground(Color.black);
		lblDebugging.setBackground(Color.black);
		lblQueues.setForeground(Color.white);
		lblUsers.setForeground(Color.white);
		lblDebugging.setForeground(Color.white);

		btnStart = new Button("Start");
		btnStop = new Button("Stop");
		btnShutdownServer = new Button("Shutdown Server");
		btnShutdownAll = new Button("Shutdown Everything");
		// btnYes = new NoFocusButton("Yes");
		// btnNo = new NoFocusButton("No");

/*---------------------------------------------*/
/* Put the server details into the text fields */
/*---------------------------------------------*/
		hostname = props.getProperty("server");
		if(hostname == null)
		{
			try
			{
				hostname = java.net.InetAddress.getLocalHost().getHostName();
			}
			catch(Exception ex) { hostname = "localhost"; }
		}
		
		portNumber = props.getProperty("port");
		if(portNumber == null) portNumber = "";

		txtServer = new TextField(hostname,30);
		txtPort = new TextField("" + makeInt(portNumber, Constants.DEFAULT_PORT),5);

		txtStatus = new Label();
		txtStatus.setBackground(Color.black);
		txtStatus.setForeground(Color.white);

		optMenuBar.add(fileMenu);
		addQueueOption = new MenuItem("Add Queue");
		aboutOption = new MenuItem("About Trots Control Interface...");
		aboutOption.setActionCommand("About");
		exitOption = new MenuItem("Exit");
		fileMenu.add(addQueueOption);
		fileMenu.addSeparator();
		fileMenu.add(aboutOption);
		fileMenu.addSeparator();
		fileMenu.add(exitOption);
		addQueueOption.addActionListener(eventListener);
		aboutOption.addActionListener(eventListener);
		exitOption.addActionListener(eventListener);

		guiFrame = new Frame("Trots Control Interface");
		guiFrame.addWindowListener(this);
		guiFrame.setSize(new Dimension(1000, 450));
		guiFrame.setMenuBar(optMenuBar);

		dlgAbout = new OKDialog(guiFrame,"About Trots Control Interface",true,"Trots Control Interface $Revision: 6 $\nWritten by Dave Kelly\n(C)Hewlett-Packard 1999");

/*------------------------------*/
/* Create some Yes / No dialogs */
/*------------------------------*/
		dlgShutdownServer = new YesNoDialog(guiFrame, "Shut Down Server", true,"Shutting the server down\nrequires it to be restarted manually.\nAre you sure ?");
		dlgShutdownServer.addYesListener(eventListener);
		dlgShutdownServer.setName("dlgShutdownServer");

		dlgShutdownAll = new YesNoDialog(guiFrame, "Shut Down Everything", true,"Are you sure you want to shut everything down ?");
		dlgShutdownAll.addYesListener(eventListener);
		dlgShutdownAll.setName("dlgShutdownAll");

		pnlQueues = new Panel();
		pnlDebug = new Panel();
		pnlUsers = new Panel();
		pnlServer= new Panel(new GridLayout(2,1));

/*--------------*/
/* Queues Panel */
/*--------------*/
		pnlQueues.setLayout(new BorderLayout());
		lstQueues = new java.awt.List();
		lstQueues.setMultipleMode(false);
		lstQueues.addActionListener(listItemListener);
		lstQueues.setName("Queues");
		pnlQueues.add("North", lblQueues);
		pnlQueues.add("Center",lstQueues);

/*-------------*/
/* Users Panel */
/*-------------*/
		Panel pnlUsers2 = new Panel(new GridLayout(1,2));
		pnlUsers.setLayout(new BorderLayout());
		lstUsers = new java.awt.List();
		lstUsers.setMultipleMode(false);
		lstUsers.addActionListener(listItemListener);
		lstUsers.setName("Users");
		pnlUsers2.add(lblUsers);
		pnlUsers2.add(btnCleanUsers);
		pnlUsers.add("North", pnlUsers2);
		pnlUsers.add("Center",lstUsers);

/*-----------------*/
/* Debugging Panel */
/*-----------------*/
		cbgDebug = new CheckboxGroup();
		cbError = new Checkbox("Errors Only", cbgDebug, false);
		cbMajor = new Checkbox("Major Events", cbgDebug, false);
		cbMinor = new Checkbox("Minor Events", cbgDebug, false);
		cbDebug = new Checkbox("Debugging", cbgDebug, false);
		cbError.addItemListener(checkboxListener);
		cbMajor.addItemListener(checkboxListener);
		cbMinor.addItemListener(checkboxListener);
		cbDebug.addItemListener(checkboxListener);
		pnlDebug.setLayout(new GridLayout(5,1));
		pnlDebug.add(lblDebugging);
		pnlDebug.add(cbError);
		pnlDebug.add(cbMajor);
		pnlDebug.add(cbMinor);
		pnlDebug.add(cbDebug);

/*--------------*/
/* Server Panel */
/*--------------*/
		pnlServerTop.add(lblServer);
		pnlServerTop.add(txtServer);
		pnlServerTop.add(lblPort);
		pnlServerTop.add(txtPort);
		pnlServerBottom.add(btnStart);
		pnlServerBottom.add(btnStop);
		pnlServerBottom.add(btnShutdownServer);
		pnlServerBottom.add(btnShutdownAll);
		btnStop.setEnabled(false);

		pnlServer.add(pnlServerTop);
		pnlServer.add(pnlServerBottom);

		btnShutdownServer.setForeground(Color.white);
		btnShutdownServer.setBackground(Color.red);
		btnShutdownAll.setForeground(Color.white);
		btnShutdownAll.setBackground(Color.red);
		btnStart.addActionListener(eventListener);
		btnStop.addActionListener(eventListener);
		btnShutdownServer.addActionListener(eventListener);
		btnShutdownAll.addActionListener(eventListener);

/*---------------------------------*/
/* Add all the panels to the frame */
/*---------------------------------*/
		pnlTop.add(pnlServer);
		pnlBottom.setLayout(new GridLayout(1,4));
		pnlBottom.add(pnlQueues);
		pnlBottom.add(pnlUsers);
		pnlBottom.add(pnlDebug);
		
		guiFrame.setLayout(new BorderLayout());

		guiFrame.add("North", pnlTop);
		guiFrame.add("Center", pnlBottom);
		guiFrame.add("South", txtStatus);

/*-------------------*/
/* Display the frame */
/*-------------------*/
                guiFrame.setVisible(true);

	}

	DataConnection makeServerConnection(String s, int p)
	{
		DataConnection d;

		try
		{
			d = new DataConnection(s, p);
		}
		catch(Exception exIO)
		{
			d = null;
			statusError("Server " + s + " is not responding");
		}
		return d;
	}

	void setDebugLevel(Checkbox cb)
	{
		int dbgLevel = Constants.DEBUG_ERROR;
		DataConnection server;
		int port;

		if(cb == cbError) dbgLevel = Constants.DEBUG_ERROR;
		if(cb == cbMajor) dbgLevel = Constants.DEBUG_MAJOR;
		if(cb == cbMinor) dbgLevel = Constants.DEBUG_MINOR;
		if(cb == cbDebug) dbgLevel = Constants.DEBUG_DEBUG;

		port = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);

		try
		{
			server = new DataConnection(txtServer.getText(), port);
			server.dataOut.writeInt(dbgLevel);
			server.close();
		}
		catch(Exception ex)
		{
			statusError("Unable to set Debug level");
		}
	}


/*------------------------------------------------------------*/
/* Update the specified list with the comma-separated details */
/*------------------------------------------------------------*/
	void updateList(String items, java.awt.List lst)
	{
                String[] tokens = items.split(",");
                if(lst.getItemCount() > 0) lst.removeAll();
                for(String this_item : tokens)
                {
                        lst.add(this_item);
                }
	}

	void getQueueNames(DataConnection d)
	{
		String serverQueues;
		try
		{
			d.dataOut.writeInt(Constants.QUEUE_NAMES);
			serverQueues = d.textIn.readLine();
			updateList(serverQueues, lstQueues);
		}
		catch(Exception ex) {}
	}

	void getUserNames(DataConnection d)
	{
		String users;
		try
		{
			d.dataOut.writeInt(Constants.LIST_USERS);
			users = d.textIn.readLine();
			updateList(users, lstUsers);
		}
		catch(Exception ex) {}
	}

	void getDebugDetails(DataConnection d)
	{
		int DebugLevel;
		try
		{
			d.dataOut.writeInt(Constants.DEBUG_DETAILS);
			DebugLevel = d.dataIn.readInt();
			switch(DebugLevel)
			{
				case Debug.ERROR:
					cbgDebug.setSelectedCheckbox(cbError);
					break;
				case Debug.MAJOR:
					cbgDebug.setSelectedCheckbox(cbMajor);
					break;
				case Debug.MINOR:
					cbgDebug.setSelectedCheckbox(cbMinor);
					break;
				case Debug.DEBUG:
					cbgDebug.setSelectedCheckbox(cbDebug);
					break;
			}
		}
		catch(Exception ex) {}
	}

	void refreshDisplay()
	{
		int pPort;
		String pServer;
		DataConnection server;

		if(! serverNamePresent()) return;

/*-------------------------------------*/
/* Save the settings into the ini file */
/*-------------------------------------*/
		props.put("server", txtServer.getText());
		props.put("port", txtPort.getText());
		try
		{
			props.store(new FileOutputStream(iniFileName), "Trots Settings");
		}
		catch(Exception ex) {}

		pPort = makeInt(txtPort.getText(), Constants.DEFAULT_PORT);
		pServer = txtServer.getText();

		try
		{
			server = makeServerConnection(pServer, pPort);
			if(server != null)
			{
					server.dataOut.writeInt(Constants.SERVER_VERSION);
					statusInfo(new StringBuilder("Connection OK - ").append(server.textIn.readLine()).toString());
					server.close();
			}

			if(dlgQueueDetails == null || ! dlgQueueDetails.isVisible() )
			{
				server = makeServerConnection(pServer, pPort);
				if(server != null)
				{
					getQueueNames(server);
					server.close();
				}
			}

			if( dlgUserDetails == null || ! dlgUserDetails.isVisible() )
			{
				server = makeServerConnection(pServer, pPort);
				if(server != null)
				{
					getUserNames(server);
					server.close();
				}
			}

			server = makeServerConnection(pServer, pPort);
			if(server != null)
			{
				getDebugDetails(server);
				server.close();
			}
		}
		catch(Exception exIO)
		{
			statusError("Server " + txtServer.getText() + " not responding");
		}
	}

/*-------------*/
/* Constructor */
/*-------------*/
	public ControlGUI(boolean Debugging)
	{
		try
		{
			iniFileName =	System.getProperty("user.home") +
									System.getProperty("file.separator") +
									Constants.CONTROL_INI_FILE;

			props = new Properties();
			props.load(new FileInputStream(iniFileName));
		}
		catch(Exception ex) {}

		buildGUI();
	}

/*----------------------------*/
/* Command line instantiation */
/*----------------------------*/
	public static void main(String argv[])
	{
		//ControlGUI cg = new ControlGUI(argv.length > 0);
		new ControlGUI(argv.length > 0);
	}

	class MonitorThread extends Thread
	{
		boolean continueProcessing;

		public MonitorThread()
		{
			continueProcessing = true;
		}

		public void run()
		{
			while(continueProcessing)
			{
				refreshDisplay();
				try {
					Thread.sleep(30 * 1000);
				}
				catch(Exception ex) {}
			}
		}

		public void halt()
		{
			continueProcessing = false;
		}
	}

	MonitorThread monitorThread;

	public void startMonitorThread()
	{
		if(monitorThread != null)
		{
			stopMonitorThread();
		}
		monitorThread = new MonitorThread();

		btnStart.setEnabled(false);
		btnCleanUsers.setEnabled(false);
		btnShutdownServer.setEnabled(false);
		btnShutdownAll.setEnabled(false);
		txtServer.setEnabled(false);
		txtPort.setEnabled(false);
		btnStop.setEnabled(true);
		

		monitorThread.start();
	}

	public void stopMonitorThread()
	{
		if(monitorThread != null)
		{
			monitorThread.halt();
		}
		btnStart.setEnabled(true);
		btnCleanUsers.setEnabled(true);
		btnShutdownServer.setEnabled(true);
		btnShutdownAll.setEnabled(true);
		txtServer.setEnabled(true);
		txtPort.setEnabled(true);
		btnStop.setEnabled(false);
		txtStatus.setText("");
	}
}
