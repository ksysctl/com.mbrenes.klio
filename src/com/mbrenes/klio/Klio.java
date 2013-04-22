package com.mbrenes.klio;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Klio extends Activity {
    private static final String TAG = "Klio";

    private static final String CONFIG = "lastfm.json";

    ArrayList<String> trackList;
    ArrayAdapter<String> trackAdapter;
    ListView trackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_klio);

        // Add list view
        trackView = (ListView) findViewById(R.id.tracks);
        trackList = new ArrayList<String>();
        trackAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trackList);
        trackView.setAdapter(trackAdapter);

        // Build url from config file
        JSONObject config = Config.get(this, CONFIG);
        String url = "";
        try {
            Uri.Builder uri = Uri.parse(config.getString("server")).buildUpon();
            uri.path(config.getString("path"));
            uri.appendQueryParameter("method", "user.getrecenttracks");
            uri.appendQueryParameter("api_key", config.getString("key"));
            uri.appendQueryParameter("user", config.getString("user"));
            uri.appendQueryParameter("format", config.getString("format"));
            uri.appendQueryParameter("limit", "2");

            url = uri.build().toString();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        // Request task
        ScrobblesTask getScrobbles = new ScrobblesTask();
        getScrobbles.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_klio, menu);
        return true;
    }

    public class ScrobblesTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        public JSONObject doInBackground(String... urls) {
            // Execute request and wait for response
            JSONObject response = Client.doGet(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            // Fetch response when is ready
            int i;
            try {
                JSONArray tracks = new JSONArray(response.getJSONObject("recenttracks").getString("track"));
                for(i = 0; i < tracks.length(); i++) {
                    JSONObject track = tracks.getJSONObject(i);

                    // Adding recently tracks listened to the list
                    trackList.add(track.getString("name"));
                    trackAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
