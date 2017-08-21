package ru.evendate.android.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;

public class WebAuthActivity extends AppCompatActivity {
    private final String LOG_TAG = WebAuthActivity.class.getSimpleName();

    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.auth_web_view) WebView mWebView;
    private String mUrl;

    public final static String EMAIL = "email";
    public final static String TOKEN = "token";
    public final static String BACK_PRESSED = "back_pressed";

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

        mWebView.setWebViewClient(new AuthWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //cause google restriction
        if (mUrl.equals(AuthActivity.getGoogleUrl(this))) {
            mWebView.getSettings()
                    .setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(BACK_PRESSED, true);
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    private class AuthWebViewClient extends WebViewClient {
        boolean timeout = true;

        //todo add timeout only for evendate
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(LOG_TAG, url);
            Runnable run = () -> {
                if (timeout) {
                    Log.e(LOG_TAG, "auth connection timeout");
                    setResult(RESULT_CANCELED);
                    finish();
                }
            };
            new Handler(Looper.getMainLooper()).postDelayed(run, 60000);


            mProgressBar.setVisibility(View.INVISIBLE);
            final String AUTH_PATH = "/mobileAuthDone.php";

            try {
                URL currentURL = new URL(url);
                if (currentURL.getPath().equals(AUTH_PATH)) {
                    timeout = false;
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
            String pattern = "(?<=token=)(.*?)(?=&|$)";
            Pattern tokenPattern = Pattern.compile(pattern);
            Matcher match = tokenPattern.matcher(query);
            return match.find() ? match.group(0) : null;
        }

        private String retrieveEmail(String query) {
            String pattern = "(?<=email=)(.*?)(?=&|$)";
            Pattern emailPattern = Pattern.compile(pattern);
            Matcher match = emailPattern.matcher(query);
            return match.find() ? match.group(0) : null;
        }
    }
}
