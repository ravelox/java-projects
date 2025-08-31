import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ThreadLocalRandom;

public class Maki extends Applet implements MouseListener
{
        private static final int MAX_X=14, MAX_Y=14;
        private int[][] board, marker, undoBoard;
	public boolean gameOver;
	public int score, undoScore;
	Font displayFont;
	ScoreClient sc;
	boolean scoreClientPresent;

/*------------------------*/
/* Re-randomize the board */
/*------------------------*/
        private void initialiseBoard()
        {
                for (int x = 0; x < MAX_X; x++)
                {
                        for (int y = 0; y < MAX_Y; y++)
                        {
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
        @Override
        public void init()
	{
		board = new int[MAX_X][MAX_Y];
		marker = new int[MAX_X][MAX_Y];
		undoBoard = new int[MAX_X][MAX_Y];

		displayFont = new Font("Helvetica", Font.BOLD, 21);

                initialiseBoard();
		
		addMouseListener(this);

/*-------------------------------------------*/
/* Check to see if a score client is present */
/*-------------------------------------------*/
		scoreClientPresent = false;
		System.out.println("Checking for presence of ScoreClient");
		sc = (ScoreClient)getAppletContext().getApplet("SCORES");
		if(sc != null) scoreClientPresent = true;

		System.out.println("Score Client present ? " + (scoreClientPresent ? "Yes" : "No"));
		
	};

/*----------------*/
/* Draw the board */
/*----------------*/
        @Override
        public void paint(Graphics g)
	{
		int x,y;

		for(x=0;x<MAX_X;x++)
		{
			for(y=0;y<MAX_Y;y++)
			{

/*------------------------------------------------*/
/* If this box has been marked then make it black */
/*------------------------------------------------*/
				if(marker[x][y] > 0)
				{
					g.setColor(Color.black);
				}
				else
				{
/*-----------------------------------*/
/* Set the colour based on the value */
/*-----------------------------------*/
					switch(board[x][y])
					{
						case 1:	g.setColor(Color.red); break;
						case 2:	g.setColor(Color.yellow); break;
						case 3:	g.setColor(Color.blue); break;
						case 4:	g.setColor(Color.white); break;
						case 5:	g.setColor(Color.green); break;
					}
				}

/*---------------------------------------------------------------------*/
/* If the box is not empty then draw the box in the appropriate colour */
/*---------------------------------------------------------------------*/
				if(board[x][y] > 0)
				{
					g.fillRect( (x * 25) + 1, (y * 25) + 1, 23, 23);
					g.draw3DRect( (x * 25) + 1, (y * 25) + 1, 24, 24, true);
				}
				else
				{
/*--------------------------*/
/* Otherwise draw "nothing" */
/*--------------------------*/
					g.clearRect( (x * 25) + 1, (y * 25) + 1, 25, 25);
				}
			}
		}

		if(gameOver)
		{
			g.setFont(displayFont);
			g.setColor(Color.black);
			g.drawString("GAME OVER", 50, 50);
			g.setColor(Color.white);
			g.drawString("GAME OVER", 52, 52);
		}
	}

/*---------------------------------*/
/* Re-draw the screen if necessary */
/*---------------------------------*/
        @Override
        public void update(Graphics g)
	{
		paint(g);
	}

/*-------------------*/
/* Display the score */
/*-------------------*/
        public void drawScore(Graphics g)
        {
                g.setFont(displayFont);
                int fh = getFontMetrics(displayFont).getHeight();
                Dimension d = this.getSize();
                g.clearRect(( MAX_X + 1) * 25, 0, 100, 100);
                g.setColor(Color.black);
                g.drawString(Integer.toString(score), ( MAX_X + 1) * 25 , fh);
                g.setColor(Color.white);
                g.drawString(Integer.toString(score), (( MAX_X + 1 ) * 25) + 2, fh + 1);
        }

/*-----------------------------*/
/* Process a mouse click event */
/*-----------------------------*/
        @Override
        public void mouseClicked(MouseEvent e)
	{
		Graphics g = this.getGraphics();
		Point p,box = new Point();
		int boxes_removed;

/*-----------------------------------*/
/* Don't carry on if the game's over */
/*-----------------------------------*/
		if(gameOver) return;

/*-------------------------*/
/* Get the pointer details */
/*-------------------------*/
		p = e.getPoint();

/*-----------------------------*/
/* Work out which box we're in */
/*-----------------------------*/
		box.x = p.x / 25 ;
		box.y = p.y / 25 ;

/*-----------------------------------------------------*/
/* No point carrying on if the pointer is out of range */
/*-----------------------------------------------------*/
		if(box.x > MAX_X || box.y > MAX_Y) return;

/*------------------------------------------*/
/* No point carrying on if the box is empty */
/*------------------------------------------*/
		if(board[box.x][box.y] == 0)
		{
			clear_boxes(true);
			return;
		}

/*-----------------------------------------------------------*/
/* If the box has already been marked then we need to remove */
/* the marked boxes                                          */
/*-----------------------------------------------------------*/
		if(marker[box.x][box.y] > 0)
		{
			boxes_removed = count_marked();
			
/*-----------------------------*/
/* Update the score as (n-2)^2 */
/*-----------------------------*/
			score = score + (int)Math.pow( ( boxes_removed-2 ) , 2); 

			drawScore(this.getGraphics());

/*-------------------*/
/* Shuffle the boxes */
/*-------------------*/
			clear_boxes(false);
			pack_columns();
			shift_columns();

			gameOver = check_win();

			if(gameOver && scoreClientPresent)
			{
				sc.newScore(score);
			}
		}
		else
		{
/*--------------------------------------------------------------*/
/* If the box is not marked then clear out any existing markers */
/* and mark from the new position                               */
/*--------------------------------------------------------------*/
			clear_boxes(true);

/*-----------------------------------------*/
/* Save the current board to allow an undo */
/*-----------------------------------------*/
			saveUndo();

			mark_boxes(box.x, box.y, board[box.x][box.y]);

/*----------------------------------------------*/
/* If only 1 box has been marked then unmark it */
/*----------------------------------------------*/
			if(count_marked() < 2) clear_boxes(true);
		}

/*------------------*/
/* Update the board */
/*------------------*/
		paint(g);

	}


/*----------------------------------------------*/
/* Remaining mouse events for the MouseListener */
/*----------------------------------------------*/
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}

/*---------------------------------------*/
/* Mark boxes based on the current color */
/*---------------------------------------*/
	private void mark_boxes(int x, int y, int current_color)
	{

/*----------------*/
/* Range checking */
/*----------------*/
		if(x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y) return;

		if( (marker[x][y] > 0) || (board[x][y] != current_color) )
		{
			return;
		}

/*---------------*/
/* Mark this box */
/*---------------*/
		marker[x][y]=1;

/*--------------------------------------------------------------*/
/* Recursive marking in N, S, E and W directions (no diagonals) */
/*--------------------------------------------------------------*/
		mark_boxes(x - 1, y , current_color);
		mark_boxes(x + 1, y , current_color);
		mark_boxes(x , y - 1 , current_color);
		mark_boxes(x , y + 1 , current_color);
	}

/*---------------------------------------------------*/
/* Un mark any marked boxes (optionally remove them) */
/*---------------------------------------------------*/
	private void clear_boxes(boolean reset)
	{
		for(int x=0; x < MAX_X; x++)
		{
			for(int y=0; y < MAX_Y; y++)
			{
				if(marker[x][y] > 0)
				{
					if(!reset) board[x][y] = 0;
					marker[x][y] = 0;
				}
			}
		}
	}

/*----------------------------------*/
/* Count the number of marked boxes */
/*----------------------------------*/
	private int count_marked()
	{
		int marked=0;

		for(int x=0; x < MAX_X; x++)
			for(int y=0; y < MAX_Y; y++)
				if(marker[x][y] > 0) marked++;

		return marked;
	}

/*-----------------------------------------*/
/* Pack any columns that have empty spaces */
/*-----------------------------------------*/
	private void pack_columns()
	{
		int x,y,j;

		for(y = MAX_Y - 1; y >= 0 ; y--)
		{
			for(x = 0; x < MAX_X; x++)
			{
				j = y;
				while(j < MAX_Y - 1)
				{
					if(board[x][j + 1] == 0)
					{
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
	private void shift_columns()
	{
		int x,y,j;

		for(x = 1; x < MAX_X; x ++)
		{

			j = x;
			while(j > 0 && board[j - 1][MAX_Y - 1] == 0)
			{
				for(y=0;y < MAX_Y; y++)
				{
					board[j - 1][y] = board[j][y];
					board[j][y] = 0;
				}
				j --;
			}
		}
	}

/*---------------------------------------------------------------------------*/
/* Check to see if there are any more possible moves, i.e. at least 2 pieces */
/* of the same colour together N, S, E or W                                  */
/*---------------------------------------------------------------------------*/
	private boolean check_win()
	{
		int x,y;
		int marked;

		x = 0;
		y = MAX_Y - 1;

		while(y >= 0 )
		{
			if(board[x][y] > 0)
			{
				mark_boxes(x,y, board[x][y]);
				marked = count_marked();
				clear_boxes(true);
				if(marked > 1) return false;
			}
			
			x ++;
			if(x > MAX_X - 1)
			{
				x = 0;
				y--;
			}
		}

		return true;
	}

	public void reset()
	{
		Graphics g = this.getGraphics();
		Dimension d = this.getSize();
                initialiseBoard();
		g.clearRect( 0, 0, d.width, d.height);
		paint(g);
	}

	private void saveUndo()
	{
		int x,y;

		for(x = 0 ; x < MAX_X ; x++)
		{
			for(y = 0 ; y < MAX_X ; y++)
			{
				undoBoard[x][y] = board[x][y];
			}
		}
		undoScore = score;
	}

	public void undo()
	{
		int x,y;

		for(x = 0 ; x < MAX_X ; x++)
		{
			for(y = 0 ; y < MAX_X ; y++)
			{
				board[x][y] = undoBoard[x][y];
			}
		}
		score = undoScore;

		paint(this.getGraphics());
		drawScore(this.getGraphics());
	}
}
