package com.seoultechus.us.usservice;

/**
 * Created by Son on 2017-11-22.
 */

public class AppData {
    public static final String host = "https://usservice.herokuapp.com";

    // user routes
    public static final String getUser = host + "/api/v1/sessions/verify";
    public static final String postSession = host + "/api/v1/sessions";
    public static final String deleteSession = host + "/api/v1/sessions";

    // card routes
    public static final String getCard = host + "/cards/data";
    public static final String postCard = host + "/cards";

    // receipt routes
    public static final String postReceipt = host + "/receipts";

}
