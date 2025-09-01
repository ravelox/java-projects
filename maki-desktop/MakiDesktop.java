import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Standalone desktop version of the Maki game.
 *
 * <p>This implementation mirrors the logic of the browser based version
 * but uses the Java AWT canvas for rendering so that the game can run
 * as a normal desktop application.</p>
 */
public class MakiDesktop extends Canvas implements ActionListener {
    private static final int MAX_X = 14, MAX_Y = 14;
    private final int[][] board = new int[MAX_X][MAX_Y];
    private final int[][] marker = new int[MAX_X][MAX_Y];
    private final int[][] undoBoard = new int[MAX_X][MAX_Y];
    private boolean gameOver = false;
    private int score = 0, undoScore = 0;

    public MakiDesktop() {
        setSize((MAX_X + 4) * 25, (MAX_Y + 4) * 25);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        initialiseBoard();
    }

    private void initialiseBoard() {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                marker[x][y] = 0;
                board[x][y] = ThreadLocalRandom.current().nextInt(1, 6);
            }
        }
        gameOver = false;
        score = 0;
        undoScore = 0;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if (marker[x][y] > 0) {
                    g.setColor(Color.black);
                } else {
                    switch (board[x][y]) {
                        case 1: g.setColor(Color.red); break;
                        case 2: g.setColor(Color.yellow); break;
                        case 3: g.setColor(Color.blue); break;
                        case 4: g.setColor(Color.white); break;
                        case 5: g.setColor(Color.green); break;
                        default: g.setColor(Color.white); break;
                    }
                }
                if (board[x][y] > 0) {
                    g.fillRect((x * 25) + 1, (y * 25) + 1, 23, 23);
                    g.setColor(Color.black);
                    g.drawRect((x * 25) + 1, (y * 25) + 1, 24, 24);
                } else {
                    g.clearRect((x * 25) + 1, (y * 25) + 1, 25, 25);
                }
            }
        }
        g.setColor(Color.black);
        g.drawString("Score: " + score, (MAX_X + 1) * 25, 20);
        if (gameOver) {
            g.drawString("GAME OVER", 50, 50);
        }
    }

    private void handleClick(int mx, int my) {
        int boxX = mx / 25;
        int boxY = my / 25;

        if (gameOver) return;
        if (boxX > MAX_X || boxY > MAX_Y) return;
        if (boxX < 0 || boxY < 0) return;

        if (board[boxX][boxY] == 0) {
            clearBoxes(true);
            repaint();
            return;
        }

        if (marker[boxX][boxY] > 0) {
            int boxesRemoved = countMarked();
            score = score + (int) Math.pow((boxesRemoved - 2), 2);
            clearBoxes(false);
            packColumns();
            shiftColumns();
            gameOver = checkWin();
            repaint();
        } else {
            saveUndo();
            clearBoxes(true);
            markBoxes(boxX, boxY, board[boxX][boxY]);
            repaint();
        }
    }

    private void clearBoxes(boolean reset) {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if (marker[x][y] > 0) {
                    if (!reset) board[x][y] = 0;
                    marker[x][y] = 0;
                }
            }
        }
    }

    private int countMarked() {
        int marked = 0;
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if (marker[x][y] > 0) marked++;
            }
        }
        return marked;
    }

    private void markBoxes(int x, int y, int colour) {
        if (x < 0 || x >= MAX_X || y < 0 || y >= MAX_Y) return;
        if (board[x][y] != colour || marker[x][y] > 0) return;
        marker[x][y] = 1;
        markBoxes(x + 1, y, colour);
        markBoxes(x - 1, y, colour);
        markBoxes(x, y + 1, colour);
        markBoxes(x, y - 1, colour);
    }

    private void packColumns() {
        for (int y = MAX_Y - 1; y >= 0; y--) {
            for (int x = 0; x < MAX_X; x++) {
                int j = y;
                while (j < MAX_Y - 1) {
                    if (board[x][j + 1] == 0) {
                        board[x][j + 1] = board[x][j];
                        board[x][j] = 0;
                    }
                    j++;
                }
            }
        }
    }

    private void shiftColumns() {
        for (int x = 1; x < MAX_X; x++) {
            int j = x;
            while (j > 0 && board[j - 1][MAX_Y - 1] == 0) {
                for (int y = 0; y < MAX_Y; y++) {
                    board[j - 1][y] = board[j][y];
                    board[j][y] = 0;
                }
                j--;
            }
        }
    }

    private boolean checkWin() {
        for (int y = MAX_Y - 1; y >= 0; y--) {
            for (int x = 0; x < MAX_X; x++) {
                if (board[x][y] > 0) {
                    markBoxes(x, y, board[x][y]);
                    int marked = countMarked();
                    clearBoxes(true);
                    if (marked > 1) return false;
                }
            }
        }
        return true;
    }

    private void saveUndo() {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                undoBoard[x][y] = board[x][y];
            }
        }
        undoScore = score;
    }

    public void undo() {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                board[x][y] = undoBoard[x][y];
            }
        }
        score = undoScore;
        repaint();
    }

    public void reset() {
        initialiseBoard();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("New".equals(cmd)) {
            reset();
        } else if ("Undo".equals(cmd)) {
            undo();
        }
    }

    public static void main(String[] args) {
        Frame frame = new Frame("Maki Desktop");
        MakiDesktop game = new MakiDesktop();
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setVisible(true);
    }
}

