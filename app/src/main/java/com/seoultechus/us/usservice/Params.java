package com.seoultechus.us.usservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Son on 2017-11-22.
 */

public class Params {
    String[] keys;
    String[] values;
    int max;
    int count;

    public Params() {
//            keys = new String[1];
//            values = new String[1];
        count = 0;
//            max = size;
    }

    public Params add(String key, String value) {
        count++;
        String[] temp;
        String[] temp2;
        temp = keys;
        temp2 = values;
        keys = new String[count];
        values = new String[count];
        for (int i = 0; i < count-1; i++) {
            keys[i] = temp[i];
            values[i] = temp2[i];
        }
        keys[count - 1] = key;
        values[count - 1] = value;
        return this;
    }

    public JSONObject getObject() {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < count; i++) {
            try {
                jsonObject.put(keys[i], values[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return "Params{" +
                "keys=" + Arrays.toString(keys) +
                ", values=" + Arrays.toString(values) +
                ", max=" + max +
                ", count=" + count +
                '}';
    }
}