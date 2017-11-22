package com.seoultechus.us.usservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Son on 2017-11-15.
 */

public class SMSReceiver extends BroadcastReceiver {
    private String TAG = "SMSBroadcastReceiver";
    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

    private String money;
    private String date;
    private String time;
    private String content;

    @Override
    public void onReceive(Context context, Intent intent) {
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

                    money = getSmsData("([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?원+", message);
                    date = getSmsData("([0-9]{1,2})/([0-9]{1,2})", message);
                    time = getSmsData("([0-9]{1,2}):([0-9]{1,2})", message);

                    infoByCompany(message);
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

    private void infoByCompany(String message) {
        List<String> messageList = divideSmsData(message);
        String companyName = messageList.get(1).substring(0,2);
        String content = messageList.get(5);
        switch (companyName)
        {
            case "신한":
                Log.d(TAG, "신한");
                content = messageList.get(6);
                if (messageList.size() > 6)
                    Log.d(TAG, content+" "+messageList.get(7));
                Log.d(TAG, content);
                break;
            case "NH":
                Log.d(TAG, "NH");
                Log.d(TAG, content);
                break;
            case "우리":
                Log.d(TAG, "우리");
                Log.d(TAG, content);
                break;
            case "KB":
                Log.d(TAG, "KB");
                Log.d(TAG, content.replace(" 사용", ""));
                break;
        }

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
