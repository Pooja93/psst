package com.psst.fixcity;

/**
 * Created by theo on 1/28/18.
 */

public class NetworkInterface {

    public NetworkInterface(){

    }


    public boolean send(String title, String description, String user_id, float latitude, float longitude){
        boolean valid_inputs = checkInputs();

        if(!valid_inputs) return false;

        //Insert code that sends data

        return true;
    }

    private boolean checkInputs(){
        return false;
    }
}
