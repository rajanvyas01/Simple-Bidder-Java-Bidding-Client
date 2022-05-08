/*
 *  EE422C Final Project submission by
 *  Replace <...> with your actual data.
 *  <Rajan Vyas>
 *  <rv23454>
 *  <16160>
 *  Fall 2020
 */

package FinalProject;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private double maxBid;
    private double currentBid;
    private Boolean listStatus;


    public Item(String name, double maxBid, double currentBid, Boolean listStatus) {
        this.name = name;
        this.maxBid = maxBid;
        this.currentBid = currentBid;
        this.listStatus = listStatus;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMaxBid(double maxBid){
        this.maxBid = maxBid;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public void setListStatus(boolean listStatus) {
        this.listStatus = listStatus;
    }

    public boolean getListStatus(){
        return listStatus;
    }
}