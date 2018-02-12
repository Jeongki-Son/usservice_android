package com.seoultechus.us.usservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
  public String TAG = "LoginActivity2";

  private final long FINISH_INTERVAL_TIME = 2000;
  private long backPressedTime = 0;

  private Intent intent;
  private EditText userEmailEditText;
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

  @Override
  public void onBackPressed() {
    long tempTime = System.currentTimeMillis();
    long intervalTime = tempTime - backPressedTime;

    if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
      super.onBackPressed();
    } else {
      backPressedTime = tempTime;
      Toast.makeText(getApplicationContext(), "한번 더 뒤로가기를 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
    }
  }


  private void initData() {
    userEmailEditText = findViewById(R.id.userid_login);
    passwordEditText = findViewById(R.id.password_login);
    userLoginButton = findViewById(R.id.login_login);
    progressBar = findViewById(R.id.progress_bar_login);
    progressBar.setVisibility(View.INVISIBLE);

    currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
    editor = currentUser.edit();

    userEmailEditText.setText("90010000@usus.com");
    passwordEditText.setText("900100001200");

    userLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        JSONObject jsonObject = new JSONObject();
        JSONObject user = new JSONObject();
        try {
          jsonObject.put("user", user);
          user.put("email", userEmailEditText.getText().toString());
          user.put("password", passwordEditText.getText().toString());
        } catch (JSONException e) {
          e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(new Params().add("email", userEmailEditText.getText().toString()).add("password", passwordEditText.getText().toString())));
        new UrlConnection()
            .setUrl(AppData.postSession)
            .setRequestMethod(UrlConnection.RequestMethod.POST)
            .setJsonObject(jsonObject)
            .setListener(new UrlConnection.OnConnectionCompleteListener() {
              @Override
              public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                if (response.getBoolean("success")) {
                  Log.d(TAG, "onComplete");
                  Log.d(TAG, String.valueOf(response));
                  token = response.getJSONObject("data").getString("auth_token");
                  UID = Integer.parseInt(response.getJSONObject("data").getString("user_id"));
                  editor.putString("token", token);
                  editor.putInt("UID", UID);
                  editor.commit();
                  new UrlConnection()
                      .setUrl(AppData.getCard)
                      .setRequestMethod(UrlConnection.RequestMethod.POST)
                      .setJsonObject(new JSONObject().put("UID", UID))
                      .setListener(new UrlConnection.OnConnectionCompleteListener() {
                        @Override
                        public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                          status = response.getString("info");
                          if (status.equals("card exist")) {
                            editor.putInt("CID", Integer.parseInt(response.get("CID").toString()));
                            editor.putString("cardCompany", response.getString("cardCompany"));
                            editor.putString("cardNumber", response.getString("cardNumber"));
                            editor.commit();
                            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                              }
                            });
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("login", "new");
                            editor.putString("money", "사용되지 않음");
                            editor.putString("time", "사용되지 않음");
                            editor.putString("content", "사용되지 않음");
                            editor.commit();
                          } else
                            intent = new Intent(LoginActivity.this, CardActivity.class);
                          runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                              progressBar.setVisibility(View.INVISIBLE);
                            }
                          });
                          startActivity(intent);
                          return null;
                        }
                      }).execute();
                  Log.d(TAG, token);
                } else {
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      progressBar.setVisibility(View.INVISIBLE);
                      Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                  });
                }

                return null;
              }

            }).execute();
      }
    });
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
