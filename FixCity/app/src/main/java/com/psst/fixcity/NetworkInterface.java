package com.psst.fixcity;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class NetworkInterface {

    MapsActivity mMain;
    JSONArray firstEvent = null;
    JSONObject firstEventObject = null;

    public NetworkInterface(MapsActivity activity){
        mMain = activity;
    }

    private boolean checkReport(Report report){
        if(report.title == null || report.title.length() <= 0)
            return false;
        if(report.pos == null || report.user_id == null)
            return false;
        return true;
    }

    public boolean send(Report report){
        boolean valid_report = checkReport(report);

        if(!valid_report) return false;


        //Insert code that sends data
        RequestParams params = new RequestParams();
        params.put("title", report.title);
        params.put("description", report.desc);
        params.put("user_id", report.user_id);
        params.put("lattitude", report.pos.latitude);
        params.put("longitude", report.pos.longitude);
        NetworkInterfaceClient.post("events/new", params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Sandeep", String.valueOf(response));
            }
            public void onFailure(int statusCode, Header[] headers, JSONObject errorResponse, Throwable e) {
                Log.d("Sandeep", String.valueOf(errorResponse));
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });

        return true;
    }


    public JSONArray get(){

        NetworkInterfaceClient.get("events", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Sandeep", String.valueOf(response));
                firstEvent.put(response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                Log.d("Sandeep", String.valueOf(timeline));
                // Pull out the first event on the public timeline

                firstEvent = (JSONArray) timeline;
                Log.d("Sandeep", String.valueOf(firstEvent));

                try {
                    mMain.fillReports(firstEvent);
                } catch (Exception e) {
                    Log.d("Sandeep", "Exception at fill Reports");
                }
            }
        });
        return firstEvent;
    }

    public JSONObject getWithID(String id){
        NetworkInterfaceClient.get("events/new/"+id, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Sandeep", String.valueOf(response));
                firstEventObject = response;

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                Log.d("Sandeep", String.valueOf(timeline));
                // Pull out the first event on the public timeline
                firstEvent = (JSONArray) timeline;
                Log.d("Sandeep", String.valueOf(firstEvent));

            }
        });

        return firstEventObject;
    }

}


