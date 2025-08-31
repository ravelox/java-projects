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
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DataConnection implements Closeable
{
        private final Socket socket;
        public final DataInputStream dataIn;
        public final DataOutputStream dataOut;
        public final BufferedReader textIn;

        public DataConnection(String host, int port) throws IOException
        {
                socket = new Socket(host, port);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                textIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void close() throws IOException
        {
                textIn.close();
                dataOut.close();
                dataIn.close();
                socket.close();
        }
}
