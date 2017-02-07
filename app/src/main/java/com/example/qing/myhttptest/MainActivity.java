package com.example.qing.myhttptest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    static final int SUCCESS = 1;
    static final int ERROR = 2;
    static final String LOG_HTTP = "http_log";

    private EditText myEdittext;
    private Button btn_http, btn_web, btn_pic;
    private TextView myText;
    private WebView myWeb;
    private ImageView myImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myEdittext = (EditText) findViewById(R.id.et);
        btn_http = (Button) findViewById(R.id.btn_html);
        btn_web = (Button) findViewById(R.id.btn_webview);
        btn_pic = (Button) findViewById(R.id.btn_pic);

        myText = (TextView) findViewById(R.id.http_text);
        myText.setText("这里显示http获取的数据");
        myWeb = (WebView) findViewById(R.id.wb);
        myImg = (ImageView) findViewById(R.id.http_img);


        myWeb.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });
        btn_http.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_HTTP, "onclick HTTP");
                get_httpurlconnection();
            }
        });

        btn_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_HTTP, "onclick WEB");
                myWeb.loadUrl("http://www.baidu.com");
            }
        });

        btn_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_HTTP, "onclick IMG");
            }
        });
    }


    //Handler 用于多个线程协作，在此情况下，activity用于ui的更新,Handler从线程那里拿到数据后显示。
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Log.d(LOG_HTTP, "msg is SUCCESS");
                    String text = (String) msg.obj;
                    Log.d(LOG_HTTP, "msg.obj is " + text);
                    myText.setText(text);
                    Toast.makeText(MainActivity.this, "请求成功！", Toast.LENGTH_LONG).show();
                    break;
                case ERROR:

                    break;
            }
        }
    };


    private void get_httpurlconnection() {
        new Thread() {
            HttpURLConnection myconn;//实例化一个HttpURLConnection

            public void run() {
                try {
                    URL myurl = new URL("http://www.baidu.com");
//                  URL myurl = new URL("https://m.baidu.com/?from=844b&vit=fps");
                    myconn = (HttpURLConnection) myurl.openConnection();
                    myconn.setRequestMethod("GET");//get方法
                    myconn.setConnectTimeout(6 * 1000);//6s超时
                    Log.d(LOG_HTTP, "myHttpURLConnection is created");
                    Log.d(LOG_HTTP, "code is:" + myconn.getResponseCode());//获取到响应码
                    if (myconn.getResponseCode() == 302) {//响应码为302的时候代表需要重定向，此时利用getHeaderField方法获取新地址
                        String location = myconn.getHeaderField("Location");//获取重定向的地址

                        Log.d(LOG_HTTP, "Location is :" + location);
                        URL url_new = new URL(location);
                        myconn = (HttpURLConnection) url_new.openConnection();//重定向地址到HttpURLConnection
                        myconn.setRequestMethod("GET");
                        myconn.setConnectTimeout(6 * 1000);
                    }
                    Log.d(LOG_HTTP, "code is:" + myconn.getResponseCode());
                    if (myconn.getResponseCode() == 200) {
                        InputStream is = myconn.getInputStream();//获取http数据的InputStream
                        String dat = StreamTools.readStream(is);//InputStream转换成字符串
                        Message msg = Message.obtain();//实例化一个Message
                        msg.obj = dat;//msg携带的数据
                        msg.what = SUCCESS;//msg的状态
                        mHandler.sendMessage(msg);
                        /*
                        * 使用一个Handler，Handler的作用是更新显示UI，线程不适合更新UI，UI更新只适合在Activity中
                        * Handler的作用是协助线程和Activity，与其他线程协同工作，接收其他线程的消息并通过接收到的消息更新主UI线程的内容。
                        * Android设计了Handler机制，由Handler来负责与子线程进行通讯，从而让子线程与主线程之间建立起协作的桥梁，
                        * 使Android的UI更新的问题得到完美的解决。
                        * */
                    } else {
                        Toast.makeText(MainActivity.this, "获取失败，返回的响应代码为：" + myconn.getResponseCode(), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    myconn.disconnect();//通信完毕后需要将连接断开
                }
            }
        }.start();
    }
}


