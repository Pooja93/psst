package com.psst.fixcity;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by theo on 1/28/18.
 */

public class Report {
    public int votes;
    public String title, desc, user_id;
    public LatLng pos;
    public Bitmap img;

    public Report(){}

    public Report(String title, String desc, String user_id, LatLng pos, Bitmap img){
        this.title = title;
        this.desc = desc;
        this.user_id = user_id;
        this.pos = pos;
        if(img != null)
            this.img = Bitmap.createBitmap(img);
    }
    public String print(){
        return "Title: "+title+
                "\nDesc: "+desc+
                "\nuserId "+user_id+
                "\nPos: "+pos.latitude+","+pos.longitude;
    }

}
