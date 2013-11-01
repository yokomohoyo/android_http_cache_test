package com.egov.httpcachetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpUrlConnectFragment extends Activity {
    private static String URL;
    private EditText checkUrl;
    private static TextView response;
    private Button fetch;
    AsyncTask task;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        checkUrl = (EditText) findViewById(R.id.check_url);
        response = (TextView) findViewById(R.id.response);
        fetch = (Button) findViewById(R.id.fetch);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.fetch:
                        response.setText("");
                        task = new TestUrlTask(HttpUrlConnectFragment.this);
                        URL = checkUrl.getText().toString();
                        pd = new ProgressDialog(HttpUrlConnectFragment.this);
                        pd.setIndeterminate(true);
                        pd.setMessage("Fetching...");
                        pd.setCancelable(false);
                        task.execute(URL);
                        pd.show();
                        break;
                }
            }
        });
    }

    public static void setResponseText(String s) {
        response.setText(s);
    }

    public class TestUrlTask extends AsyncTask<Object, Void, Activity> {
        private final HttpUrlConnectFragment a;

        public TestUrlTask(HttpUrlConnectFragment a) {
            this.a = a;
        }

        @Override
        protected Activity doInBackground(Object... params) {
            Map<String, List<String>> hMap;
            String h = new String();

            try {
                URL url = new URL(URL);
                Log.d(this.getClass().getCanonicalName(), "### PROTO = " + url.getProtocol());
                if (url.getProtocol().equals("http")) {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader isr = new InputStreamReader((InputStream) conn.getContent());
                    BufferedReader br = new BufferedReader(isr);
                    String c = new String();
                    String line;
                    do {
                        line = br.readLine();
                        c += line + "\n";
                    } while (line != null);
                    final String content = c;
                    hMap = conn.getHeaderFields();
                    final int status = conn.getResponseCode();
                    for (Map.Entry<String, List<String>> kp : hMap.entrySet()) {
                        h += kp.getKey() + " : " + kp.getValue() + "\n";
                    }
                    final String hs = h;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResponseText("STATUS: " + status
                                    + "\n\n HEADERS:\n"
                                    + hs
                                    + "\n\n CONTENT:\n"
                                    + content
                            );

                        }
                    });


                } else {
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    InputStreamReader isr = new InputStreamReader((InputStream) conn.getContent());
                    BufferedReader br = new BufferedReader(isr);
                    String c = new String();
                    String line;
                    do {
                        line = br.readLine();
                        c += line + "\n";
                    } while (line != null);
                    final String content = c;
                    hMap = conn.getHeaderFields();
                    final int status = conn.getResponseCode();
                    for (Map.Entry<String, List<String>> kp : hMap.entrySet()) {
                        h += kp.getKey() + " : " + kp.getValue() + "\n";
                    }
                    final String hs = h;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResponseText("STATUS: " + status
                                    + "\n\n HEADERS:\n"
                                    + hs
                                    + "\n\n CONTENT:\n"
                                    + content
                            );

                        }
                    });

                }

            } catch (final Exception e) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(a)
                                .setTitle("Error")
                                .setMessage(e.getMessage())
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();
                    }
                });
            } finally {
                try {
                    pd.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
