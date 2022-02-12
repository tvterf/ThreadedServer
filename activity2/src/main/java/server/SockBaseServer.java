package server;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import java.lang.*;
/*
import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;
*/

class SockBaseServer {
    
    ServerSocket serv = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;
    StringList leader;

    public static void main (String args[]) throws Exception {
        Game game = new Game();
        StringList leader = new StringList();
        leader.readFile();
        System.out.println("Accepting Requests...");

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        while(true){

        clientSocket = serv.accept();
        ThreadedWorker worker = new ThreadedWorker(clientSocket, game, leader);
        worker.start();

        }
    }
}

