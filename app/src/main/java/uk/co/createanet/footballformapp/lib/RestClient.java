package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class RestClient {

    public AsyncHttpClient client;

    public static final String BASE_URL = "http://footballform.createaclients.co.uk/api/v2/";

    public RestClient(){
        client = new AsyncHttpClient();
        client.setUserAgent("Createadroid");
    }

    public void addAuthHeaders(int userId, String password){
        client.setBasicAuth(String.valueOf(userId), password);
    }

    public static void get(Context c, String url, RequestParams params, CreateaResponseHandler responseHandler) {
        get(c, url, params, responseHandler, true);
    }

    public static void get(Context c, String url, RequestParams params, CreateaResponseHandler responseHandler, boolean showLoading) {
        responseHandler.start(showLoading);

        RestClient restClient = new RestClient();
        responseHandler.preStart(restClient);
        restClient.client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context c, String url, RequestParams params, CreateaResponseHandler responseHandler) {
        post(c, url, params, responseHandler, true);
    }

    public static void post(Context c, String url, RequestParams params, CreateaResponseHandler responseHandler, boolean showLoading) {
        if(responseHandler != null) {
            responseHandler.start(showLoading);
        }

        RestClient restClient = new RestClient();
        if(responseHandler != null) {
            responseHandler.preStart(restClient);
        }
        restClient.client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void put(Context c, String url, RequestParams params, CreateaResponseHandler responseHandler) {
        responseHandler.start();
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    public void delete(Context c, String url, CreateaResponseHandler responseHandler) {
        responseHandler.start();
        client.delete(getAbsoluteUrl(url), responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.d("RestClient", "Requesting URL: " + BASE_URL + relativeUrl);

        return BASE_URL + relativeUrl;
    }

}