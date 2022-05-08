package FinalProject;

import java.io.Serializable;

public class Message implements Serializable {
    String objectType;
    Item itemInput;
    Integer validity;


    protected Message() {
        this.objectType = "";
        this.itemInput = null;
        this.validity = null;
        System.out.println("Client-Side message created.");
    }

    protected Message(String objectType, Item itemInput, Integer validity){
        this.objectType = objectType;
        this.itemInput = itemInput;
        this.validity = validity;
    }
}