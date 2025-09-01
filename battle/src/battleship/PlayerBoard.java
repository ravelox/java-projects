package battleship;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayerBoard extends Canvas implements MouseListener
{
	private int board[][];
	private int state;
	private int turn;
	private int currentShip;
	private boolean startPlaced;
	private int startX, startY;
	private BattleShip parent;

	public int getCurrentShip()
	{
		return currentShip;
	}

	public void setState(int s)
	{
		state = s;
	}

	public int getState()
	{
		return state;
	}

	public void setTurn(int t)
	{
		turn = t;
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

		setValue(x,y, Constants.START, false);
/*----*/
/* UP */
/*----*/
		if(up)
		{
			j=numPieces;
			for(i=y;j>0;i--)
			{
				if(getValue(x,i) != Constants.BLANK)
				{
					if(getValue(x,i) != Constants.START)
					{
						j=0;
						resetPieces(Constants.UP);
						up=false;
					}
				}
				else
				{
					setValue(x,i,Constants.UP, false);
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
				if(getValue(x,i) != Constants.BLANK)
				{
					if(getValue(x,i) != Constants.START)
					{
						j=0;
						resetPieces(Constants.DOWN);
						down=false;	
					}
				}
				else
				{
					setValue(x,i,Constants.DOWN, false);
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
				if(getValue(i,y) != Constants.BLANK)
				{
					if(getValue(i,y) != Constants.START)
					{
						j=0;
						resetPieces(Constants.LEFT);
						left=false;	
					}
				}
				else
				{
					setValue(i,y,Constants.LEFT, false);
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
				if(getValue(i,y) != Constants.BLANK)
				{
					if(getValue(i,y) != Constants.START)
					{
						j=0;
						resetPieces(Constants.RIGHT);
						right=false;	
					}
				}
				else
				{
					setValue(i,y,Constants.RIGHT, false);
				}
				j--;
			}
		}

		repaint();
	}

/*-----------------------*/
/* Mouse Listener events */
/*-----------------------*/
	public void mouseClicked(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e)
	{
		int i,j;
		int pieceValue;
		int actualShip;

/*------------------------------------------*/
/* If it's not our turn then stop right now */
/*------------------------------------------*/
		if(state != Constants.PLACE_SHIPS || (state==Constants.PLAYING && turn != Constants.LOCAL_TURN)) return;

/*-------------------------------------------------------------------*/
/* If we've slipped the net then limit check what ship we're placing */
/*-------------------------------------------------------------------*/
		if(currentShip>5)
		{
			return;
		}

/*---------------------------------------------------------------*/
/* Get the position of the mouse on the board by square location */
/*---------------------------------------------------------------*/
		i = e.getX() / Constants.SQUARE_SIZE;
		j = e.getY() / Constants.SQUARE_SIZE;

		actualShip=currentShip;
	
		if(startPlaced)
		{
			pieceValue=getValue(i,j);

			if((pieceValue >= Constants.UP && pieceValue <= Constants.RIGHT) || (pieceValue==Constants.START && currentShip==1))
			{
				setShip(pieceValue,currentShip);
				repaint();
				startPlaced=false;
				currentShip++;
				if(currentShip > 5)
				{
					state=Constants.PLAYING;
				}
				parent.notify(this);
			}
			else
			{
/*------------------------------------*/
/* Reset UP DOWN LEFT RIGHT and START */
/*------------------------------------*/
				resetPieces(Constants.START);
				resetPieces(Constants.UP);
				resetPieces(Constants.DOWN);
				resetPieces(Constants.LEFT);
				resetPieces(Constants.RIGHT);
				startPlaced=false;
			}
		}

/*---------------------------------------------------*/
/* Special case if the start position is being moved */
/*---------------------------------------------------*/
		if(!startPlaced && actualShip==currentShip)
		{
			startX = i; startY=j;
			startPlaced = true;
			showPossiblePlaces(startX, startY, currentShip);
		}
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}

/*-------------*/
/* Constructor */
/*-------------*/
	public PlayerBoard(BattleShip p)
	{
		super();
		parent = p;
		board = new int[Constants.MAX_X][Constants.MAX_Y];
		this.setSize(3 + (Constants.MAX_X * Constants.SQUARE_SIZE) + 3, 3 + (Constants.MAX_X * Constants.SQUARE_SIZE) + 3);
		state = 0;
		turn = 0;
		currentShip = 1;
		addMouseListener(this);
	}

	public void initialise()
	{
		for(int i=0;i<Constants.MAX_X;i++)
		{
			for(int j=0;j<Constants.MAX_Y;j++)
			{
				board[i][j]=Constants.BLANK;
			}
		}
		repaint();
	}

	public void paint(Graphics g)
	{
		int i,j;

		for(i=0;i<Constants.MAX_X;i++)
		{
			for(j=0;j<Constants.MAX_Y;j++)
			{
				switch(board[i][j])
				{
					case Constants.BLANK:
						g.setColor(Color.lightGray);
						break;
					case Constants.DESTROYER:
					case Constants.GUNBOAT:
					case Constants.SUBMARINE:
					case Constants.FRIGATE:
					case Constants.AIRCRAFTCARRIER:
						g.setColor(Color.white);
						break;
					case Constants.HIT:
						g.setColor(Color.red);
						break;
					case Constants.MISS:
						g.setColor(Color.black);
						break;
					case Constants.START:
						g.setColor(Color.yellow);
						break;
					case Constants.UP:
					case Constants.DOWN:
					case Constants.LEFT:
					case Constants.RIGHT:
						g.setColor(Color.darkGray);
						break;
				}
				g.fill3DRect(3 + (i * Constants.SQUARE_SIZE), 3 + (j * Constants.SQUARE_SIZE ), Constants.SQUARE_SIZE, Constants.SQUARE_SIZE, false);
			}
		}
	}

	public void repaint()
	{
		paint(this.getGraphics());
	}

	public void resetPieces(int piece)
	{
		int i,j;

		for(i=0;i<Constants.MAX_X;i++)
		{
			for(j=0;j<Constants.MAX_Y;j++)
			{
				if(board[i][j]==piece) board[i][j]=Constants.BLANK;
			}
		}
	}

	public void setShip(int piece, int shipValue)
	{
		int i,j;

		for(i=0;i<Constants.MAX_X;i++)
		{
			for(j=0;j<Constants.MAX_Y;j++)
			{
				if(board[i][j]==piece || board[i][j]==Constants.START)
				{
					board[i][j]=10+shipValue;
				}
				
			}
		}

		resetPieces(Constants.UP);
		resetPieces(Constants.DOWN);
		resetPieces(Constants.LEFT);
		resetPieces(Constants.RIGHT);
	}

	public void setValue(int i, int j, int type, boolean drawPiece)
	{
		if(i<0 || i>=Constants.MAX_X) return;
		if(j<0 || j>=Constants.MAX_Y) return;
		if(type<Constants.BLANK || type>Constants.RIGHT) return;

		board[i][j]=type;
		
		if(drawPiece) repaint();
	}

	public int getValue(int i, int j)
	{
		if(i<0 || i>=Constants.MAX_X) return 0;
		if(j<0 || j>=Constants.MAX_Y) return 0;
		return(board[i][j]);
	}
}
