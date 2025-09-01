package battleship;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.EventQueue;
import java.awt.Panel;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class BattleShip implements WindowListener,ActionListener
{
	MenuItem miNew;
	MenuItem miStartServer;
	MenuItem miStopServer;
	MenuItem miClient;
	MenuItem miConfig;
	MenuItem miExit;
	private DataConnection client;
	private YesNoDialog whoPlays;
	private Frame win;
	private PlayerBoard winLocal,winRemote;	
	private Label localStatus, remoteStatus;
	private boolean runningAsServer,connectedToServer,changingConfig,startPlaced;
	static IniFileGen ini;
	private int clientID, gameState;
	private String shipName[]={
					"Destroyer",
					"GunBoat",
					"Submarine",
					"Frigate",
					"Aircraft Carrier"
										};
	private int whoseTurn;

	ServerSocket server;
	Socket clientSocket;
	String remoteName;

/*------------------------*/
/* Window Listener events */
/*------------------------*/
	public void windowOpened(WindowEvent e) {}
	public void windowClosing(WindowEvent e)
	{
		exitProgram();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}

/*------------------------*/
/* Action Listener events */
/*------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();
		if(action.equals("Exit")) exitProgram();
		if(action.equals("New Game")) newGame();
		if(action.equals("Start Server"))
		{
			miStartServer.setEnabled(false);
			miStopServer.setEnabled(true);
			startServer();
			startClient();
		}
		if(action.equals("Stop Server"))
		{
			miStopServer.setEnabled(false);
			miStartServer.setEnabled(true);
			stopClient();
			stopServer();
		}
		if(action.equals("Connect to Server")) ini.show();
		if(action.equals("Config"))
		{
			changingConfig=true;
			ini.show();
		}
	}

	ActionListener startListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			String action = e.getActionCommand();
			whoPlays.setVisible(false);
			whoseTurn = (action.equals("Local") ? Constants.LOCAL_TURN : Constants.REMOTE_TURN);
		}
	};

/*-----------------*/
/* Pseudo notifier */
/*-----------------*/
	public void notify(Object o)
	{
		if(winLocal.getState() == Constants.PLACE_SHIPS)
		{
			localStatus.setText("Place your " + shipName[winLocal.getCurrentShip() - 1]);
		}
		else
		{
			localStatus.setText("PLAYING");
		}
	}

/*--------------------------------------------------------------*/
/* Utility function to return an integer value from a string or */
/* the supplied default                                         */
/*--------------------------------------------------------------*/
	static int makeInt(String value, int defaultValue)
	{
		int returnValue = defaultValue;

		try
		{
                        returnValue = Integer.parseInt(value);
		}
		catch(NumberFormatException exNumFmt) {}

		return returnValue;
	} 

	private void showPossiblePlaces(int x, int y, int numPieces)
	{
		boolean up,down,left,right;
		int i,j;

		up=down=left=right=false;

		if((y - numPieces) >= 0) up=true;
		if((y + numPieces) < Constants.MAX_Y) down=true;
		if((x - numPieces) >= 0) left=true;
		if((x + numPieces) < Constants.MAX_X) right=true;

/*----------------------------*/
/* Try each of the directions */
/*----------------------------*/

		winLocal.setValue(x,y, Constants.START, false);
/*----*/
/* UP */
/*----*/
		if(up)
		{
			j=numPieces;
			for(i=y;j>0;i--)
			{
				if(winLocal.getValue(x,i) != Constants.BLANK)
				{
					if(winLocal.getValue(x,i) != Constants.START)
					{
						j=0;
						winLocal.resetPieces(Constants.UP);
						up=false;
					}
				}
				else
				{
					winLocal.setValue(x,i,Constants.UP, false);
				}
				j--;
			}
		}

/*------*/
/* DOWN */
/*------*/
		if(down)
		{
			j=numPieces;
			for(i=y;j>0;i++)
			{
				if(winLocal.getValue(x,i) != Constants.BLANK)
				{
					if(winLocal.getValue(x,i) != Constants.START)
					{
						j=0;
						winLocal.resetPieces(Constants.DOWN);
						down=false;	
					}
				}
				else
				{
					winLocal.setValue(x,i,Constants.DOWN, false);
				}
				j--;
			}
		}

/*------*/
/* LEFT */
/*------*/
		if(left)
		{
			j=numPieces;
			for(i=x;j>0;i--)
			{
				if(winLocal.getValue(i,y) != Constants.BLANK)
				{
					if(winLocal.getValue(i,y) != Constants.START)
					{
						j=0;
						winLocal.resetPieces(Constants.LEFT);
						left=false;	
					}
				}
				else
				{
					winLocal.setValue(i,y,Constants.LEFT, false);
				}
				j--;
			}
		}

/*-------*/
/* RIGHT */
/*-------*/
		if(right)
		{
			j=numPieces;
			for(i=x;j>0;i++)
			{
				if(winLocal.getValue(i,y) != Constants.BLANK)
				{
					if(winLocal.getValue(i,y) != Constants.START)
					{
						j=0;
						winLocal.resetPieces(Constants.RIGHT);
						right=false;	
					}
				}
				else
				{
					winLocal.setValue(i,y,Constants.RIGHT, false);
				}
				j--;
			}
		}

		winLocal.repaint();
	}

	private void process_player_joined()
	{
	}

	private void stopServer()
	{
	}

	private void startServer()
	{
		System.out.println("Starting server...");

		Thread th = new Thread()
		{
			public void run()
			{
				BattleServer bs = new BattleServer(makeInt(ini.getProperty("port"), Constants.DEFAULT_PORT));
			}
		};

		th.start();
	}

	private void stopClient()
	{
	}

	private void startClient()
	{
		System.out.println("Starting client...");
		try
		{
			client = new DataConnection(ini.getProperty("server"), makeInt(ini.getProperty("port"), Constants.DEFAULT_PORT));

			client.dataOut.writeInt(Constants.COMMAND_REGISTER);

			clientID = client.dataIn.readInt();

			if(clientID == Constants.REPLY_SERVER_FULL)
			{
				localStatus.setText("No more connections allowed");
			}
			else
			{
				localStatus.setText("Client ID: " + clientID);

/*----------------------*/
/* Send the player name */
/*----------------------*/
				client.dataOut.writeBytes(ini.getProperty("player")+"\n");

				clientThread();
			}
		}
		catch(IOException exIO)
		{
			System.out.println("Exception: " + exIO.getMessage());
		}
	}

	void getClientCommand()
	{
		int command;
		while(true)
		{
			try
			{
				command = client.dataIn.readInt();
				System.out.println("Client reads: " + command);

				switch(command)
				{
					case Constants.COMMAND_PLAYER_JOINED:
						process_player_joined();
						break;
				}
			}
			catch(IOException exIO)
			{
				System.out.println("clientThread: " + exIO.getMessage());
				break;
			}
		}
	}

	private void clientThread()
	{

		Thread th = new Thread()
		{
			public void run()
			{
				getClientCommand();
			}
		};

		th.start();
	}

	private void exitProgram()
	{
		System.exit(0);
	}

	private void build_windows()
	{
		Panel topPanel, bottomPanel;
		MenuBar mb = new MenuBar();
		Menu menFile = new Menu("File");
		miNew = new MenuItem("New Game");
		miStartServer = new MenuItem("Start Server");
		miStopServer = new MenuItem("Stop Server");
		miClient = new MenuItem("Connect to Server");
		miConfig = new MenuItem("Config");
		miExit = new MenuItem("Exit");

		win = new Frame("BattleShip");

		menFile.add(miNew);
		menFile.add(miStartServer);
		menFile.add(miStopServer);
		menFile.add(miClient);
		menFile.addSeparator();	
		menFile.add(miConfig);
		menFile.add(miExit);

		miNew.addActionListener(this);
		miStartServer.addActionListener(this);
		miStopServer.addActionListener(this);
		miClient.addActionListener(this);
		miConfig.addActionListener(this);
		miExit.addActionListener(this);

		win.setMenuBar(mb);
		mb.add(menFile);
		winLocal = new PlayerBoard(this);
		winRemote = new PlayerBoard(this);

		win.addWindowListener(this);
		topPanel = new Panel();
		topPanel.setLayout(new GridLayout(1,2));
		bottomPanel = new Panel();
		bottomPanel.setLayout(new GridLayout(1,2));
		localStatus = new Label();
		remoteStatus = new Label();
		topPanel.add(winLocal);
		topPanel.add(winRemote);
		bottomPanel.add(localStatus);
		bottomPanel.add(remoteStatus);

		localStatus.setBackground(Color.black);
		localStatus.setForeground(Color.white);
		localStatus.setText("Local");
		localStatus.setAlignment(Label.CENTER);
		remoteStatus.setBackground(Color.black);
		remoteStatus.setForeground(Color.white);
		remoteStatus.setText("Remote");
		remoteStatus.setAlignment(Label.CENTER);
		win.setLayout(new BorderLayout());
		win.add("North", topPanel);
		win.add("South", bottomPanel);
		winLocal.setSize(winLocal.getPreferredSize());
		winRemote.setSize(winLocal.getPreferredSize());
		win.pack();
		win.setVisible(true);
	}

/*-------------*/
/* Constructor */
/*-------------*/
        @SuppressWarnings("this-escape")
        public BattleShip()
        {
		runningAsServer=false;
		connectedToServer=false;
		changingConfig=false;
		build_windows();
		whoPlays = new YesNoDialog(win, "New Game", true, "Who starts?", "Local", "Remote");
		whoPlays.addListener(startListener);
	}

	private void newGame()
	{

		winLocal.initialise();
		winRemote.initialise();
		winLocal.setState(Constants.PLACE_SHIPS);
		notify(winLocal);
	}

	public static void main(String argv[])
	{
		String propArray[][]={
                     		   {"Player Name", "player"},
               		         {"Server", "server"},
										{"Port", "port"},
									};
		ini = new IniFileGen("Game Information", "battleship.ini", propArray);

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
					System.out.println("Property ["+propArray[i][1]+"] is not set");	
				}
			}
			System.out.println("Properties were not set. Abnormal termination");
			System.exit(1);
		}
                EventQueue.invokeLater(BattleShip::new);
        }
}
