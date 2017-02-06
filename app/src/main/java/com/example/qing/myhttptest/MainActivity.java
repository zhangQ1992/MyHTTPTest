package com.example.qing.myhttptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    static final int SUCCESS = 1;
    static final int ERROR = 2;

    Button myButton;
    TextView myText;
    EditText et_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myButton = (Button) findViewById(R.id.btn);
        myText = (TextView) findViewById(R.id.text_web);
        et_path = (EditText) findViewById(R.id.edt);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getByhttpclient();
            }
        });
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what) {
                case SUCCESS:
                    String text = (String) msg.obj;
                    myText.setText(text);
                    break;
                case ERROR:
                    Toast.makeText(MainActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };





    private void getByhttpclient(){
        final String path = et_path.getText().toString().trim();
        new Thread(){
            public void run(){
                try {
                    URL myurl = new URL(path);
                    HttpURLConnection myHttpURLConnection = (HttpURLConnection)myurl.openConnection();
                    myHttpURLConnection.setRequestMethod("GET");

                    int code = myHttpURLConnection.getResponseCode();
                    if(code ==200){
                        InputStream is = myHttpURLConnection.getInputStream();
                        String result = StreamTools.readStream(is);

                        Message msg = Message.obtain();//减少消息创建的数量
                        msg.obj = result;
                        msg.what = SUCCESS;
                        mHandler.sendMessage(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}


