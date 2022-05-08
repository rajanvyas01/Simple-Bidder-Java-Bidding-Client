/*
 *  EE422C Final Project submission by
 *  Replace <...> with your actual data.
 *  <Rajan Vyas>
 *  <rv23454>
 *  <16160>
 *  Fall 2020
 */

package FinalProject;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Client extends Application {
	// I/O streams
	ObjectOutputStream toServer = null;
	ObjectInputStream fromServer = null;

	static Message GUIupdate;
	static HashMap<String, Item> clientItemHashMap = new HashMap<>();
	public Label itemStatus = new Label("-- -- -- Select an Item to See its Current Auction Status -- -- --");
	public Label bidStatus = new Label("-- -- -- Waiting on a Bid to be Sent -- -- --");
	public TextField bidAmount = new TextField();
	public ComboBox<String> items = new ComboBox<>();

	@Override
	public void start(Stage primaryStage) {
		BorderPane rootPane = new BorderPane();
		BorderPane botPane = new BorderPane();
		VBox verticalPane = new VBox(30);
		BorderPane topPane = new BorderPane();
		BorderPane middlePane = new BorderPane();
		BorderPane lowerPane = new BorderPane();

		rootPane.setBottom(botPane);
		rootPane.setTop(verticalPane);


		// Create a scene and place it in the stage
		primaryStage.setTitle("Simple Bidder: Buyer's Window"); // Set the stage title

		verticalPane.getChildren().addAll(topPane, middlePane, lowerPane);

		//ComboBox<String> items = new ComboBox<>(); had to make public
		items.setValue("Items Up For Auction");
		//Label itemStatus = new Label("-- -- -- Select an Item to See its Current Auction Status -- -- --"); had to make public
		topPane.setLeft(items);
		topPane.setCenter(itemStatus);

		Button bid = new Button();
		bid.setText("Send Bid");
		//TextField bidAmount = new TextField(); had to make public
		bidAmount.setEditable(true);
		Label bidAmountLbl = new Label("  Bid Amount:  $");
		middlePane.setLeft(bidAmountLbl);
		middlePane.setCenter(bidAmount);
		middlePane.setRight(bid);

		Label bidStatusLbl = new Label("  Bid Status: ");
		//Label bidStatus = new Label("-- -- -- Waiting on a Bid to be Sent -- -- --"); had to make public
		lowerPane.setLeft(bidStatusLbl);
		lowerPane.setCenter(bidStatus);

		Button quit = new Button();
		quit.setText("Quit");
		botPane.setRight(quit);

		Scene scene = new Scene(rootPane, 1000, 150);
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage


		try {
			// Create a socket to connect to the server 
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 4242);

			// Create an input stream to receive data from the server 
			fromServer = new ObjectInputStream(socket.getInputStream());

			// Create an output stream to send data to the server 
			toServer = new ObjectOutputStream(socket.getOutputStream());

			//update client hashmap
			HashMap<String, Item> serverHashMap = (HashMap<String, Item>) fromServer.readObject();
			clientItemHashMap.putAll(serverHashMap);


			//update comboBox
			Iterator clientItemHashMapIterator = clientItemHashMap.entrySet().iterator();
			while(clientItemHashMapIterator.hasNext()){
				Map.Entry mapElement = (Map.Entry)clientItemHashMapIterator.next();
				items.getItems().add((String)mapElement.getKey());
			}

			//comboBox event for item status
			items.setOnAction(event -> {updateItemStatus(items.getValue());});
			
			//sendbid event for bid status
			bid.setOnAction(event -> {
				try {
					sendBid();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			//Quit Button Event
			quit.setOnAction(event -> {
				System.exit(0);
			});


		} catch (IOException | ClassNotFoundException ex) {
		}

		Thread readerThread = new Thread(new Runnable()  {

			@Override
			public void run() {
				Runnable setBidStatusLbl = new Runnable() {
					@Override
					public void run() {
						processRequest(GUIupdate);
					}
				};

				Message update;
				try{
					while ((update = (Message) fromServer.readObject()) != null) {
						GUIupdate = update;
						System.out.println("Client received Message");
						Platform.runLater(setBidStatusLbl);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		readerThread.start();


	}

	private void processRequest(Message update) {
		if(update.objectType.equals("item")){
			String itemName = update.itemInput.getName();
			clientItemHashMap.replace(itemName, update.itemInput);

			Boolean listStatus = clientItemHashMap.get(itemName).getListStatus();
			double currentBid = clientItemHashMap.get(itemName).getCurrentBid();
			double maxBid = clientItemHashMap.get(itemName).getMaxBid();
			if(listStatus){
				itemStatus.setText(itemName + " is up for auction. The current bid is set at $" + currentBid + ". $" + maxBid + " to buy now.");
			}
			else{
				itemStatus.setText(itemName + " has been sold for $" + currentBid + " and is no longer up for auction.");
			}
		}

		else {
			if(update.validity == 1){
				bidStatus.setText("Invalid Bid. The item is inactive and has been sold.");
			}
			else if(update.validity == 2){
				bidStatus.setText("Invalid Bid. Bid must be higher than the current bid.");
			}
			else if(update.validity == 3){
				bidStatus.setText("Valid Bid. You have met the buyout price and won the item.");
			}
			else if(update.validity == 0){
				bidStatus.setText("Valid Bid. You are the current highest bidder at $" + update.itemInput.getCurrentBid());
			}
		}

	}

	private void sendBid() throws IOException {
		String itemName = items.getValue();
		double bid = Double.parseDouble(bidAmount.getText());
		Item clientBid = new Item(clientItemHashMap.get(itemName).getName(), clientItemHashMap.get(itemName).getMaxBid(), bid, clientItemHashMap.get(itemName).getListStatus());
		clientBid.setCurrentBid(bid);
		toServer.reset();
		toServer.writeObject(clientBid);
		toServer.flush();
	}


	private void updateItemStatus(String itemName) {
		Boolean listStatus = clientItemHashMap.get(itemName).getListStatus();
		double currentBid = clientItemHashMap.get(itemName).getCurrentBid();
		double maxBid = clientItemHashMap.get(itemName).getMaxBid();

		if(listStatus){
			itemStatus.setText(itemName + " is up for auction. The current bid is set at $" + currentBid + ". $" + maxBid + " to buy now.");
		}

	}

	public static void main(String[] args) {
		launch(args);
	}

}
