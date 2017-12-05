package com.seoultechus.us.usservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Son on 2017-11-15.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class SMSReceiver extends BroadcastReceiver {
    private String TAG = "SMSBroadcastReceiver";
    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;
    private String phoneNumber;

    private String money;
    private String date;
    private String time;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date dateTime;
    private String content;

    private SharedPreferences currentUser;
    private SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent) {

        currentUser = context.getSharedPreferences("currentUser", MODE_PRIVATE);
        editor = currentUser.edit();

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("onReceive()", "부팅완료");
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("onReceive()", "스크린 ON");
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("onReceive()", "스크린 OFF");
        }


        if (intent.getAction().equals(
                "android.provider.Telephony.SMS_RECEIVED")) {
            Log.d("SMSBroadcastReceiver", "SMS 메시지가 수신되었습니다.");
            // Toast.makeText(context, "문자가 수신되었습니다.", Toast.LENGTH_SHORT).show();

            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                for (Object aObject : pdu_Objects) {
                    currentSMS = getIncomingMessage(aObject, bundle);
                    message = currentSMS.getDisplayMessageBody(); // 문자 값
                    phoneNumber = currentSMS.getDisplayOriginatingAddress();

                    money = getSmsData("([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?원+", message).replace("원", "").replace(",", "");
                    String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                    date = getSmsData("([0-9]{1,2})/([0-9]{1,2})", message).replace("/", "-");
                    time = getSmsData("([0-9]{1,2}):([0-9]{1,2})", message);
                    try {
                        dateTime = dateFormat.parse(year+"-"+date+" "+time+":00");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        String cardNumber = currentUser.getString("cardNumber", "");
                        String modifiedCardNumber = cardNumber.substring(0,1)+"*"+cardNumber.substring(2,3)+"*";
                        if(message.contains(modifiedCardNumber))
                            infoByCompany(message, phoneNumber);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Toast.makeText(context, "senderNum: " + senderNo + " :\n message: " + message, Toast.LENGTH_SHORT).show();
                }
                this.abortBroadcast();
            }
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[])aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[])aObject);
        }
        return currentSMS;
    }

    private void infoByCompany(String message, String PhoneNumber) throws IOException {
        List<String> messageList = divideSmsData(message);
        String companyName = messageList.get(1).substring(0,2);
        String content = messageList.get(5);

        switch (PhoneNumber)
        {
            case "15447200":
                Log.d(TAG, "신한");
                content = messageList.get(6);
                if (messageList.size() > 7)
                    content = content+" "+messageList.get(7);
                Log.d(TAG, content);
                break;
            case "15881600":
                Log.d(TAG, "NH");
                Log.d(TAG, content);
                break;
            case "15889955":
                Log.d(TAG, "우리");
                Log.d(TAG, content);
                break;
            case "15881688":
                Log.d(TAG, "KB");
                content = content.replace(" 사용", "");
                Log.d(TAG, content.replace(" 사용", ""));
                break;
        }

        Receipt.currentReceipt = new Receipt();
        Receipt.currentReceipt.category = "출금";
        Receipt.currentReceipt.user_id = currentUser.getInt("UID", 0);
        Receipt.currentReceipt.pay_date = dateTime;
        Receipt.currentReceipt.amount = Integer.parseInt(money);
        Receipt.currentReceipt.content = content;
        Receipt.currentReceipt.card_id = currentUser.getInt("CID", 0);
        Log.d(TAG, String.valueOf(Receipt.currentReceipt.category));
        new UrlConnection()
                .setUrl(AppData.postReceipt)
                .setRequestMethod(UrlConnection.RequestMethod.POST)
                .setJsonObject(LoganSquare.serialize(Receipt.currentReceipt))
                .setListener(new UrlConnection.OnConnectionCompleteListener() {
                    @Override
                    public JSONObject onComplete(JSONObject response) throws IOException, JSONException {
                        return null;
                    }
                }).execute();

        try
        {
            MainActivity.smsMoneyTextView.setText(getSmsData("([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?원+", message));
            MainActivity.smsTimeTextView.setText(getSmsData("([0-9]{1,2})/([0-9]{1,2})", message) + " " + time);
            MainActivity.smsContentTextView.setText(content);
        }
        catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }

        editor.putString("money", getSmsData("([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?원+", message));
        editor.putString("time", getSmsData("([0-9]{1,2})/([0-9]{1,2})", message) + " " + time);
        editor.putString("content", content);
        editor.commit();

    }

    private String getSmsData(String regex, String message) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        String medium = null;
        if(matcher.find()){
             medium = matcher.group(0);
            Log.d("SMSBroadcastReceiver", medium);
        }
        return medium;
    }

    private List<String> divideSmsData(String message) {
        List<String> smsData;
        String [] stringList = message.split("\n");
        if (stringList.length <= 2) {
            stringList = message.split("\\s+");
        }
        smsData = Arrays.asList(stringList);
        return smsData;
    }
}
