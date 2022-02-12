package server;

import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;


/**
 * Class: StringList
 * Description: Shared Resource amongst clients.
 */
class StringList {

    List<String> strings = new ArrayList<String>();

    public synchronized void readFile(){
        try{
            Scanner inFile1 = new Scanner(new File("leader.txt"));   
            String leaders = inFile1.nextLine();
            String[] sLead = leaders.split("]");

            for(int i =0; i < sLead.length;i++){
                strings.add(sLead[i]);
            }
            
        //List<String> strings = Files.readAllLines(Paths.get("leader.txt"), StandardCharsets.UTF_8);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
   
   

    //List<String> strings = Files.readAllLines(Paths.get("leader.json"), StandardCharsets.UTF_8);
    

    public synchronized void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }
    public synchronized boolean contains(String str) {
        return strings.indexOf(str) >= 0;
    }

    public synchronized int getIndex(String str) {
        return strings.indexOf(str);
    }

    public synchronized int size() {
        return strings.size();
    }

    public synchronized String toString() {
        return strings.toString();
    }

    public synchronized String pop(){
        if(size() == 0){
            return "null";
        }
        return strings.remove(0);
    }


    public synchronized void updateElement(int x, String update){
        strings.set(x, update);
    }
}