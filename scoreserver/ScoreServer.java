import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ScoreServer
{
        ServerSocket serverSocket;
        List<GameTable> games;

        static class Player
	{
		String playerName;
		int playerScore;
		
		public Player(String name, int score)
		{
			playerName = name;
			playerScore = score;
		}
	}

        static class GameTable
	{
		String name;
                List<Player> playerScores;

                public GameTable(String pName)
                {
                        playerScores = new ArrayList<>();
                        for(int i=0;i<10;i++)
                        {
                                Player p = new Player(pName + " " + (i+1), 0);
                                playerScores.add(p);
                        }

                        name = pName;
                }

                public boolean isHiScore(int score)
                {
                        Player p;

                        p = playerScores.get(9);

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
                                hiPlayer = playerScores.get(i);
                                if(p.playerScore > hiPlayer.playerScore)
                                {
                                        playerScores.add(i, p);
                                        break;
                                }
                                i++;
                        }
                }

                public String getScores()
                {
                        StringBuilder sb = new StringBuilder();
                        int i;
                        Player p;

                        for(i=0;i<10;i++)
                        {
                                p = playerScores.get(i);
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
                if(games.isEmpty()) return null;

                for(int i = 0; i < games.size(); i++)
                {
                        GameTable g = games.get(i);
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
                        games.add(g);
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
                        games.add(g);
		}

		g.addScore(pName, pScore);
	}

	public ScoreServer()
	{
                games = new ArrayList<>();
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
                                try (Socket client = serverSocket.accept();
                                     DataInputStream dataIn = new DataInputStream(client.getInputStream());
                                     DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());
                                     BufferedReader textIn = new BufferedReader(new InputStreamReader(client.getInputStream())))
                                {
                                        System.out.println("Client connection made");

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
                                                games.remove(gt);
                                                gt = new GameTable(game);
                                                games.add(gt);
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
