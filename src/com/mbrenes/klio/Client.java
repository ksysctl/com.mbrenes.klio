package com.mbrenes.klio;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

public class Client {
    private static final String TAG = "Klio.Client";

    private static final String USER_AGENT = "Klio/1.0 (Android)";

    public static JSONObject doGet(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("User-Agent", USER_AGENT);
        httpGet.setHeader("Accept", "application/json");

        JSONObject object = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line);
                }
                JSONTokener tokener = new JSONTokener(builder.toString());

                try {
                    object = new JSONObject(tokener);
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Log.e(TAG, Integer.toString(statusLine.getStatusCode()));
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return object;
    }
}
