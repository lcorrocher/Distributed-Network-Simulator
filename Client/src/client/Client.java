import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("wipe")) {
            handleWipeCommand();
            return;
        }

        if (args.length != 3) {
            System.err.println("Usage: java Client <src> <dst> <data>");
            System.exit(1);
        }

        handleNormalMessage(args);
    }

    private static void handleWipeCommand() {
        String hostName = "localhost";
        int portNumber = 8080;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("wipe");

            out.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Could not connect to server " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for server connection " + hostName);
            System.exit(1);
        }
    }

    private static void handleNormalMessage(String[] args) {
        String hostName = "localhost";
        int portNumber = 8080;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(args[0]);
            out.println(args[1]);
            out.println(args[2]);

            out.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Could not connect to server " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for server connection " + hostName);
            System.exit(1);
        }
    }
}