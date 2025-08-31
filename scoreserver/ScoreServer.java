import netscape.security.PrivilegeManager;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

public class ScoreServer
{
	ServerSocket serverSocket;
	Vector Games;

	class Player
	{
		String playerName;
		int playerScore;
		
		public Player(String name, int score)
		{
			playerName = name;
			playerScore = score;
		}
	}

	class GameTable
	{
		String name;
		Vector playerScores;

		public GameTable(String pName)
		{
			playerScores = new Vector();
			for(int i=0;i<10;i++)
			{
				Player p = new Player(pName + " " + (i+1), 0);
				playerScores.insertElementAt(p, i);
			}

			name = pName;
		}

		public boolean isHiScore(int score)
		{
			Player p;
			
			p = (Player)playerScores.elementAt(9);

			return(score > p.playerScore);
		}

		public void addScore(String name, int score)
		{
			Player p = new Player(name, score);
			Player hiPlayer;
			int i;

			i = 0;
			while(i < 10)
			{
				hiPlayer = (Player)playerScores.elementAt(i);
				if(p.playerScore > hiPlayer.playerScore)
				{
					playerScores.insertElementAt(p, i);
					break;
				}
				i++;
			}
		}

		public String getScores()
		{
			StringBuffer sb = new StringBuffer("");
			int i;
			Player p;

			for(i=0;i<10;i++)
			{
				p = (Player)playerScores.elementAt(i);
				if(i > 0)
				{
					sb.append(";");
				}
				sb.append(p.playerName);
				sb.append("#");
				sb.append(p.playerScore);
			}
			return sb.toString();
		}
	}

	private GameTable findGame(String game)
	{
		if(Games.size() == 0) return null;

		for(int i = 0; i < Games.size(); i++)
		{
			GameTable g = (GameTable)Games.elementAt(i);
			if(g.name.equalsIgnoreCase(game)) return g;
		}

		return null;
	}

	private int isHi(String game, int score)
	{
		GameTable g = findGame(game);
		if(g == null) return 0;
		return (g.isHiScore(score) ? 1 : 0);
	}

	private String getTable(String game)
	{
		GameTable g = findGame(game);

		if(g == null)
		{
			g = new GameTable(game);
			Games.addElement(g);
		}

		return g.getScores()+"\n";
	}

	private void addScore(String game, String pName, int pScore)
	{
		GameTable g;
		
		g = findGame(game);

		if(g == null)
		{
			g = new GameTable(game);
			Games.addElement(g);
		}

		g.addScore(pName, pScore);
	}

	public ScoreServer()
	{
		Games = new Vector();
		Socket client;
		DataInputStream dataIn;
		DataOutputStream dataOut;
		BufferedReader textIn;
		String commandString = "";
		String command = "";
		String game = "";
		String pName = "";
		String score = "";
		int pScore = 0;
		String scoreList;
		GameTable gt;

		try
		{
			serverSocket = new ServerSocket(9800);


			while(true)
			{
				client = serverSocket.accept();
				System.out.println("Client connection made");

				dataIn = new DataInputStream(client.getInputStream());
				dataOut = new DataOutputStream(client.getOutputStream());
				textIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

				commandString = textIn.readLine();

				StringTokenizer st = new StringTokenizer(commandString, "#");
				
				int numTokens = st.countTokens();
				
				command = st.nextToken();
				game = st.nextToken();

				if(numTokens >= 3)
				{
					score = st.nextToken();
					pScore = Integer.parseInt(score);
				}

				if(numTokens >= 4)
				{
					pName = st.nextToken();
				}
					
				System.out.println("Command = " + command);
				if(command.equalsIgnoreCase("get"))
				{
					scoreList = getTable(game);
					dataOut.writeBytes(scoreList);
				}

				if(command.equalsIgnoreCase("clr"))
				{
					gt = findGame(game);
					if(gt != null)
					{
						Games.removeElement(gt);
						gt = new GameTable(game);
						Games.addElement(gt);
					}
				}

				if(command.equalsIgnoreCase("chk"))
				{
					dataOut.writeInt(isHi(game, pScore));
				}

				if(command.equalsIgnoreCase("add"))
				{
					addScore(game, pName, pScore);
				}
			}
		}
		catch(IOException exIO)
		{
			System.out.println("Unable to create server socket");
			System.out.println(exIO.getMessage());
		}
		
	}
	public static void main(String argv[])
	{
		ScoreServer ss = new ScoreServer();
	}
}
