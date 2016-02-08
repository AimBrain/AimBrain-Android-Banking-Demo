package com.aimbrain.bankingdemo.server;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;
import com.aimbrain.sdk.exceptions.InternalException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class DemoServer {

    private static final int SOCKET_TIMEOUT = 10000; //[ms]
    private static final int MAX_RETRIES = 1;
    private URL registerURL;
    private RequestQueue requestQueue;

    public DemoServer() {
        try {
            this.registerURL = new URL("https://demo.aimbrain.com:443/api/v2/register");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        setUpRequestQueue();
    }

    private void setUpRequestQueue() {
        Network network = new BasicNetwork(new HurlStack());
        this.requestQueue = new RequestQueue(new NoCache(), network);
        this.requestQueue.start();
    }

    public void register(String email, String interfaceUUID, String [] phoneAccounts, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) throws InternalException {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("device", android.os.Build.MODEL);
            jsonObject.put("system", "Android " + android.os.Build.VERSION.RELEASE);
            jsonObject.put("phoneAccounts", new JSONArray(Arrays.asList(phoneAccounts)));
            jsonObject.put("installuuid", interfaceUUID);
            Log.i("UUID", "json: " + jsonObject.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, registerURL.toString(), jsonObject, listener, errorListener);
            sendRequest(request);
        } catch (JSONException e) {
            throw new InternalException("Unable to create correct session request.");
        }
    }

    private void sendRequest(Request request)
    {
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }

    public void login(String loginURL, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loginURL, listener, errorListener);
        requestQueue.add(request);
    }
}
