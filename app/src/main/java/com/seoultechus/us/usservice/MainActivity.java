package com.seoultechus.us.usservice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
  public String TAG = "MainActivity2";

  private final long FINISH_INTERVAL_TIME = 2000;
  private long backPressedTime = 0;

  private ImageView backButton;
  private TextView appBarTitle;
  private ImageView logoutButton;
  private SharedPreferences currentUser;

  private String cardCompany;
  private String cardNumber;

  private TextView cardCompanyTextView;
  private TextView cardNumberTextView;
  public static TextView smsMoneyTextView;
  public static TextView smsTimeTextView;
  public static TextView smsContentTextView;

  String loginStatus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    cardCompany = getIntent().getStringExtra("card_company");
    cardNumber = getIntent().getStringExtra("card_number");
    loginStatus = getIntent().getStringExtra("login");

    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "SMS 수신권한 있음", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, "SMS 수신권한 없음", Toast.LENGTH_SHORT).show();
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
        Toast.makeText(this, "SMS 권한 설정이 필요함", Toast.LENGTH_SHORT).show();
      } else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
      }
    }

    currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
    final String token = currentUser.getString("token", "null");

    if (cardCompany == null) {
      cardCompany = currentUser.getString("cardCompany", "");
      cardNumber = currentUser.getString("cardNumber", "");
    }

    cardCompanyTextView = findViewById(R.id.tv_card_company);
    cardNumberTextView = findViewById(R.id.tv_card_number);
    smsMoneyTextView = findViewById(R.id.tv_sms_money);
    smsTimeTextView = findViewById(R.id.tv_sms_time);
    smsContentTextView = findViewById(R.id.tv_sms_content);

    cardCompanyTextView.setText(cardCompany);
    cardNumberTextView.setText(cardNumber);

    appBarTitle = findViewById(R.id.app_bar_title);
    appBarTitle.setText("문자 정보");
    backButton = findViewById(R.id.btn_back);
    logoutButton = findViewById(R.id.btn_logout);
    logoutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
        alert_confirm.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                new UrlConnection()
                    .setUrl(AppData.deleteSession)
                    .setRequestMethod(UrlConnection.RequestMethod.DELETE)
                    .setJsonObject(new Params().add("auth_token", token))
                    .setListener(new UrlConnection.OnConnectionCompleteListener() {
                      @Override
                      public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                        Log.d(TAG, String.valueOf(response));
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        currentUser.edit().remove("token").commit();
                        currentUser.edit().remove("UID").commit();
                        finish();
                        return null;
                      }
                    }).execute();
              }
            }).setNegativeButton("취소",
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // 'No'
                return;
              }
            });
        AlertDialog alert = alert_confirm.create();
        alert.show();
      }
    });
    if (loginStatus != null && loginStatus.equals("new")) {
      smsMoneyTextView.setText("사용되지 않음");
      smsTimeTextView.setText("사용되지 않음");
      smsContentTextView.setText("사용되지 않음");
    } else {
      smsMoneyTextView.setText(currentUser.getString("money", "사용되지 않음"));
      smsTimeTextView.setText(currentUser.getString("time", "사용되지 않음"));
      smsContentTextView.setText(currentUser.getString("content", "사용되지 않음"));
    }
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 1) {
      for (int i = 0; i < permissions.length; i++) {
        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this, permissions[i] + " 권한이 거부됨됨.", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }
}
