package ru.getlect.evendate.evendate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by fj on 14.08.2015.
 */


public class WebAuthActivity extends Activity {

    public WebView myWebView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_auth);

        myWebView = (WebView)findViewById(R.id.myWebView);
        myWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("https://oauth.vk.com/authorize?client_id=5029623&scope=friends,email,offline,nohttps&redirect_uri=http://evendate.ru/vkOauthDone.php?mobile=true&response_type=code");



    }

    protected class MyWebViewClient extends WebViewClient {
//        @Override
//        public void onPageFinished(WebView view, String url) {
//            // TODO Auto-generated method stub
//            super.onPageFinished(view, url);
////        super.onPageFinished(view, url);
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            // TODO Auto-generated method stub
//            return super.shouldOverrideUrlLoading(view, url);
//        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            String LOG_TAG = "VKVKVK";
            Log.e(LOG_TAG,url);
            super.onPageStarted(view, url, favicon);
        }
    }


}
