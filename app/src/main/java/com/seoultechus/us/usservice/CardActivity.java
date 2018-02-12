package com.seoultechus.us.usservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CardActivity extends AppCompatActivity {

  private final long FINISH_INTERVAL_TIME = 2000;
  private long backPressedTime = 0;

  private EditText cardCompanyEditText;
  private EditText cardNumberEditText;
  private Button submitButton;

  private SharedPreferences currentUser;
  private SharedPreferences.Editor editor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_card);

    currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
    editor = currentUser.edit();
    final int UID = currentUser.getInt("UID", 0);


    cardCompanyEditText = findViewById(R.id.card_company);
    cardNumberEditText = findViewById(R.id.card_number);
    submitButton = findViewById(R.id.submit_button);

    submitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          new UrlConnection().setUrl(AppData.postCard)
              .setRequestMethod(UrlConnection.RequestMethod.POST)
              .setJsonObject(new JSONObject().put("user_id", UID).put("card_company", String.valueOf(cardCompanyEditText.getText())).put("card_number", String.valueOf(cardNumberEditText.getText())))
              .setListener(new UrlConnection.OnConnectionCompleteListener() {
                @Override
                public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                  String cardCompany = String.valueOf(cardCompanyEditText.getText());
                  String cardNumber = String.valueOf(cardNumberEditText.getText());
                  editor.putInt("CID", Integer.parseInt(response.get("CID").toString()));
                  editor.putString("cardCompany", cardCompany);
                  editor.putString("cardNumber", cardNumber);
                  editor.commit();
                  Log.d("CARD2", currentUser.getString("cardCompany", "null"));
                  Intent intent = new Intent(CardActivity.this, MainActivity.class).putExtra("card_company", String.valueOf(cardCompanyEditText.getText())).putExtra("card_number", String.valueOf(cardNumberEditText.getText()));
                  intent.putExtra("login", "new");
                  editor.putString("money", "사용되지 않음");
                  editor.putString("time", "사용되지 않음");
                  editor.putString("content", "사용되지 않음");
                  editor.commit();
                  startActivity(intent);
                  finish();
                  return null;
                }
              }).execute();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
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
}
