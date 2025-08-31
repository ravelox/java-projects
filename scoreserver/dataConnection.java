/*
$Log$
Revision 1.1  2003/10/10 10:16:47  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.3  99/04/19  10:31:08  10:31:08  dkelly (Dave Kelly)
Forced checkin

Revision 1.2  99/02/26  12:24:53  12:24:53  dkelly (Dave Kelly)
Added RCS statements

*/
import java.io.*;
import java.lang.*;
import java.net.*;

public class dataConnection 
{
	Socket s;
	public DataInputStream dataIn;
	public DataOutputStream dataOut;
	public BufferedReader textIn;

	public dataConnection(String host, int port) throws IOException
	{
		s = new Socket(host, port);
		dataIn = new DataInputStream(s.getInputStream());
		dataOut = new DataOutputStream(s.getOutputStream());
		textIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
	
	public void close()
	{
		try
		{
			textIn.close();
			dataOut.close();
			dataIn.close();
			s.close();
		}
		catch(IOException exIO) {}
	}
}
