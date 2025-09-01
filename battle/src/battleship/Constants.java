package battleship;

public class Constants
{
	public static final int COMMAND_NULL = 0;
	public static final int COMMAND_REGISTER = 1;
	public static final int COMMAND_DEREGISTER = 2;
	public static final int COMMAND_SHUTDOWN = 3;
	public static final int COMMAND_GET_STATE=4;
	public static final int COMMAND_FIRE=5;
   public static final int COMMAND_HIT=6;
   public static final int COMMAND_MISS=7;
   public static final int COMMAND_SUNK=8;
	public static final int COMMAND_LOSE=9;
	public static final int COMMAND_SUBMIT=10;

	public static final int COMMAND_OPPONENT_NAME=11;
	public static final int COMMAND_PLAYER_JOINED=12;

	public static final int REPLY_ACK = 54321;
	public static final int REPLY_SERVER_FULL=0;

	public static final int SOCKET_TIMEOUT = 10;

	public static final int DEFAULT_PORT = 9877;

	public static final int PLACE_SHIPS=1;
	public static final int REMOTE_TURN=2;
	public static final int LOCAL_TURN=3;
	public static final int PLAYING=4;

	public static final int MAX_X=15, MAX_Y=15;
	public static final int SQUARE_SIZE=20;
	public static final int BLANK=0;
	public static final int START=1;
	public static final int HIT=2;
	public static final int MISS=3;
	public static final int UP=4;
	public static final int DOWN=5;
	public static final int LEFT=6;
	public static final int RIGHT=7;
	public static final int DESTROYER=11;
	public static final int GUNBOAT=12;
	public static final int SUBMARINE=13;
	public static final int FRIGATE=14;
	public static final int AIRCRAFTCARRIER=15;
}
