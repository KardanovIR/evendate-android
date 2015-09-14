package ru.getlect.evendate.evendate.Authorization;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;

import ru.getlect.evendate.evendate.MainActivity;
import ru.getlect.evendate.evendate.R;

/**
 * Created by fj on 14.08.2015.
 */


public class WebAuthActivity extends Activity {

    public WebView myWebView;
    Intent intent ;
    public String token;
    public String mail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_auth);

        myWebView = (WebView)findViewById(R.id.myWebView);
        myWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = myWebView.getSettings();

        String accType = "accountType";
        int accountType = getIntent().getExtras().getInt(accType);

        webSettings.setJavaScriptEnabled(true);
        String vkUrl = "https://oauth.vk.com/authorize?client_id=5029623&scope=friends,email,offline,nohttps&redirect_uri=http://evendate.ru/vkOauthDone.php?mobile=true&response_type=code";
        String fbUrl = "https://www.facebook.com/dialog/oauth?client_id=1692270867652630&response_type=code&scope=public_profile,email,user_friends&display=popup&redirect_uri=http://evendate.ru/fbOauthDone.php?mobile=true";
        String gPlusUrl = "https://accounts.google.com/o/oauth2/auth?scope=email profile https://www.googleapis.com/auth/plus.login &redirect_uri=http://evendate.ru/googleOauthDone.php?mobile=true&response_type=token&client_id=403640417782-lfkpm73j5gqqnq4d3d97vkgfjcoebucv.apps.googleusercontent.com";

        switch(accountType){
            case 0:
                myWebView.loadUrl(vkUrl);
                break;
            case 1:
                myWebView.loadUrl(fbUrl);
                break;
            case 2:
                myWebView.loadUrl(gPlusUrl);
                break;
        }





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
            Log.e(LOG_TAG, url);


            super.onPageStarted(view, url, favicon);
            URL currentURL;
            String ouath = "/mobileAuthDone.php";
            intent = new Intent(getApplicationContext(),MainActivity.class);


            try {
                currentURL = new URL(url);
                if(currentURL.getPath().equals(ouath)){
                    String query = currentURL.getQuery();
                    Log.e("ТОКЕН", query);
                    startActivity(intent);
                    int start = query.indexOf("=");
                    int end = query.indexOf("&email");
                    token = query.substring(start + 1,end);
                    Log.e("ТОкен", token);

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }




        }
    }


}
