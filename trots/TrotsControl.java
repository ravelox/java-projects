/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.15  99/07/19  10:20:09  10:20:09  dkelly (Dave Kelly)
Added code to send SERVER_VERSION

Revision 1.14  99/07/16  14:10:49  14:10:49  dkelly (Dave Kelly)
Added wait for ACK from CLEAN_USERS command

Revision 1.13  99/07/15  16:36:05  16:36:05  dkelly (Dave Kelly)
Removed Call Receipt Admin commands

Revision 1.12  99/07/14  09:30:31  09:30:31  dkelly (Dave Kelly)
Changed location of ini file to be current directory

Revision 1.11  99/07/14  09:27:04  09:27:04  dkelly (Dave Kelly)
Fixed error message

Revision 1.10  99/07/13  12:49:59  12:49:59  dkelly (Dave Kelly)
Multiple changes to add extra administration function

Revision 1.9  99/07/07  11:31:51  11:31:51  dkelly (Dave Kelly)
Modified code to only reqest HTML status output

Revision 1.8  99/07/05  15:52:41  15:52:41  dkelly (Dave Kelly)
Added list_queues as a command

Revision 1.7  99/07/02  10:17:04  10:17:04  dkelly (Dave Kelly)
General code cleanup
Removed privates and statics

Revision 1.6  99/04/29  13:50:37  13:50:37  dkelly (Dave Kelly)
Added CLEAN_USERS as a command

Revision 1.5  99/04/22  16:29:18  16:29:18  dkelly (Dave Kelly)
Added RCS log

Revision 1.4  99/04/21  15:44:40  15:44:40  dkelly (Dave Kelly)
Added add_queue and del_queue 

Revision 1.3  99/04/19  10:31:19  10:31:19  dkelly (Dave Kelly)
Forced checkin

Revision 1.2  99/02/26  12:24:55  12:24:55  dkelly (Dave Kelly)
Added RCS statements

*/
import java.util.*;
import java.net.*;
import java.io.*;

public class TrotsControl
{
	static Properties props = new Properties();

        String availableCommands[] = {
                "server_shutdown", "debug_major", "debug_minor", "debug_debug",
                "debug_error", "status_html", "add_queue", "del_queue", "clean_users",
                "list_queues", "heartbeat","list_users",
                "debug_details", "all_shutdown", "user_details", "client_shutdown",
                "queue_details", "version", "server_version"};

	public boolean commandAllowed(String c)
	{
		for(int i=0; i < availableCommands.length; i++)
		{
			if(availableCommands[i].equalsIgnoreCase(c))
				return true;
		}
		return false;
	}

	public void showCommands()
	{
		for(int i=0; i < availableCommands.length; i++)
			System.out.println(availableCommands[i]);
	}

	public int makeInt(String value, int defaultValue)
	{
		int returnValue = defaultValue;
		try
		{
			returnValue = Integer.parseInt(value);
		}
		catch(NumberFormatException exNumFmt) {}

		return returnValue;
	}
		
