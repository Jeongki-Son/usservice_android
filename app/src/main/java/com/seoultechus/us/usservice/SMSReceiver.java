package com.seoultechus.us.usservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Son on 2017-11-15.
 */

public class SMSReceiver extends BroadcastReceiver {
    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

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
            Toast.makeText(context, "문자가 수신되었습니다.", Toast.LENGTH_SHORT).show();

            StringBuilder sms = new StringBuilder();
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                for (Object aObject : pdu_Objects) {
                    currentSMS = getIncomingMessage(aObject, bundle);
                    String senderNo = currentSMS.getDisplayMessageBody();
                    message = currentSMS.getDisplayMessageBody(); // 문자 값
                    boolean b = message.contains("신한");
                    Log.d("SMSBroadcastReceiver", message);
                    Log.d("SMSBroadcastReceiver", String.valueOf(b));
                    Toast.makeText(context, "senderNum: " + senderNo + " :\n message: " + message, Toast.LENGTH_SHORT).show();
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
}
