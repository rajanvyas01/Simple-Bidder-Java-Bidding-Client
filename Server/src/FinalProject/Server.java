/*
 *  EE422C Final Project submission by
 *  Replace <...> with your actual data.
 *  <Rajan Vyas>
 *  <rv23454>
 *  <16160>
 *  Fall 2020
 */
package FinalProject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Scanner;

public class Server extends Observable {
    static Server server;
    static HashMap<String, Item> itemHashMap = new HashMap<>();

    public static void main (String [] args) throws FileNotFoundException {
        server = new Server();
        server.populateItems();
        server.SetupNetworking();
    }

    private void populateItems() throws FileNotFoundException {
        ArrayList<String> items = new ArrayList<>();
        ArrayList<Integer> maxBid = new ArrayList<>();
        File file = new File("Items.txt");
        Scanner scan = new Scanner(file);
        int lineCounter = 0;
        while(scan.hasNextLine()){
            if(lineCounter == 0) {
                items.add(scan.nextLine());
                lineCounter++;
            }
            else if(lineCounter == 1){
                maxBid.add(Integer.parseInt(scan.nextLine()));
                lineCounter = 0;
            }
        }

        for(int i = 0; i < items.size(); i++){
            Item newItem =  new Item(items.get(i), maxBid.get(i), (maxBid.get(i)*0.1), true);
            itemHashMap.put(items.get(i), newItem);
        }

    }

    private void SetupNetworking() {
        int port = 4242;
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket clientSocket = ss.accept();
                ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
                Thread t = new Thread(new ClientHandler(clientSocket, writer));
                t.start();
                addObserver(writer);
                System.out.println("got a connection");
            }
        } catch (IOException e) {}
    }

    class ClientHandler implements Runnable {
        private ObjectInputStream reader;
        private  ClientObserver writer; // See Canvas. Extends ObjectOutputStream, implements Observer
        Socket clientSocket;

        public ClientHandler(Socket clientSocket, ClientObserver writer) {
            try {
                this.clientSocket = clientSocket;
                this.writer = writer;
                reader = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                writer.writeObject(itemHashMap);
                while(true){
                    Item clientBid = (Item) reader.readObject(); //move out of synchronize to allow connections from multiple clients
                    synchronized (server) {
                        Integer validity;
                        Item itemStatus = itemHashMap.get(clientBid.getName());
                        if (!itemStatus.getListStatus()) {
                            validity = 1;
                        } else if (itemStatus.getCurrentBid() >= clientBid.getCurrentBid()) {
                            validity = 2;
                        } else if (itemStatus.getMaxBid() <= clientBid.getCurrentBid()) {
                            validity = 3;
                            clientBid.setListStatus(false);
                        } else {
                            validity = 0;
                        }
                        Message validCheck = new Message("int", clientBid, validity);
                        writer.reset();
                        writer.writeObject(validCheck);
                        writer.flush();

                        if(validity == 0 | validity == 3){
                            itemHashMap.replace(clientBid.getName(), clientBid);
                            Message updateItem = new Message("item", clientBid, validity);
                            setChanged();
                            notifyObservers(updateItem);
                         }
                    }
                }
            } catch (ClassNotFoundException e ) {
                e.printStackTrace();
            }
            catch(IOException e){
                //clientSocket.close(); commented out to avoid exceptions
                System.out.println("A Connection Closed"); //for debugging
            }

        }
    } // end of class ClientHandler
}