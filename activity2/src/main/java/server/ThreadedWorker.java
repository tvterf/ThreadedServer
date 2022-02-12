/**
  File: ThreadedWorker.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package server;


import org.json.JSONObject;
import org.json.JSONArray;


import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;
import java.lang.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

/**
 * Class: ThreadedWorker 
 * Description: Threaded Performer for server tasks.
 */
class ThreadedWorker extends Thread {

    static String logFilename = "logs.txt";
    static String leaderFilename = "leader.txt";
    private Game state;
    private Socket conn;
    private String clientName;
    boolean gameStarted = false;
    boolean hasGreeted = false;
    int multiplier = 1;
    int logins = 0;
    int wins = 0;
    private StringList leaders;

    public ThreadedWorker(Socket sock, Game game, StringList leader ) {
        this.conn = sock;
        this.state = game;
        this.leaders = leader;
    }

    // writing a connect message to the log with name and CONNENCT
    public Response greet(String name){
        clientName = name;
    writeToLog(name, Message.CONNECT);
    logins++;
    
        leaders.add(clientName + " " + wins);
    
    
    System.out.println("Got a connection and a name: " + name);
    Response response = Response.newBuilder()
            .setResponseType(Response.ResponseType.GREETING)
            .setMessage("Hello " + name + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to Exit")
            .build();
            return response;
    }

    public Response mainMenu(){
        
        Response response = Response.newBuilder()
                .setResponseType(Response.ResponseType.GREETING)
                .setMessage("What would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to Exit")
                .build();
                return response;
        }

    
    public Response leaderBoard(){
        //writeToLog(name, Message.CONNECT);
        /*
        System.out.println("Viewing the LeaderBoard");
        Response response = Response.newBuilder()
                .setResponseType(Response.ResponseType.LEADER)
                .setTask("Display LeaderBoard")
                .setMessage("What would you like to do? \n 2 - to enter a game \n 3 - to Exit")
                .build();
                */
                Response response = Response.newBuilder()
            .setResponseType(Response.ResponseType.LEADER)
            .setTask("Displaying LeaderBoard")
                .setMessage("What would you like to do? \n 2 - to enter a game \n 3 - to Exit")
                .setImage(leaders.toString())
                .build();

                /*
            // building and Entry
            Entry leader = Entry.newBuilder()
                .setName(clientName)
                .setWins(wins)
                .setLogins(logins)
                .build();

            res.addLeader(leader);

            Response response3 = res.build();
            */

                return response;
        }

    public Response newClientGame(){
            writeToLog(clientName, Message.START);
            System.out.println("Starting new Game");
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.TASK)
                    .setImage(state.getImage())
                    .setTask("Enter t: ")
                    .build();
                    return response;
            }

