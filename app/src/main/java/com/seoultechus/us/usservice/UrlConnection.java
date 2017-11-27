package com.seoultechus.us.usservice;

import android.content.ContentValues;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Son on 2017-11-22.
 */

public class UrlConnection {
    private String url;
    private RequestMethod requestMethod;
    private Object jsonObject;
    private OnConnectionCompleteListener listener;


    static String TAG = "UrlConnection";

    public UrlConnection setUrl(String url) {
        this.url = url;
        return this;
    }

    public UrlConnection setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public UrlConnection setJsonObject(Object jsonObject_) {
        if (jsonObject_.getClass().equals(Params.class)) {
            jsonObject = ((Params) jsonObject_).getObject();
        } else
            this.jsonObject = jsonObject_;

        Log.d(TAG, "setJsonObject() called with: jsonObject_ = [" + jsonObject_.toString() + "]");
        return this;
    }

    public UrlConnection setListener(OnConnectionCompleteListener listener) {
        this.listener = listener;
        return this;
    }

    public void execute() {
        UrlConnection.httpsRequest(this.url, this.requestMethod, this.jsonObject, this.listener);
        Log.e(TAG, "execute: ");
    }

    public enum RequestMethod {
        GET,
        POST,
        PATCH,
        DELETE,
        PUT,
        HEAD,
        OPTIONS,
        CONNECT
    }

    static private String getString(ContentValues params) {
        String temp = "";
        int i;

        if (params != null) {
            for (i = 0; i < params.valueSet().toArray().length - 1; i++) {
                temp += params.valueSet().toArray()[i].toString() + "&";
            }
            temp += params.valueSet().toArray()[i].toString();
        }

        return temp;
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///sends the json string
    private static HttpsURLConnection httpsURLConnection(String url, RequestMethod requestMethod, Object _params) {
        trustAllHosts();
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setHostnameVerifier(DO_NOT_VERIFY);
            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setConnectTimeout(connTimeout);
//            connection.setReadTimeout(readTimeout);


            if (requestMethod == RequestMethod.POST) {
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                DataOutputStream dataOutputStream;
                String params;
                if (_params.getClass().equals(String.class)) {
                    params = (String) _params;
                    connection.setRequestProperty("Content-Type", "application/json");
                    Log.d(TAG, "string");
                } else if (_params.getClass().equals(ContentValues.class)) {
                    //Content values doesn't use json as a request property
                    connection.setRequestProperty("Content-Type", "application/json");
                    params = getString((ContentValues) _params);
                    Log.d(TAG, "Content Values");
                } else {
                    if (_params.getClass().equals(JSONObject.class)) {
                        JSONObject jsonObject = (JSONObject) _params;
                        connection.setRequestProperty("Content-Type", "application/json");
                        params = jsonObject.toString();
                        Log.d(TAG, "httpsURLConnection() returned: " + "JsonObject");
                    } else {
                        params = LoganSquare.serialize(_params);
                        Log.d(TAG, params);
                        Log.d(TAG, "jsonObject");
                    }
                }
                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(params.getBytes("UTF-8"));
                dataOutputStream.close();

            } else if (requestMethod == RequestMethod.GET) {
                connection.setRequestMethod("GET");
            } else if (requestMethod == RequestMethod.PATCH) {
                connection.setRequestMethod("PATCH");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return connection;
    }

    private static HttpURLConnection httpURLConnection(String url, RequestMethod requestMethod, ContentValues _params) {

        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestMethod("GET");

            if (requestMethod == RequestMethod.POST) {
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                String params = getString(_params);

                DataOutputStream dataOutputStream =
                        new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(params.getBytes("UTF-8"));
                dataOutputStream.close();
            } else if (requestMethod == RequestMethod.GET) {
                connection.setRequestMethod("GET");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return connection;
    }

    public static JSONObject httpsRequest(String url, RequestMethod method, Object contentValues) {

        HttpsURLConnection httpsURLConnection;
        InputStream inputStream;
        JSONObject response = null;

        //try the connection to the server
        try {
            httpsURLConnection = httpsURLConnection(url, method, contentValues);

            inputStream = httpsURLConnection.getInputStream();

//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            inputStream.close();

            String json = sb.toString();
            response = new JSONObject(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * https request post get update delete<br>
     * <p>
     * example post<br>
     * UrlConnection.httpsRequest(AppData.postShopUrl,UrlConnection.RequestMethod.POST, new JSONObject().put("SID", ShopData.shopData.SID), new UrlConnection.OnConnectionCompleteListener() {...});
     *
     * @param url                        the url where the data comes from
     * @param method                     post get update delete
     * @param contentValues              Json object, raw file,
     * @param connectionCompleteListener callback
     * @see RequestMethod
     */
    public static void httpsRequest(final String url, final RequestMethod method, final Object contentValues, final OnConnectionCompleteListener connectionCompleteListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpsURLConnection httpsURLConnection;
                    InputStream inputStream;
                    JSONObject response = null;
                    httpsURLConnection = httpsURLConnection(url, method, contentValues);

                    inputStream = httpsURLConnection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    reader.close();
                    inputStream.close();
                    String json = sb.toString();
                    response = new JSONObject(json);
                    if (connectionCompleteListener != null) {
                        connectionCompleteListener.onComplete(response);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface OnConnectionCompleteListener {
        JSONObject onComplete(JSONObject response) throws IOException, JSONException;
    }

}