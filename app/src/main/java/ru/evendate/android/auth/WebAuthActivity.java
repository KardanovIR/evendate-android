package ru.evendate.android.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;

public class WebAuthActivity extends AppCompatActivity {
    private final String LOG_TAG = WebAuthActivity.class.getSimpleName();

    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    @Bind(R.id.auth_web_view) WebView mWebView;
    private String mUrl;

    public final static String EMAIL = "email";
    public final static String TOKEN = "token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_auth);
        ButterKnife.bind(this);

        if (savedInstanceState != null)
            mWebView.restoreState(savedInstanceState);
        else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }

        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        mUrl = intent.getExtras().getString(AuthActivity.URL_KEY);

        mWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        initProgressBar();

        if (mUrl != null)
            mWebView.loadUrl(mUrl);
    }

    private void initProgressBar() {
        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);
        if (!mUrl.equals(AuthActivity.getGoogleUrl(this)))
            mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    protected class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(LOG_TAG, url);
            mProgressBar.setVisibility(View.INVISIBLE);
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

        private String retrieveToken(String query) {
            int start = query.indexOf("=");
            int end = query.indexOf("&");
            return query.substring(start + 1, end);
        }

        private String retrieveEmail(String query) {
            return query.substring(query.lastIndexOf("=") + 1);
        }
    }
}
