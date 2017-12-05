package com.seoultechus.us.usservice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public String TAG = "MainActivity2";
    private ImageView backButton;
    private TextView appBarTitle;
    private ImageView logoutButton;
    private SharedPreferences currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "SMS 수신권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS 수신권한 없음", Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                Toast.makeText(this, "SMS 권한 설정이 필요함", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS},1);
            }
        }

        currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
        final String token = currentUser.getString("token", "null");

        appBarTitle = (TextView) findViewById(R.id.app_bar_title);
        appBarTitle.setText("문자 정보");
        backButton = (ImageView) findViewById(R.id.btn_back);
        logoutButton = (ImageView) findViewById(R.id.btn_logout);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 거부됨됨.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
