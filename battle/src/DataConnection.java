import java.io.*;
import java.net.*;

public class DataConnection 
{
	Socket s;
	public DataInputStream dataIn;
	public DataOutputStream dataOut;
	public BufferedReader textIn;

	public DataConnection(String host, int port) throws IOException
	{
		s = new Socket(host, port);
		createIO();
	}

	public DataConnection(Socket clientSocket)
	{
		s = clientSocket;
		createIO();
	}
	
	private void createIO()
	{
		try
		{
			dataIn = new DataInputStream(s.getInputStream());
			dataOut = new DataOutputStream(s.getOutputStream());
			textIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
		}
		catch(IOException exIO) {}
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
