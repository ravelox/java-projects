package battleship;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class BattleServer {
    private static class User {
        int id;
        String name;
        DataInputStream dataIn;
        DataOutputStream dataOut;
        BufferedReader textIn;

        User(DataInputStream dis, DataOutputStream dos, BufferedReader ti) {
            id = 0;
            dataIn = dis;
            dataOut = dos;
            textIn = ti;
            name = "";
        }

        void setID(int i) {
            id = i;
        }

        int getID() {
            return id;
        }

        void setName(String n) {
            name = n;
        }

        String getName() {
            return name;
        }
    }

    private ServerSocket server;
    private Socket client;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private BufferedReader textIn;
    private User[] users;
    private int command;
    private int numClients;

    static int makeInt(String value, int defaultValue) {
        int returnValue = defaultValue;
        try {
            returnValue = Integer.parseInt(value);
        } catch (NumberFormatException exNumFmt) {
            // ignore
        }
        return returnValue;
    }

    private void process_register() {
        String playerName;
        User u = new User(dataIn, dataOut, textIn);

        if (numClients == 2) {
            try {
                dataOut.writeInt(Constants.REPLY_SERVER_FULL);
            } catch (IOException exIO) {
                System.out.println("process_register: " + exIO.getMessage());
            }
        } else {
            users[numClients] = u;

            try {
                dataOut.writeInt(++numClients);

                // Get the player name
                playerName = textIn.readLine();
                u.setName(playerName);

                if (numClients == 2) {
                    // Send the opponents names to each other
                    users[0].dataOut.writeInt(Constants.COMMAND_OPPONENT_NAME);
                    users[0].dataOut.writeBytes(users[1].name + "\n");

                    users[1].dataOut.writeInt(Constants.COMMAND_OPPONENT_NAME);
                    users[1].dataOut.writeBytes(users[0].name + "\n");

                    // Wait for an ACK from each
                }
            } catch (IOException exIO) {
                System.out.println("process_register: " + exIO.getMessage());
            }
        }
    }

    private void process_deregister() {
        try {
            int tmpClientID = dataIn.readInt();
        } catch (IOException exIO) {
            System.out.println("process_deregister: " + exIO.getMessage());
        }
    }

    private void process_shutdown() { }
    private void process_get_state() { }
    private void process_fire() { }
    private void process_hit() { }
    private void process_miss() { }
    private void process_sunk() { }
    private void process_lose() { }
    private void process_submit() { }

    public BattleServer(int port) {
        users = new User[2];

        numClients = 0;
        System.out.println("Battle Server: " + port);
        try {
            // Create the server socket
            server = new ServerSocket(port);

            while (command != Constants.COMMAND_SHUTDOWN) {
                try {
                    // Wait for client command
                    client = server.accept();

                    // Build input/output streams
                    dataIn = new DataInputStream(client.getInputStream());
                    dataOut = new DataOutputStream(client.getOutputStream());
                    textIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    // Set socket timeout
                    client.setSoTimeout(Constants.SOCKET_TIMEOUT * 1000);

                    // Get the client command
                    command = dataIn.readInt();

                    switch (command) {
                        case Constants.COMMAND_REGISTER:
                            process_register();
                            break;
                        case Constants.COMMAND_DEREGISTER:
                            process_deregister();
                            break;
                        case Constants.COMMAND_SHUTDOWN:
                            process_shutdown();
                            break;
                        case Constants.COMMAND_GET_STATE:
                            process_get_state();
                            break;
                        case Constants.COMMAND_FIRE:
                            process_fire();
                            break;
                        case Constants.COMMAND_HIT:
                            process_hit();
                            break;
                        case Constants.COMMAND_MISS:
                            process_miss();
                            break;
                        case Constants.COMMAND_SUNK:
                            process_sunk();
                            break;
                        case Constants.COMMAND_LOSE:
                            process_lose();
                            break;
                        case Constants.COMMAND_SUBMIT:
                            process_submit();
                            break;
                    }
                    System.gc();
                } catch (IOException exIO) {
                    // ignore
                }
            }
        } catch (IOException exIO) {
            // ignore
        }
        System.out.println("Battle Server: Normal Termination");
    }
}

