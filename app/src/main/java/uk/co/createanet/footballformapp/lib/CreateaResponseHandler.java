package uk.co.createanet.footballformapp.lib;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.createanet.footballformapp.R;

public class CreateaResponseHandler extends JsonHttpResponseHandler {

    public Context context;
    public String defaultError = "This action cannot be completed, please try again"; // context.getString(R.string.error_unable_process);

    private Dialog pDialog;

    public CreateaResponseHandler(Context contextIn){
        context = contextIn;
    }

    public void preStart(RestClient client){
        // allows adding of additional headers
    }

    public void start() {
        start(true);
    }

    public void start(boolean shouldShowDialog){
        if(shouldShowDialog) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(context);
            } else {
                if (pDialog.isShowing()) {
                    pDialog.hide();
                }
            }

//        pDialog.setTitle(loadingMessage);

            if (context != null) {
                pDialog.show();
                pDialog.setContentView(R.layout.dialog_loading);
            }
        }
    }

    public void end(){
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (IllegalArgumentException e){
            // ignore - this is when it's closing premature
            Log.d("FF", "Invalid window - closing dialog");
        }

//            resetTimeout();
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        Log.d("FF", "Success response object");

        onSuccess(response);
        end();
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        Log.d("FF", "Success response array");

        onSuccess(response);
        end();
    }

    public void onSuccess(JSONArray response){
        // just for old style methods - deprecated
    }

    public void onSuccess(JSONObject response){
        // just for old style methods - deprecated
    }

    @Override
    public void onFailure(int statusCode, org.apache.http.Header[] headers, java.lang.Throwable ex, org.json.JSONObject jsonObject)  {
        super.onFailure(statusCode, headers, ex, jsonObject) ;

        end();

        String message = defaultError;

        if(jsonObject != null){
            try {
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(int statusCode, org.apache.http.Header[] headers, java.lang.String responseBody){

        try {
            onSuccess(statusCode, headers, (JSONObject) parseResponse(responseBody.getBytes()));
        } catch (JSONException e) {
            onFailure(statusCode, headers, responseBody, null);
        } catch(ClassCastException e){
            onFailure(statusCode, headers, responseBody, null);
        }

    }

    @Override
    public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseBody, Throwable e) {

        end();

        super.onFailure(statusCode, headers, responseBody, e);

        /*
        if(statusCode == 404){
            Toast.makeText(context, defaultError, Toast.LENGTH_SHORT).show();
        } else {

        }
        */
    }

    @Override
    public void onFinish() {
        end();
    }

}