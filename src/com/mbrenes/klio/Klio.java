package com.mbrenes.klio;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Log;
import android.app.ProgressDialog;

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
        SharedPreferences preferences = null;
        ScrobblesTask getScrobbles = null;
        Uri.Builder uri = null;
        JSONObject config = null;
        String url = null;
        String limit = null;
        String user = null;

        // Get prefences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Set list view
        trackView = (ListView) findViewById(R.id.trackView);
        trackView.setAdapter(trackAdapter);

        // Build url from config file
        config = Config.get(this, CONFIG);
        user = preferences.getString(
            "user",
            getResources().getString(R.string.preferences_user_default)
        );
        limit = preferences.getString(
            "limit",
            getResources().getString(R.string.preferences_limit_default)
        );

        try {
            uri = Uri.parse(config.getString("server")).buildUpon();
            uri.path(config.getString("path"));
            uri.appendQueryParameter("method", "user.getrecenttracks");
            uri.appendQueryParameter("api_key", config.getString("key"));
            uri.appendQueryParameter("format", config.getString("format"));
            uri.appendQueryParameter("user", user);
            uri.appendQueryParameter("limit", limit);

            url = uri.build().toString();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        // Request task
        getScrobbles = new ScrobblesTask(this);
        getScrobbles.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.activity_klio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Set menu actions
        Intent settings = null;

        switch (item.getItemId()) {
            case R.id.menu_settings:
                settings = new Intent(getBaseContext(), Settings.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class ScrobblesTask extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog progressDialog ;
        private WeakReference<Klio> context;

        public ScrobblesTask(Klio activity) {
            context = new WeakReference<Klio>(activity);
        }

        @Override
        protected void onPreExecute() {
            // Set progress dialog
            Klio activity = context.get();

            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(activity.getResources().getString(R.string.progress_message));
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        public JSONObject doInBackground(String... urls) {
            // Execute request and wait for response
            JSONObject response = Client.doGet(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            // Fetch response when is ready
            Klio activity = context.get();
            progressDialog.dismiss();

            JSONArray tracks = null;
            JSONObject track = null;
            HashMap<String, String> map = null;
            String message = null;

            int i;

            if (activity != null && !activity.isFinishing()) {
                if (response == null) {
                    Toast.makeText(
                        activity,
                        activity.getResources().getString(R.string.msg_network_error),
                        Toast.LENGTH_LONG
                    ).show();
                    return;
                }

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
                                activity.getResources().getString(R.string.track_detail_format),
                                track.getJSONObject("album").getString("#text"),
                                track.getJSONObject("artist").getString("#text")
                            )
                        );

                        if (track.isNull("date")) {
                            map.put(
                                "trackDate",
                                activity.getResources().getString(R.string.track_date_default)
                            );
                        } else {
                            map.put(
                                "trackDate",
                                String.format(
                                    activity.getResources().getString(R.string.track_date_format),
                                    track.getJSONObject("date").getString("#text")
                                )
                            );
                        }

                        activity.trackList.add(map);
                        activity.trackAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }

                if (tracks == null) {
                    try {
                        message = response.getString("message");
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    } catch (JSONException error) {
                        Log.e(TAG, error.toString());
                    }
                }
            }
        }
    }
}
