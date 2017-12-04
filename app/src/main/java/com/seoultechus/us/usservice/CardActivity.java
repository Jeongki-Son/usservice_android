package com.seoultechus.us.usservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CardActivity extends AppCompatActivity {
    private EditText cardCompanyEditText;
    private EditText cardNumberEditText;
    private Button submitButton;

    private SharedPreferences currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
        final int UID = currentUser.getInt("UID", 0);

        cardCompanyEditText = (EditText) findViewById(R.id.card_company);
        cardNumberEditText = (EditText) findViewById(R.id.card_number);
        submitButton = (Button) findViewById(R.id.submit_button);

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
                                    return null;
                                }
                            }).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(CardActivity.this, MainActivity.class);
                startActivity(intent);
            }
    });
    }
}
