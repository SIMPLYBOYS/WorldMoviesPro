package com.github.florent37.materialviewpager.sample.trends;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.roger.catloadinglibrary.CatLoadingView;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by aaron on 2016/6/30.
 */
public class TrendsWebViewActivity extends BaseActivity implements AdvancedWebView.Listener {
    private ProgressBar progressBar;
    private AdvancedWebView mWebView;
    private String castUrl;
    CatLoadingView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mView = new CatLoadingView();
        mView.show(getSupportFragmentManager(), "");
        castUrl = (String) getIntent().getSerializableExtra("url");

        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.setListener(this, this);
        mWebView.setGeolocationEnabled(false);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mView.dismiss();
                Toast.makeText(TrendsWebViewActivity.this, "Finished loading", Toast.LENGTH_SHORT).show();
            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                Toast.makeText(TrendsWebViewActivity.this, title, Toast.LENGTH_SHORT).show();
            }

        });
        mWebView.addHttpHeader("X-Requested-With", "");
        Log.d("0630", String.valueOf(castUrl));
        mWebView.loadUrl(castUrl);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) { return; }
        // ...
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        mWebView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        mWebView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        Toast.makeText(TrendsWebViewActivity.this, "onPageError(errorCode = "+errorCode+",  description = "+description+",  failingUrl = "+failingUrl+")", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Toast.makeText(TrendsWebViewActivity.this, "onDownloadRequested(url = "+url+",  userAgent = "+userAgent+",  contentDisposition = "+contentDisposition+",  mimetype = "+mimetype+",  contentLength = "+contentLength+")", Toast.LENGTH_LONG).show();

		/*final String filename = UUID.randomUUID().toString();

		if (AdvancedWebView.handleDownload(this, url, filename)) {
			// download successfully handled
		}
		else {
			// download couldn't be handled because user has disabled download manager app on the device
		}*/
    }

    @Override
    public void onExternalPageRequest(String url) {
        Toast.makeText(TrendsWebViewActivity.this, "onExternalPageRequest(url = "+url+")", Toast.LENGTH_SHORT).show();
    }
}
