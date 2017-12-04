package com.seoultechus.us.usservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    public String TAG = "LoginActivity2";
    private Intent intent;
    private EditText useremailEditText;
    private EditText passwordEditText;

    private Button userLoginButton;

    private ProgressBar progressBar;

    private SharedPreferences currentUser;
    private SharedPreferences.Editor editor;
    private String token;
    private int UID;
    private String status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initData();

    }

    private void initData() {
        useremailEditText = (EditText) findViewById(R.id.userid_login);
        passwordEditText = (EditText) findViewById(R.id.password_login);
        userLoginButton = (Button) findViewById(R.id.login_login);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_login);
        progressBar.setVisibility(View.INVISIBLE);

        currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
        editor = currentUser.edit();

        useremailEditText.setText("ghkdgh2365@naver.com");
        passwordEditText.setText("12345678");

        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                JSONObject jsonObject = new JSONObject();
                JSONObject user = new JSONObject();
                try {
                    jsonObject.put("user", user);
                    user.put("email", useremailEditText.getText().toString());
                    user.put("password", passwordEditText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, String.valueOf(new Params().add("email",useremailEditText.getText().toString()).add("password",passwordEditText.getText().toString())));
                new UrlConnection()
                        .setUrl(AppData.postSession)
                        .setRequestMethod(UrlConnection.RequestMethod.POST)
                        .setJsonObject(jsonObject)
                        .setListener(new UrlConnection.OnConnectionCompleteListener() {
                            @Override
                            public JSONObject onComplete(JSONObject response) throws IOException, JSONException {

                                Log.d(TAG, "onComplete");
                                Log.d(TAG, String.valueOf(response));
                                token = response.getJSONObject("data").getString("auth_token");
                                UID = Integer.parseInt(response.getJSONObject("data").getString("user_id"));
                                editor.putString("token", token);
                                editor.putInt("UID", UID);
                                editor.commit();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                                new UrlConnection()
                                        .setUrl(AppData.getCard)
                                        .setRequestMethod(UrlConnection.RequestMethod.POST)
                                        .setJsonObject(new JSONObject().put("UID", UID))
                                        .setListener(new UrlConnection.OnConnectionCompleteListener() {
                                            @Override
                                            public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                                                status = response.getString("info");
                                                if (status.equals("card exist"))
                                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                                else
                                                    intent = new Intent(LoginActivity.this, CardActivity.class);
                                                startActivity(intent);
                                                return null;
                                            }
                                        }).execute();
                                Log.d(TAG, token);
                                return null;
                            }

                        }).execute();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (token == null) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 2000);
            }
        });
    }
}
