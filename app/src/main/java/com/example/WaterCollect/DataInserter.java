package com.example.WaterCollect;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

// post 방식으로 php->MySQL 데이터 전송
class DataInserter extends AsyncTask<String, Void, String> {
    private final static String TAG = "phptest";
    boolean check; // true: send, false: receive

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Log.d(TAG, "POST response  - " + result);

    }

    @Override
    protected String doInBackground(@NonNull String... params) {
        String device = (String)params[1];;
        String intake;
        String date;
        String postParameters;

        String serverURL = (String)params[0];

        if(Objects.equals((String) params[3], "send")) {
            check = true;
            intake = (String)params[2];
            postParameters = "device=" + device + "&intake=" + intake;
        }
        else  { // receive
            check = false;
            date = (String)params[2];
            postParameters = "device=" + device + "&date=" + date;
        }

        try {

            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            if(!check)
                httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseStatusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "POST response code - " + responseStatusCode);

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
            else {
                inputStream = httpURLConnection.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }

            bufferedReader.close();

            return sb.toString().trim();

        } catch (Exception e) {

            Log.d(TAG, "InsertData: Error ", e);
            return "0";
        }

    }
}