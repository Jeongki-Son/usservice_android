package com.seoultechus.us.usservice;

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
    private EditText useremailEditText;
    private EditText passwordEditText;

    private Button userLoginButton;


    private ProgressBar progressBar;

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
                                String token;
                                Log.d(TAG, "onComplete");
                                Log.d(TAG, String.valueOf(response));
                                token = response.getJSONObject("data").getString("auth_token");
                                Log.d(TAG, token);
                                return null;
                            }
                        }).execute();
            }
        });

    }
}
