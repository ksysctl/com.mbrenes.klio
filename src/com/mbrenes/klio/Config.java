package com.mbrenes.klio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Config {
    private static final String TAG = "Klio.Config";

    public static JSONObject get(Context context, String name) {
        InputStream stream = null;
        JSONObject object = null;
        String string = null;

        AssetManager assetManager = context.getAssets();

        int size;
        byte[] buffer;

        try {
            stream = assetManager.open(name);
            size = stream.available();
            buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            try {
                string = new String(buffer);
                object = new JSONObject(string);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return object;
    }
}
