/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:09  dkelly


Revision 1.4  99/07/05  15:55:08  15:55:08  dkelly (Dave Kelly)
Forced checkin after benchmarking

Revision 1.3  99/04/19  10:31:10  10:31:10  dkelly (Dave Kelly)
Forced checkin

Revision 1.2  99/02/26  12:24:54  12:24:54  dkelly (Dave Kelly)
Added RCS statements

*/
import java.util.*;
//import java.net.*;
import java.io.*;
//import java.awt.event.*;
//import java.text.*;

public class Debug
{
	private File debugFile;
	private DataOutputStream debugOut;
	private int currentLevel;
	private long startTime;

	public static final int ERROR = 0;
	public static final int MAJOR = 1;
	public static final int MINOR = 2;
	public static final int DEBUG = 4;

        private String zeropad(long number, int numZeros)
        {
                return String.format("%0" + (numZeros + 1) + "d", number);
        }

        public Debug(int initialLevel, String fileName, String title)
	{
		try
		{
			startTime = System.currentTimeMillis() / 1000;
			currentLevel = initialLevel;
			debugFile = new File(fileName);
			debugOut = new DataOutputStream(new FileOutputStream(debugFile));
	
			if(title.length() > 0)
				trace(0, title + " ("+new Date() + ")\n");
		}
		catch(IOException exIO)
		{
			System.out.println("Unable to set debugging");
			System.out.println("Error: " + exIO.getMessage());
		}
	}

	public void trace(int messageLevel, String message)
	{

		long currentTime = System.currentTimeMillis() / 1000;
		String msg;

		if(messageLevel > currentLevel) return;
		try
		{
			msg = "+" + zeropad(currentTime-startTime , 8) + " " + message ;
			debugOut.writeBytes(msg);
			debugOut.writeBytes("\n");
			debugOut.flush();
		}
		catch(IOException exIO)
		{
			System.out.println("Unable to write to debug file ["+debugFile+"]");
			System.out.println("Error: " + exIO.getMessage());
		}
	}

	public void setLevel(int newLevel)
	{
		currentLevel = newLevel;
	}

	public int getLevel()
	{
		return currentLevel;
	}
}