        public TrotsControl(String host, int port, String command, String parm)
	{

		Socket s;
		DataOutputStream dataOut;
		DataInputStream dataIn;
		BufferedReader textIn;
		String textRead;

		if(!commandAllowed(command))
		{
			System.out.println("Commands available are:");
			showCommands();
			return;
		}

		try
		{
			s = new Socket(host, port);

			dataOut = new DataOutputStream(s.getOutputStream());
			dataIn = new DataInputStream(s.getInputStream());
			textIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			if(command.equalsIgnoreCase("version"))
			{
				System.out.println("Trots Control $Revision: 6 $");
			}
			if(command.equalsIgnoreCase("server_shutdown"))
			{
				dataOut.writeInt(Constants.SERVER_SHUTDOWN);
			}
			if(command.equalsIgnoreCase("all_shutdown"))
			{
				dataOut.writeInt(Constants.ALL_SHUTDOWN);
			}
                        if(command.equalsIgnoreCase("debug_major"))
                        {
                                dataOut.writeInt(Constants.DEBUG_MAJOR);
                        }
                        if(command.equalsIgnoreCase("debug_minor"))
                        {
                                dataOut.writeInt(Constants.DEBUG_MINOR);
                        }
                        if(command.equalsIgnoreCase("debug_debug"))
                        {
                                dataOut.writeInt(Constants.DEBUG_DEBUG);
                        }
                        if(command.equalsIgnoreCase("debug_error"))
                        {
                                dataOut.writeInt(Constants.DEBUG_ERROR);
                        }
			if(command.equalsIgnoreCase("status_html"))
			{
				dataOut.writeInt(Constants.STATUS_HTML);
				textRead = "";
				while( ! textRead.equalsIgnoreCase("</HTML>") )
				{
					textRead = textIn.readLine();
					System.out.println(textRead);
				}
			}
			if(command.equalsIgnoreCase("add_queue"))
			{
				dataOut.writeInt(Constants.ADD_QUEUE);
				dataOut.writeBytes(parm+"\n");
			}
			if(command.equalsIgnoreCase("del_queue"))
			{
				dataOut.writeInt(Constants.DEL_QUEUE);
				dataOut.writeBytes(parm+"\n");
			}
			if(command.equalsIgnoreCase("server_version"))
			{
				dataOut.writeInt(Constants.SERVER_VERSION);
				textRead = textIn.readLine();
				System.out.println(textRead);
			}
			if(command.equalsIgnoreCase("queue_details"))
			{
				dataOut.writeInt(Constants.QUEUE_DETAILS);
				dataOut.writeBytes(parm+"\n");
				textRead = textIn.readLine();
				System.out.println(textRead);
			}
			if(command.equalsIgnoreCase("clean_users"))
			{
				dataOut.writeInt(Constants.CLEAN_USERS);
				dataIn.readInt();
				System.out.println("Done");
			}

			if(command.equalsIgnoreCase("list_queues"))
			{
				dataOut.writeInt(Constants.QUEUE_NAMES);
				textRead = textIn.readLine();
				System.out.println(textRead);
			}
			if(command.equalsIgnoreCase("heartbeat"))
			{
				dataOut.writeInt(Constants.HEARTBEAT);
				if(dataIn.readInt() == Constants.ACK)
				{
					System.out.println("OK");
				}
				else
				{
					System.out.println("Something responded");
				}
			}
			if(command.equalsIgnoreCase("list_users"))
			{
				dataOut.writeInt(Constants.LIST_USERS);
				textRead = textIn.readLine();
				System.out.println(textRead);
			}
			if(command.equalsIgnoreCase("debug_details"))
			{
				dataOut.writeInt(Constants.DEBUG_DETAILS);
				switch(dataIn.readInt())
				{
					case Debug.ERROR:
						System.out.println("Errors only");
						break;
					case Debug.MAJOR:
						System.out.println("Major events");
						break;
					case Debug.MINOR:
						System.out.println("Minor events");
						break;
					case Debug.DEBUG:
						System.out.println("Debugging events");
						break;
					default:
						System.out.println("Unknown debug level");
						break;
				}
			}
			if(command.equalsIgnoreCase("user_details"))
			{
				dataOut.writeInt(Constants.USER_DETAILS);
				dataOut.writeInt(makeInt(parm,0));
				
				if(dataIn.readInt() > 0)
				{
					textRead = textIn.readLine();
					System.out.println(textRead);
				}
				else
				{
					System.out.println("User ID does not exist");
				}
			}
			if(command.equalsIgnoreCase("client_shutdown"))
			{
				dataOut.writeInt(Constants.CLIENT_SHUTDOWN);
				dataOut.writeInt(makeInt(parm,0));
			}
			dataOut.close();
			s.close();
		}
		catch(IOException exIO)
		{
			System.out.println("Unable to contact the server");
			System.out.println("Error: " + exIO.getMessage());
			System.exit(1);
		}
	}

	public static void main(String argv[])
	{

		String propServerHost, propServerPort;
		String parameter;

		if(argv.length != 1 && argv.length != 2)
		{
                    System.out.println("Usage: TrotsControl <command> [<parm>]");
			System.exit(1);
		}

		try
		{
			props.load(new FileInputStream(Constants.CONTROL_INI_FILE));

			propServerPort = props.getProperty("port");
			propServerHost = props.getProperty("server");

			if(propServerHost == null)
			{
				System.out.println("server property is not in " + Constants.CONTROL_INI_FILE);
				System.exit(1);
			}

			if(propServerPort == null)
			{
				System.out.println("port property is not in " + Constants.CONTROL_INI_FILE);
				System.exit(1);
			}

			if(argv.length == 1)
			{
				parameter = null;
			}
			else
			{
				parameter = argv[1];
			}
			
                    new TrotsControl(propServerHost, Integer.parseInt(propServerPort), argv[0], parameter);

		}
		catch(FileNotFoundException exFile)
		{
			System.out.println(exFile.getMessage());
		}
		catch(IOException exIO)
		{
			System.out.println("Problems loading properties from " + Constants.CONTROL_INI_FILE);
			System.out.println("Error: " + exIO.getMessage());
			System.exit(1);
		}
	}
}
