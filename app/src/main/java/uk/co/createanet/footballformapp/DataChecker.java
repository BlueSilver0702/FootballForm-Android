package uk.co.createanet.footballformapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.lib.DataManager;
import uk.co.createanet.footballformapp.lib.RemoteImageDownloader;

public class DataChecker extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_data_checker);

        getActionBar().hide();

        showDownloadPrompt();
    }

    private void showDownloadPrompt(){

        if (!RemoteImageDownloader.hasExistingDatabase(DataChecker.this)) {
            getNewData();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(DataChecker.this);
            builder.setMessage("Would you like to refresh the Football Form data now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            FFDatabase db = new FFDatabase(DataChecker.this);
                            DataManager.storeFavouriteTeams(DataChecker.this, db);

                            getNewData();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loadDatabase();
                            dialog.dismiss();
                        }
                    });


            Dialog d = builder.create();
            d.setCancelable(false);
            d.show();
        }

    }

    private void getNewData() {

        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DataChecker.this);
            builder.setMessage("To download new data you need an active data connection")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getNewData();
                            dialog.dismiss();
                        }
                    });

            if (RemoteImageDownloader.hasExistingDatabase(DataChecker.this)) {
                builder.setNegativeButton("Cancel Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadDatabase();
                        dialog.dismiss();
                    }
                });
            }

            Dialog d = builder.create();
            d.setCancelable(false);
            d.show();

            return;
        }


        RemoteImageDownloader.bringRemoteFileLocal(DataChecker.this, new RemoteImageDownloader.ImageDownloadResponseListener() {
            @Override
            public void success(String localAbsolutePath) {
                Log.d("FF", "Got new data " + localAbsolutePath);
                DataManager.setDatabasePath(DataChecker.this, localAbsolutePath);

                FFDatabase db = new FFDatabase(DataChecker.this);
                DataManager.restoreFavouriteTeams(DataChecker.this, db);

                loadDatabase();
            }

            @Override
            public void failed(Context c, int reason) {
                super.failed(c, reason);

                showDownloadPrompt();
            }

        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void loadDatabase() {
        Intent i = new Intent(DataChecker.this, MainActivity.class);
        startActivity(i);
    }

}
