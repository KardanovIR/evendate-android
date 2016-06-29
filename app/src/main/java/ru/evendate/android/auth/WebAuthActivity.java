package ru.evendate.android.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.ButterKnife;
import ru.evendate.android.R;

/**
 * Created by fj on 14.08.2015.
 */


public class WebAuthActivity extends AppCompatActivity {
    private final String LOG_TAG = WebAuthActivity.class.getSimpleName();

    private WebView mWebView;
    private String mUrl;

    public final static String EMAIL = "email";
    public final static String TOKEN = "token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_web_auth);

        mWebView = (WebView)findViewById(R.id.auth_web_view);
        if (savedInstanceState != null)
            mWebView.restoreState(savedInstanceState);
        else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        mUrl = intent.getExtras().getString(AuthActivity.URL_KEY);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUrl != null)
            mWebView.loadUrl(mUrl);
    }

    protected class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(LOG_TAG, url);
            final String AUTH_PATH = "/mobileAuthDone.php";

            try {
                URL currentURL = new URL(url);
                if (currentURL.getPath().equals(AUTH_PATH)) {

                    String query = currentURL.getQuery();
                    Log.i(LOG_TAG, "start authorization");
                    Log.d(LOG_TAG, "query: " + query);

                    String token = retrieveToken(query);
                    String email = retrieveEmail(query);

                    Intent intent = new Intent();
                    intent.putExtra(EMAIL, email);
                    intent.putExtra(TOKEN, token);
                    setResult(RESULT_OK, intent);
                    finish();

                    Log.d(LOG_TAG, "token: " + token);
                    Log.i(LOG_TAG, "finish authorization");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        private String retrieveToken(String query){
            int start = query.indexOf("=");
            int end = query.indexOf("&");
            return query.substring(start + 1, end);
        }

        private String retrieveEmail(String query){
            return query.substring(query.lastIndexOf("=") + 1);
        }
    }
}
