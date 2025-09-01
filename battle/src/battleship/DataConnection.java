package battleship;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DataConnection {
    private final Socket socket;
    public final DataInputStream dataIn;
    public final DataOutputStream dataOut;
    public final BufferedReader textIn;

    public DataConnection(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public DataConnection(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
        this.dataIn = new DataInputStream(socket.getInputStream());
        this.dataOut = new DataOutputStream(socket.getOutputStream());
        this.textIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void close() {
        try {
            textIn.close();
            dataOut.close();
            dataIn.close();
            socket.close();
        } catch (IOException exIO) {
            // ignore
        }
    }
}