    public Response currGame(){
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.TASK)
                    .setImage(state.getImage())
                    .setTask("Enter t: ")
                    .build();
                    return response;
            }
    public Response correct(){
        try{
            multiplier+=2;
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.TASK)
                    .setImage(replace(4*multiplier))
                    .setTask("Enter t: ")
                    .build();/*
                    if(state.getImage().equals(state.getOriginalImage())){
                        
                        Response winResponse = winner();
                        multiplier = 1;
                        return winResponse;
                        
                    }  
                    */
                    

                    return response;
        }catch(java.lang.ArrayIndexOutOfBoundsException e){
            
            Response winResponse = winner();
            multiplier = 1;
            return winResponse;
        }
            }

    public Response winner(){
                gameStarted = false;
                leaders.updateElement(leaders.getIndex(clientName + " " + wins), clientName + " " + ++wins);
                try (PrintWriter out = new PrintWriter(leaderFilename)) {
                    out.println(leaders.toString());
                } catch(FileNotFoundException e)
                {
                    System.out.println(e);
                }
                //wins++;
                state.setWon();
                writeToLog(clientName, Message.WIN);
                System.out.println("Client won the game");
                Response response = Response.newBuilder()
                        .setResponseType(Response.ResponseType.WON)
                        .setImage(state.getOriginalImage())
                        .setTask("You won!")
                        .setMessage("What would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to Exit")
                        .build();
                        
                        return response;
                }
    public Response startError(){
            //writeToLog(name, Message.CONNECT);
            System.out.println("Error with input");
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.ERROR)
                    .setMessage("What would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to Exit")
                    .build();
                    return response;
            }
    public Response gameError(){
            //writeToLog(name, Message.CONNECT);
            System.out.println("Error with input");
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.ERROR)
                    .setImage(state.getImage())
                    .setTask("Enter t: ")
                    .build();
                    return response;
            }

    public Response quit(){
            //writeToLog(name, Message.CONNECT);
            System.out.println("Client quit Game");
            Response response = Response.newBuilder()
                    .setResponseType(Response.ResponseType.BYE)
                    .setMessage("Ending Connection")
                    .build();
                    return response;
            }

    
    
    

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void begin() {
        String name = "";
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        JSONObject playerInfo = new JSONObject();


        System.out.println("Ready...");
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");

            String result = null;
            Response response = null;
            String answer = "";
            

            while(!quit){
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                
                 if(!gameStarted){
                     try{
                    switch (op.getOperationType()) {
                        case NAME: //NAME
                        if(hasGreeted == false){
                        name = op.getName();
                            
                        response = greet(name);
                        hasGreeted = true;
                        }
                        else{
                            response = mainMenu();
                        }
                        break;
                        case LEADER: //LEADERBOARD
                        response = leaderBoard();

                        break;
                        case NEW: //NEW
                        state.newGame();
                        response = newClientGame();
                        gameStarted = true;
                        break;
                        case QUIT: //QUIT
                        System.out.println("quitting here:");
                        response = quit();
                        quit = true;
                        break;
                        default:
                        System.out.println("start Error:");
                            response = startError();
                            break;    
                         }
                        }catch(java.lang.NullPointerException e){
                            
                            quit = true;
                        }
                    }
                      else if(gameStarted){
                          try{
                    switch (op.getOperationType()) {
                        
                        case ANSWER: //ANSWER
                        //answer = op.getAnswer();
                        switch(op.getAnswer()){
                            case "t":
                            System.out.println("entered t");
                            response = correct();
                            break;
                            case "3":
                            System.out.println("quitting");
                            response = quit();
                            quit = true;
                            break;
                            default:
                            System.out.println("game Error:");
                            response = gameError();
                            break;
                        }
                        break;

                            /*
                            if(game.checkWinner()){
                            response = winner();
                            gameStarted = false;
                            }
                            */
                        
                        case QUIT: //QUIT
                        System.out.println("quitting from here");
                        response = quit();
                        quit = true;
                        break;
                        default:
                        System.out.println("game Error:");
                        response = gameError();
                            break;    
                         }
                        }catch(java.lang.NullPointerException e){
                            
                            quit = true;
                        }
                        }
                         
                         response.writeDelimitedTo(out);
            }
            // close the resource
            System.out.println("Client ended connection ");
            System.out.println("closing the resources of client ");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        /*    

            // if the operation is NAME (so the beginning then say there is a commention and greet the client)
            if (op.getOperationType() == Request.OperationType.NAME) {
                // get name from proto object
            

            // writing a connect message to the log with name and CONNENCT
            writeToLog(name, Message.CONNECT);
                System.out.println("Got a connection and a name: " + name);
                Response response = Response.newBuilder()
                        .setResponseType(Response.ResponseType.GREETING)
                        .setMessage("Hello " + name + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game")
                        .build();
                response.writeDelimitedTo(out);
            }

            // Example how to start a new game and how to build a response with the image which you could then send to the server
            // LINE 67-108 are just an example for Protobuf and how to work with the differnt types. They DO NOT
            // belong into this code. 
            game.newGame(); // starting a new game

            // adding the String of the game to 
            Response response2 = Response.newBuilder()
                .setResponseType(Response.ResponseType.TASK)
                .setImage(game.getImage())
                .setTask("Great task goes here")
                .build();

            // On the client side you would receive a Response object which is the same as the one in line 70, so now you could read the fields
            System.out.println("Task: " + response2.getResponseType());
            System.out.println("Image: \n" + response2.getImage());
            System.out.println("Task: \n" + response2.getTask());

            // Creating Entry and Leader response
            Response.Builder res = Response.newBuilder()
                .setResponseType(Response.ResponseType.LEADER);

            // building and Entry
            Entry leader = Entry.newBuilder()
                .setName("name")
                .setWins(0)
                .setLogins(0)
                .build();

            // building and Entry
            Entry leader2 = Entry.newBuilder()
                .setName("name2")
                .setWins(1)
                .setLogins(1)
                .build();

            res.addLeader(leader);
            res.addLeader(leader2);

            Response response3 = res.build();

            for (Entry lead: response3.getLeaderList()){
                System.out.println(lead.getName() + ": " + lead.getWins());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null)  out.close();
            if (in != null)   in.close();
            if (clientSocket != null) clientSocket.close();
        }
    }
*/

    /**
     * Replaces num characters in the image. I used it to turn more than one x when the task is fulfilled
     * @param num -- number of x to be turned
     * @return String of the new hidden image
     */
    public String replace(int num){
        for (int i = 0; i < num; i++){
           // if (state.getIdx()< state.getIdxMax())
            state.replaceOneCharacter();
        }
        return state.getImage();
    }


    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }
   

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

   

    public void run(){
        begin();
    }

}
