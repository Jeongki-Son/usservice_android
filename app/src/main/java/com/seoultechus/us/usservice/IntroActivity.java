package com.seoultechus.us.usservice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class IntroActivity extends AppCompatActivity {
    private String TAG = "Intro2";
    private Intent intent;
    private SharedPreferences currentUser;
    private SharedPreferences.Editor editor;
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
        final String token = currentUser.getString("token", "null");

        String idByANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, idByANDROID_ID);
        
        new UrlConnection()
                .setUrl(AppData.getUser)
                .setRequestMethod(UrlConnection.RequestMethod.POST)
                .setJsonObject(new Params().add("token", token))
                .setListener(new UrlConnection.OnConnectionCompleteListener() {
                    @Override
                    public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                        Log.d(TAG, String.valueOf(response));
                        info = response.getString("info");
                        return null;
                    }
                }).execute();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, token);
                if (info != null && info.equals("Card Registered"))
                    intent = new Intent(IntroActivity.this, CardActivity.class);
                else if (info != null && info.equals("Logged in"))
                    intent = new Intent(IntroActivity.this, MainActivity.class);
                else
                    intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            }
        }, 2000);
    }
}
