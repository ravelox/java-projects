import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSExport;

/**
 * Minimal in-browser scoreboard server.  The original implementation listened
 * on a TCP port which is not possible inside a browser environment.  This
 * version keeps the same command based protocol but exposes a single
 * {@link #handle(String)} method that can be invoked from JavaScript.
 */
@JSExport
public class ScoreServer {
    List<GameTable> games;

    static class Player {
        String playerName;
        int playerScore;

        public Player(String name, int score) {
            playerName = name;
            playerScore = score;
        }
    }

    static class GameTable {
        String name;
        List<Player> playerScores;

        public GameTable(String pName) {
            playerScores = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Player p = new Player(pName + " " + (i + 1), 0);
                playerScores.add(p);
            }
            name = pName;
        }

        public boolean isHiScore(int score) {
            Player p = playerScores.get(9);
            return (score > p.playerScore);
        }

        public void addScore(String name, int score) {
            Player p = new Player(name, score);
            Player hiPlayer;
            int i = 0;
            while (i < 10) {
                hiPlayer = playerScores.get(i);
                if (p.playerScore > hiPlayer.playerScore) {
                    playerScores.add(i, p);
                    break;
                }
                i++;
            }
        }

        public String getScores() {
            StringBuilder sb = new StringBuilder();
            int i;
            Player p;
            for (i = 0; i < 10; i++) {
                p = playerScores.get(i);
                if (i > 0) {
                    sb.append(";");
                }
                sb.append(p.playerName);
                sb.append("#");
                sb.append(p.playerScore);
            }
            return sb.toString();
        }
    }

    private GameTable findGame(String game) {
        if (games.isEmpty()) return null;
        for (int i = 0; i < games.size(); i++) {
            GameTable g = games.get(i);
            if (g.name.equalsIgnoreCase(game)) return g;
        }
        return null;
    }

    private int isHi(String game, int score) {
        GameTable g = findGame(game);
        if (g == null) return 0;
        return (g.isHiScore(score) ? 1 : 0);
    }

    private String getTable(String game) {
        GameTable g = findGame(game);
        if (g == null) {
            g = new GameTable(game);
            games.add(g);
        }
        return g.getScores();
    }

    private void addScore(String game, String pName, int pScore) {
        GameTable g = findGame(game);
        if (g == null) {
            g = new GameTable(game);
            games.add(g);
        }
        g.addScore(pName, pScore);
    }

    public ScoreServer() {
        games = new ArrayList<>();
        expose(this);
    }

    /**
     * Handle a command of the form used by the original socket based protocol.
     * For example: "get#MAKI" or "add#MAKI#100#Bob".
     */
    @JSExport
    public String handle(String commandString) {
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

    @JSBody(params = { "srv" }, script = "window.scoreServer = srv;")
    private static native void expose(ScoreServer srv);
}
