/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.13  99/07/20  11:28:03  11:28:03  dkelly (Dave Kelly)
Added SOCKET_TIMEOUT

Revision 1.12  99/07/19  10:09:11  10:09:11  dkelly (Dave Kelly)
Added SERVER_VERSION

Revision 1.11  99/07/15  16:35:17  16:35:17  dkelly (Dave Kelly)
Removed Call Receipt admin commands

Revision 1.10  99/07/13  11:59:28  11:59:28  dkelly (Dave Kelly)
Added CONTROL_INI_FILE

Revision 1.9  99/07/12  11:43:22  11:43:22  dkelly (Dave Kelly)
Added REMOVE_CALL_RECEIPT (Doh!)

Revision 1.8  99/07/09  12:11:02  12:11:02  dkelly (Dave Kelly)
Added ALL_SHUTDOWN

Revision 1.7  99/07/07  12:57:53  12:57:53  dkelly (Dave Kelly)
Added DEFAULT_PORT

Revision 1.6  99/07/07  11:40:22  11:40:22  dkelly (Dave Kelly)
Changed SERVER_STATUS to STATUS_HTML
Added LIST_USERS, USER_DETAILS, QUEUE_DETAILS, DEBUG_DETAILS,
LIST_CALL_RECEIPT and CALL_RECEIPT_DETAILS

Revision 1.5  99/07/02  09:39:21  09:39:21  dkelly (Dave Kelly)
Removed REMOVE_CALL_RECEIPT

Revision 1.4  99/07/01  13:17:35  13:17:35  dkelly (Dave Kelly)
Removed CONNECTION_OK and added NOT_LISTENING

Revision 1.3  99/04/29  13:18:09  13:18:09  dkelly (Dave Kelly)
Added CLEAN_USERS and HEARTBEAT

Revision 1.2  99/04/22  16:29:14  16:29:14  dkelly (Dave Kelly)
Added RCS log

*/
public class Constants
{
	public static final String LISTENING = "Listening";
	public static final String NOT_LISTENING = "Not Listening";
	public static final String CONNECTING = "Connecting...";
	public static final String LOST_CONNECTION = "Lost connection. Retry in ";

	public static final int CLIENT_NULL = 0;
	public static final int CLIENT_REGISTER = 1;
	public static final int CLIENT_DEREGISTER = 2;
	public static final int SERVER_SHUTDOWN = 3;
	public static final int CLIENT_SHUTDOWN = 4;
	public static final int TROTS_MESSAGE = 5;
	public static final int TROTS_UPDATE = 6;
	public static final int TROTS_ASSIGN = 7;
	public static final int TROTS_CANCEL = 8;
	public static final int CLIENT_STATUS = 9;
	public static final int QUEUE_NAMES = 10;
	public static final int CLIENT_RESET = 11;
	public static final int TROTS_MESSAGE_SEND=12;
	public static final int TROTS_MESSAGE_RECV=13;
	public static final int TROTS_ASSIGN_SEND=14;
	public static final int TROTS_ASSIGN_RECV=15;
	public static final int ALL_SHUTDOWN=16;
	public static final int SERVER_VERSION=17;
	public static final int MSG_PROCESSED = 99;
	public static final int HEARTBEAT = 1000;
	public static final int DEBUG_MAJOR = 1001;
	public static final int DEBUG_MINOR = 1002;
	public static final int DEBUG_DEBUG = 1003;
	public static final int DEBUG_ERROR = 1004;
	public static final int STATUS_HTML = 1005;
	public static final int ADD_QUEUE = 1006;
	public static final int DEL_QUEUE = 1007;
	public static final int CLEAN_USERS = 1008;
	public static final int LIST_USERS = 1009;
	public static final int USER_DETAILS = 1010;
	public static final int QUEUE_DETAILS = 1011;
	public static final int DEBUG_DETAILS = 1012;
	public static final int ACK = 54321;

	public static final String TROTS_INI_FILE = "trots.ini";
	public static final String SENDER_INI_FILE = "sender.ini";
	public static final String SERVER_INI_FILE = "server.ini";
	public static final String CONTROL_INI_FILE = "control.ini";

	public static final int CONNECTION_RETRY_TIME = 15;
	public static final int SENDER_TIMEOUT = 40;
	public static final int MESSAGE_TIMEOUT = 10;
	public static final int SOCKET_TIMEOUT = 10;

	public static final int MAX_CONNECTIONS = 500;

	public static final String CALL_RECEIPT_QUEUE="*Sender*";

	public static final int DEFAULT_PORT = 9876;
}
