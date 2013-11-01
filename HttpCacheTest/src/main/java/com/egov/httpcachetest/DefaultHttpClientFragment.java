package com.egov.httpcachetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DefaultHttpClientFragment extends Activity {
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
                        task = new TestUrlTask(DefaultHttpClientFragment.this);
                        URL = checkUrl.getText().toString();
                        pd = new ProgressDialog(DefaultHttpClientFragment.this);
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
        private final DefaultHttpClientFragment a;

        public TestUrlTask(DefaultHttpClientFragment a) {
            this.a = a;
        }

        @Override
        protected Activity doInBackground(Object... params) {
            HttpClient client = new DefaultHttpClient();

            HttpGet hg = new HttpGet(URL);
            hg.setHeader("User-Agent", MainActivity.USER_AGENT);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = new ByteArrayInputStream(new byte[512]);

            try {
                HttpResponse res = client.execute(hg);
                final StatusLine status = res.getStatusLine();

                HttpEntity entity = res.getEntity();
                is = entity.getContent();

                int readBytes = 0;
                byte[] buffer = new byte[512];
                while ((readBytes = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, readBytes);
                }

                Header[] h = res.getAllHeaders();
                String ht = new String();
                for (Header k : h) {
                    ht += k.getName() + " : " + k.getValue() + "\n";
                }

                final String headers = ht;
                final String content = new String(baos.toByteArray());
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setResponseText("STATUS: " + status.getProtocolVersion()
                                + "/" + status.getStatusCode()
                                + "\n\n HEADERS:\n"
                                + headers
                                + "\n\n CONTENT:\n"
                                + content
                        );

                    }
                });

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
                    baos.close();
                    is.close();
                    pd.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

