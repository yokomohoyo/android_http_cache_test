package com.egov.httpcachetest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    public static final String USER_AGENT = "ACME test http client!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void startDhtc(View v) {
        Intent def = new Intent(this, DefaultHttpClientFragment.class);
        startActivity(def);
    }

    public void startHtuc(View v) {
        Intent huc = new Intent(this, HttpUrlConnectFragment.class);
        startActivity(huc);
    }
}
