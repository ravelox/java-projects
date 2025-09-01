import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Simple desktop scoreboard server for the Maki game.
 *
 * <p>The server listens on TCP port 9800 and speaks the same text based
 * protocol as the original implementation.  Each connection handles a single
 * command and then closes.</p>
 */
public class ScoreServerDesktop {
    private final List<GameTable> games = new ArrayList<>();

    private static class Player {
        String playerName;
        int playerScore;
        Player(String name, int score) {
            playerName = name;
            playerScore = score;
        }
    }

    private static class GameTable {
        String name;
        List<Player> playerScores = new ArrayList<>();
        GameTable(String pName) {
            name = pName;
            for (int i = 0; i < 10; i++) {
                playerScores.add(new Player(pName + " " + (i + 1), 0));
            }
        }
        boolean isHiScore(int score) {
            Player p = playerScores.get(9);
            return score > p.playerScore;
        }
        void addScore(String name, int score) {
            Player p = new Player(name, score);
            int i = 0;
            while (i < playerScores.size() && p.playerScore <= playerScores.get(i).playerScore) {
                i++;
            }
            playerScores.add(i, p);
            if (playerScores.size() > 10) {
                playerScores.remove(10);
            }
        }
        String getScores() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                Player p = playerScores.get(i);
                if (i > 0) sb.append(';');
                sb.append(p.playerName).append('#').append(p.playerScore);
            }
            return sb.toString();
        }
    }

    private GameTable findGame(String game) {
        for (GameTable g : games) {
            if (g.name.equalsIgnoreCase(game)) return g;
        }
        return null;
    }

    private int isHi(String game, int score) {
        GameTable g = findGame(game);
        return g == null ? 0 : (g.isHiScore(score) ? 1 : 0);
    }

    private String getTable(String game) {
        GameTable g = findGame(game);
        if (g == null) {
            g = new GameTable(game);
            games.add(g);
        }
        return g.getScores();
    }

    private void addScore(String game, String name, int score) {
        GameTable g = findGame(game);
        if (g == null) {
            g = new GameTable(game);
            games.add(g);
        }
        g.addScore(name, score);
    }

    private String handle(String commandString) {
        StringTokenizer st = new StringTokenizer(commandString, "#");
        int numTokens = st.countTokens();
        String command = st.nextToken();
        String game = st.nextToken();
        switch (command.toLowerCase()) {
            case "get":
                return getTable(game);
            case "clr":
                GameTable gt = findGame(game);
                if (gt != null) {
                    games.remove(gt);
                    gt = new GameTable(game);
                    games.add(gt);
                }
                return "";
            case "chk":
                if (numTokens >= 3) {
                    int pScore = Integer.parseInt(st.nextToken());
                    return Integer.toString(isHi(game, pScore));
                }
                return "0";
            case "add":
                if (numTokens >= 4) {
                    int pScore = Integer.parseInt(st.nextToken());
                    String pName = st.nextToken();
                    addScore(game, pName, pScore);
                }
                return "";
            default:
                return "";
        }
    }

    private void run() throws IOException {
        try (ServerSocket server = new ServerSocket(9800)) {
            while (true) {
                try (Socket s = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                     PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                    String line = in.readLine();
                    if (line != null) {
                        String resp = handle(line);
                        out.print(resp);
                        if (!resp.isEmpty()) {
                            out.print('\n');
                        }
                        out.flush();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ScoreServerDesktop srv = new ScoreServerDesktop();
        srv.run();
    }
}
