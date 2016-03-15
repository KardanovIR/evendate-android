package ru.evendate.android.authorization;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.MalformedURLException;
import java.net.URL;

import ru.evendate.android.R;

/**
 * Created by fj on 14.08.2015.
 */


public class WebAuthFragment extends Fragment {
    private final String LOG_TAG = WebAuthFragment.class.getSimpleName();

    private WebView mWebView;
    private String mUrl;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_web_auth, container, false);
        super.onCreate(savedInstanceState);

        mWebView = (WebView)rootView.findViewById(R.id.auth_web_view);
        if (savedInstanceState != null)
            mWebView.restoreState(savedInstanceState);
        else{
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Bundle bundle = getArguments();
        if(bundle != null)
            mUrl = bundle.getString(AuthActivity.URL_KEY);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mUrl != null)
            mWebView.loadUrl(mUrl);
    }

    protected class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(LOG_TAG, url);
            String oAuth = "/mobileAuthDone.php";

            try {
                URL currentURL = new URL(url);
                if(currentURL.getPath().equals(oAuth)){
                    Log.i(LOG_TAG, "start authorization");
                    String query = currentURL.getQuery();
                    Log.i(LOG_TAG, query);
                    int start = query.indexOf("=");
                    int end = query.indexOf("&");
                    String token = query.substring(start + 1, end);
                    String email = query.substring(query.lastIndexOf("=") + 1);
                    Log.i(LOG_TAG, token);
                    String accountType = getResources().getString(R.string.account_type);
                    Account account = new Account(email, accountType);
                    ((AuthActivity) mContext).onTokenReceived(account, "", token);
                    Log.i(LOG_TAG, "finish authorization");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


}
