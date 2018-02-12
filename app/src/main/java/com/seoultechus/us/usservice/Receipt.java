package com.seoultechus.us.usservice;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Date;

/**
 * Created by Son on 2017-11-22.
 */

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS_AND_ACCESSORS)
public class Receipt {
    // input data
    public int user_id;
    public int card_id;
    public String category;
    public Date pay_date;
    public int amount;
    public String content;

    public static Receipt currentReceipt;
}
