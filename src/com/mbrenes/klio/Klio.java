package com.mbrenes.klio;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Klio extends Activity {
    private static final String TAG = "Klio";

    private static final String CONFIG = "lastfm.json";

    private ArrayList<HashMap<String, String>> trackList;
    private SimpleAdapter trackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_klio);

        ListView trackView = null;
        Uri.Builder uri = null;
        JSONObject config = null;
        String url = null;

        // Set adapter
        trackList = new ArrayList<HashMap<String, String>>();
        trackAdapter = new SimpleAdapter(
            this,
            trackList,
            R.layout.track_item,
            new String[] {
                "trackName",
                "trackDetail",
                "trackDate"
            },
            new int[] {
                R.id.trackName,
                R.id.trackDetail,
                R.id.trackDate
            }
        );
        trackView = (ListView) findViewById(R.id.trackView);
        trackView.setAdapter(trackAdapter);

        // Build url from config file
        config = Config.get(this, CONFIG);
        try {
            uri = Uri.parse(config.getString("server")).buildUpon();
            uri.path(config.getString("path"));
            uri.appendQueryParameter("method", "user.getrecenttracks");
            uri.appendQueryParameter("api_key", config.getString("key"));
            uri.appendQueryParameter("user", config.getString("user"));
            uri.appendQueryParameter("format", config.getString("format"));
            uri.appendQueryParameter("limit", config.getString("limit"));

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
            JSONArray tracks = null;
            JSONObject track = null;
            HashMap<String, String> map = null;

            int i;

            try {
                tracks = new JSONArray(
                    response.getJSONObject("recenttracks").getString("track")
                );

                for(i = 0; i < tracks.length(); i++) {
                    track = tracks.getJSONObject(i);
                    map = new HashMap<String, String>();

                    // Adding recently tracks listened to the list
                    map.put("trackName", track.getString("name"));

                    map.put(
                        "trackDetail",
                        String.format(
                            "from %s by %s",
                            track.getJSONObject("album").getString("#text"),
                            track.getJSONObject("artist").getString("#text")
                        )
                    );

                    if (track.isNull("date")) {
                        map.put("trackDate", "playing now");
                    } else {
                        map.put(
                            "trackDate",
                            String.format(
                                "at %s",
                                track.getJSONObject("date").getString("#text")
                            )
                        );
                    }

                    trackList.add(map);
                    trackAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
