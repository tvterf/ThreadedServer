package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    public Request requestBuilder(String selected, String data){
        Request op = null;
        if(selected == "0"){
             op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(data).build();
        }else{
             op = Request.newBuilder()
                .setOperationType(Request.OperationType.ANSWER)
                .setAnswer(data).build();      
        }
        return op;

    }

    public Request requestBuilder(String selected){
        Request op = null;
        System.out.println("Building a request..");
        if(selected == "1"){
            System.out.println("selected leader board");
             op = Request.newBuilder()
                .setOperationType(Request.OperationType.LEADER)
                .build();
        }
        else if(selected == "2"){
         op = Request.newBuilder()
            .setOperationType(Request.OperationType.NEW)
            .build();
        }
        else {
         op = Request.newBuilder()
            .setOperationType(Request.OperationType.QUIT)
            .build();
        }
        return op;
    }

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1=0, i2=0;
        int port = 9099; // default port
        Boolean gameStarted = false;

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server. ( ͡❛ ͜ʖ ͡❛)");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();
            op.writeDelimitedTo(out);
            
            do{
                // read from the server
                 response = Response.parseDelimitedFrom(in);
                 if(!gameStarted){
                    switch (response.getResponseType()) {
                        case GREETING: //GREETING
                        System.out.println(response.getMessage());
                        break;
                        case LEADER: //LEADER
                        System.out.println(response.getTask());
                        System.out.println(response.getImage());
                        /*
                        for (Entry lead: response.getLeaderList()){
                            System.out.println(lead.getName() + ": " + lead.getWins());
                        }
                        */
                        System.out.println(response.getMessage());
                        
                        break;
                        case TASK: //TASK
                        System.out.println("Image: \n" + response.getImage());
                        System.out.println("Task: " + response.getTask());
                        gameStarted = true;
                        break;
                        case ERROR: //ERROR
                        System.out.println(response.getMessage());
                        break;
                        case BYE: //BYE
                        System.out.println(response.getMessage());
                        break;
                        default:
                        System.out.println("Invalid selection: " + response.getResponseType() 
                        + " is not an option");
                            break;    
                         }
                        }
                       else if(gameStarted){
                    switch (response.getResponseType()) {
                        case TASK: //TASK
                        System.out.println("Image: \n" + response.getImage());
                        System.out.println("Task: " + response.getTask());
                        break;
                        case WON: //WON
                        System.out.println(response.getImage());
                        System.out.println(response.getTask());
                        System.out.println(response.getMessage());
                        gameStarted = false;
                        break;
                        case ERROR: //ERROR
                        System.out.println("Image: \n" + response.getImage());
                        System.out.println("Task: " + response.getTask());
                        break;
                        default:
                        System.out.println(response.getMessage());
                        System.exit(0); 
                            break;    
                         }
                        }
                // print the server response. 
            //System.out.println(response.getMessage());
            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
            String selected = sysin.readLine();
            op = null;
            System.out.println("Entered: " + selected);
            if(!gameStarted){
            switch (selected) {
                case "1": //leader
                System.out.println("selected leader board");
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.LEADER)
                .build();
                break;
                case "2": //New game
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.NEW)
                .build();
                break;
                case "3": //Quit
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.QUIT)
                .build();
                op.writeDelimitedTo(out);  
                System.exit(0);
                break;
                default:
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .build();
                    break;    
                 }
                }
                else if(gameStarted){

            
               if(selected == "3"){
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.QUIT)
                .build();
                op.writeDelimitedTo(out);  
               System.exit(0);
               }
               
               else{
                op = Request.newBuilder()
                .setOperationType(Request.OperationType.ANSWER)
                .setAnswer(selected)
                .build(); 
               }   
                 
                }
                 op.writeDelimitedTo(out);  

            } while(true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Disconnecting. Bye Bye");
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }
}


