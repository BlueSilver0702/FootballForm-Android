package uk.co.createanet.footballformapp.lib;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

import org.apache.http.Header;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import uk.co.createanet.footballformapp.R;

/**
 * Created by Matthew Grundy (Createanet) on 19/03/2014
 */
public class RemoteImageDownloader {

    private static final String remoteUrl = "http://footballform.createaclients.co.uk/football_form_database_android.db.zip";
//    private static final String DATABASE_NAME = "football_form_database_android.db";

    // 3 mins to download data
    final static int DEFAULT_TIMEOUT = 180 * 1000;

    public static void bringRemoteFileLocal(final Context c, final ImageDownloadResponseListener listener) {

        listener.start(c);

        Log.d("FF Downloader", "Downloading image: " + remoteUrl);

        final Dialog pDialog = new ProgressDialog(c);

        pDialog.show();
        pDialog.setContentView(R.layout.dialog_progress);
        pDialog.setCancelable(false);

        final ProgressBar progressBar = (ProgressBar) pDialog.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setMax(100);

        ImageView progressBall = (ImageView) pDialog.findViewById(R.id.progressBall);
        final TextView text_percent = (TextView)pDialog.findViewById(R.id.text_percent);
        final TextView textView = (TextView)pDialog.findViewById(R.id.textView);

        Animation rotation = AnimationUtils.loadAnimation(c, R.anim.ball_rotate);
        progressBall.setAnimation(rotation);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(DEFAULT_TIMEOUT);
        client.setUserAgent("Createadroid");

        String[] allowedTypes = new String[]{"application/zip", "application/octet-stream"};
        client.get(remoteUrl, new BinaryHttpResponseHandler(allowedTypes) {

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] imageData) {
                textView.setText("Processing data");

                Thread t = new Thread() {
                    @Override
                    public void run() {


                        try {

                            File outputFile = File.createTempFile("football_form_database_android", ".zip");

                            OutputStream f = new FileOutputStream(outputFile);
                            f.write(imageData);
                            f.close();

                            File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/uk.co.createanet.footballform/");
                            outFile.mkdirs();

                            String newFile = unpackZip(outFile.getAbsolutePath(), outputFile.getAbsolutePath());

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                            listener.success(newFile);
                            listener.end();
                        } catch (IOException e) {
                            e.printStackTrace();
                            listener.failed(c, ImageDownloadResponseListener.FAIL_WRITE);
                        }
                    }
                };

                t.start();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }

                listener.failed(c, ImageDownloadResponseListener.FAIL_CONNECTION);
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progressPercentage = (int) Math.round((double) 100 * ((double) bytesWritten / (double) totalSize));
                progressBar.setProgress(progressPercentage);
                text_percent.setText(progressPercentage + "%");
            }

        });

    }

    public static boolean hasExistingDatabase(Context c) {
        String path = DataManager.getDatabasePath(c);

        if(path != null) {
            File f = new File(path);
            if (f.exists()) {
                return true;
            }
        }

        return false;
    }

    private static String unpackZip(String outPath, String path) {
        InputStream is;
        ZipInputStream zis;

        String destPath = null;
        try {
            String filename;
            is = new FileInputStream(path);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            ze = zis.getNextEntry();

            filename = ze.getName();

            File f = new File(outPath);
            f.mkdirs();

            f = new File(outPath, filename);
            if(f.exists()){
                f.delete();
            }

            destPath = f.getAbsolutePath();

            Log.d("FFDB", "Unpacking to: " + destPath);

            FileOutputStream fout = new FileOutputStream(f);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();

            zis.close();


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return destPath;
    }

    public static abstract class ImageDownloadResponseListener {

        public static final int FAIL_WRITE = 0;
        public static final int FAIL_CONNECTION = 1;

        public void start(Context c) {
        }

        public abstract void success(String localAbsolutePath);

        public void failed(Context c, int reason) {
            end();
            Toast.makeText(c, "Sorry, we were unable to retrieve this update", Toast.LENGTH_SHORT).show();
        }

        public void end() {
        }
    }
}
