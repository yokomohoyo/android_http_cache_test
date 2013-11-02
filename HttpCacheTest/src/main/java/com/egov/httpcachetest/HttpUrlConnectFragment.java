package com.egov.httpcachetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    CheckBox cb;
    TextView cs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        checkUrl = (EditText) findViewById(R.id.check_url);
        response = (TextView) findViewById(R.id.response);
        cb = (CheckBox) findViewById(R.id.cache_cb);
        cs = (TextView) findViewById(R.id.cache_status);
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

    private void enableCache() {
        try {
            File httpCacheDir = new File(HttpUrlConnectFragment.this.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i(this.getClass().getCanonicalName(), "HTTP response cache installation failed:" + e);
        }
    }

    private void setCacheStatus() {
        cs.setText("");
        if (cb.isChecked()) {
            HttpResponseCache cache = HttpResponseCache.getInstalled();
            cs.setText(
                    "Requests:" +  cache.getRequestCount() +
                    "\nHits: " + cache.getHitCount() +
                    "\nMisses:" + cache.getNetworkCount()
            );
        }
    }

    private void disableCache() {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
            try {
                cache.delete();
            } catch (Exception e) {
                Log.d(this.getClass().getCanonicalName(), "Unable to remove cache");
            }
        }
    }

    public void handleCache(View v) {
        if (cb.isChecked()) {
            enableCache();
        } else {
            disableCache();
        }
    }

    private static void setResponseText(String s) {
        response.setText(s);
    }

    public class TestUrlTask extends AsyncTask<Object, Void, Activity> {
        private final HttpUrlConnectFragment a;

        public TestUrlTask(HttpUrlConnectFragment a) {
            this.a = a;
        }

        public void sendAlert(String title, String message) {
            new AlertDialog.Builder(a)
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }

        public String processContent(InputStream is) throws Exception {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String c = "";
            String line;
            do {
                line = br.readLine();
                c += line + "\n";
            } while (line != null);

            return c;
        }

        public String processHeaders(Map<String, List<String>> hMap) {
            String h = "";
            for (Map.Entry<String, List<String>> kp : hMap.entrySet()) {
                h += kp.getKey() + " : " + kp.getValue() + "\n";
            }

            return h;
        }

        @Override
        protected Activity doInBackground(Object... params) {
            InputStreamReader isr = null;
            String h = "";
            String c = "";
            int s = 0;

            try {
                URL url = new URL(URL);
                if (url.getProtocol().equals("http")) {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    h = processHeaders(conn.getHeaderFields());
                    c = processContent((InputStream) conn.getContent());
                    s = conn.getResponseCode();
                } else if (url.getProtocol().equals("https")) {
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    h = processHeaders(conn.getHeaderFields());
                    c = processContent((InputStream) conn.getContent());
                    s = conn.getResponseCode();
                }

                final String hs = h;
                final int status = s;
                final String content = c;

                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setCacheStatus();
                        setResponseText("STATUS: " + status
                                + "\n\n HEADERS:\n"
                                + hs
                                + "\n\n CONTENT:\n"
                                + content
                        );

                    }
                });
            } catch (final Exception e) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendAlert("Exception", e.getMessage());
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
