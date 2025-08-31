import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Browser friendly version of the Maki game.  This implementation uses
 * the HTML5 canvas through TeaVM's DOM bindings so that the classic game can
 * run completely inside a web page without any AWT dependencies.
 */
public class Maki {
private static final int MAX_X = 14, MAX_Y = 14;
private int[][] board, marker, undoBoard;
public boolean gameOver;
public int score, undoScore;

private final HTMLCanvasElement canvas;
private final CanvasRenderingContext2D ctx;

/*------------------------*/
/* Re-randomize the board */
/*------------------------*/
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
}

/*-------------*/
/* Constructor */
/*-------------*/
public Maki(HTMLCanvasElement canvas) {
this.canvas = canvas;
this.ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
board = new int[MAX_X][MAX_Y];
marker = new int[MAX_X][MAX_Y];
undoBoard = new int[MAX_X][MAX_Y];

initialiseBoard();

canvas.addEventListener("click", (EventListener<MouseEvent>) this::mouseClicked);
paint();
drawScore();
}

/*----------------*/
/* Draw the board */
/*----------------*/
private void paint() {
for (int x = 0; x < MAX_X; x++) {
for (int y = 0; y < MAX_Y; y++) {
if (marker[x][y] > 0) {
ctx.setFillStyle("black");
} else {
switch (board[x][y]) {
case 1: ctx.setFillStyle("red"); break;
case 2: ctx.setFillStyle("yellow"); break;
case 3: ctx.setFillStyle("blue"); break;
case 4: ctx.setFillStyle("white"); break;
case 5: ctx.setFillStyle("green"); break;
default: ctx.setFillStyle("white"); break;
}
}

if (board[x][y] > 0) {
ctx.fillRect((x * 25) + 1, (y * 25) + 1, 23, 23);
ctx.strokeRect((x * 25) + 1, (y * 25) + 1, 24, 24);
} else {
ctx.clearRect((x * 25) + 1, (y * 25) + 1, 25, 25);
}
}
}

if (gameOver) {
ctx.setFillStyle("black");
ctx.fillText("GAME OVER", 50, 50);
}
}

/*-------------------*/
/* Display the score */
/*-------------------*/
private void drawScore() {
ctx.clearRect((MAX_X + 1) * 25, 0, 100, 100);
ctx.setFillStyle("black");
ctx.fillText(Integer.toString(score), (MAX_X + 1) * 25, 20);
}

/*-----------------------------*/
/* Process a mouse click event */
/*-----------------------------*/
private void mouseClicked(MouseEvent e) {
int boxX = (int) (e.getOffsetX() / 25);
int boxY = (int) (e.getOffsetY() / 25);
int boxes_removed;

if (gameOver) return;

if (boxX > MAX_X || boxY > MAX_Y) return;

if (board[boxX][boxY] == 0) {
clear_boxes(true);
return;
}

if (marker[boxX][boxY] > 0) {
boxes_removed = count_marked();
score = score + (int) Math.pow((boxes_removed - 2), 2);
drawScore();
clear_boxes(false);
pack_columns();
shift_columns();
gameOver = check_win();
} else {
clear_boxes(true);
saveUndo();
mark_boxes(boxX, boxY, board[boxX][boxY]);
if (count_marked() < 2) clear_boxes(true);
}

paint();
}

/*---------------------------------------*/
/* Mark boxes based on the current color */
/*---------------------------------------*/
private void mark_boxes(int x, int y, int current_color) {
if (x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y) return;
if ((marker[x][y] > 0) || (board[x][y] != current_color)) {
return;
}
marker[x][y] = 1;
mark_boxes(x - 1, y, current_color);
mark_boxes(x + 1, y, current_color);
mark_boxes(x, y - 1, current_color);
mark_boxes(x, y + 1, current_color);
}

/*---------------------------------------------------*/
/* Un mark any marked boxes (optionally remove them) */
/*---------------------------------------------------*/
private void clear_boxes(boolean reset) {
for (int x = 0; x < MAX_X; x++) {
for (int y = 0; y < MAX_Y; y++) {
if (marker[x][y] > 0) {
if (!reset) board[x][y] = 0;
marker[x][y] = 0;
}
}
}
}

/*----------------------------------*/
/* Count the number of marked boxes */
/*----------------------------------*/
private int count_marked() {
int marked = 0;
for (int x = 0; x < MAX_X; x++)
for (int y = 0; y < MAX_Y; y++)
if (marker[x][y] > 0) marked++;
return marked;
}

/*-----------------------------------------*/
/* Pack any columns that have empty spaces */
/*-----------------------------------------*/
private void pack_columns() {
int x, y, j;
for (y = MAX_Y - 1; y >= 0; y--) {
for (x = 0; x < MAX_X; x++) {
j = y;
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

/*--------------------------------------------------*/
/* Shift any columns left if there are empty spaces */
/*--------------------------------------------------*/
private void shift_columns() {
int x, y, j;
for (x = 1; x < MAX_X; x++) {
j = x;
while (j > 0 && board[j - 1][MAX_Y - 1] == 0) {
for (y = 0; y < MAX_Y; y++) {
board[j - 1][y] = board[j][y];
board[j][y] = 0;
}
j--;
}
}
}

/*---------------------------------------------------------------------------*/
/* Check to see if there are any more possible moves, i.e. at least 2 pieces */
/* of the same colour together N, S, E or W                                  */
/*---------------------------------------------------------------------------*/
private boolean check_win() {
int x, y;
int marked;
x = 0;
y = MAX_Y - 1;
while (y >= 0) {
if (board[x][y] > 0) {
mark_boxes(x, y, board[x][y]);
marked = count_marked();
clear_boxes(true);
if (marked > 1) return false;
}
x++;
if (x > MAX_X - 1) {
x = 0;
y--;
}
}
return true;
}

public void reset() {
initialiseBoard();
ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
paint();
drawScore();
}

private void saveUndo() {
for (int x = 0; x < MAX_X; x++) {
for (int y = 0; y < MAX_X; y++) {
undoBoard[x][y] = board[x][y];
}
}
undoScore = score;
}

public void undo() {
for (int x = 0; x < MAX_X; x++) {
for (int y = 0; y < MAX_X; y++) {
board[x][y] = undoBoard[x][y];
}
}
score = undoScore;
paint();
drawScore();
}

@JSBody(script = "return;")
private static native void noop();

/** Entry point used by TeaVM to start the game in the browser. */
public static void main(String[] args) {
HTMLCanvasElement canvas = (HTMLCanvasElement) Window.current().getDocument().getElementById("maki");
if (canvas != null) {
canvas.setWidth((MAX_X + 4) * 25);
canvas.setHeight((MAX_Y + 4) * 25);
new Maki(canvas);
}
}
}
