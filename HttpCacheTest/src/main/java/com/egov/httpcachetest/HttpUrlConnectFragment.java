package com.egov.httpcachetest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class HttpUrlConnectFragment extends Activity {
    private static String URL;
    private EditText checkUrl;
    private static TextView response;
    private Button fetch;
    AsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        task = new HttpUrlConnTestTask(this);

        checkUrl = (EditText) findViewById(R.id.check_url);
        response = (TextView) findViewById(R.id.response);
        fetch = (Button) findViewById(R.id.fetch);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.fetch:
                        Log.d(this.getClass().getCanonicalName(), "Getting it");
                        URL = checkUrl.getText().toString();
                        task.execute(URL);
                        break;
                }
            }
        });
        setContentView(R.layout.main);
    }

    public static void setBodyText(String s) {
        response.setText(s);
    }

    public class HttpUrlConnTestTask extends AsyncTask<String, Void, String> {

        Activity a;

        public HttpUrlConnTestTask(Activity a) {
            this.a = a;
        }
        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();

            HttpGet hg = new HttpGet(URL);
            hg.setHeader("User-Agent", MainActivity.USER_AGENT);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = new ByteArrayInputStream(new byte[512]);

            try {
                HttpResponse res = client.execute(hg);
                StatusLine status = res.getStatusLine();

                HttpEntity entity = res.getEntity();
                is = entity.getContent();

                int readBytes = 0;
                byte[] buffer = new byte[512];

                response.append("Content:\n\n");
                while ((readBytes = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, readBytes);
                }

                System.out.println(response);
                response.setText(new String(baos.toByteArray()));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    baos.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "ok";
        }
    }
}
