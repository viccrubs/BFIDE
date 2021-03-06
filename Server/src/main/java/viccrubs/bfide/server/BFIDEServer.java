package viccrubs.bfide.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by viccrubs on 2017/5/6.
 */
public class BFIDEServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(20006);
        System.out.println("Waiting for connection");
        while(true){
            Socket client = server.accept();
            System.out.println("Connection established.");
            new Thread(new Controller(client)).start();
        }
    }
}
