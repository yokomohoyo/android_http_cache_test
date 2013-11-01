package com.egov.httpcachetest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;
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
        setContentView(R.layout.main);
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
                if (url.getProtocol() == "http") {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    final String content = conn.getContent().toString();
                    hMap = conn.getHeaderFields();
                    final int status = conn.getResponseCode();
                    while (hMap.entrySet().iterator().hasNext()) {
                        Map.Entry kp = (Map.Entry) hMap.entrySet().iterator().next();
                        h += kp.getKey() + " : " + kp.getValue();
                        hMap.entrySet().iterator().remove();
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
                }

            } catch (Exception e) {
                e.printStackTrace();
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
