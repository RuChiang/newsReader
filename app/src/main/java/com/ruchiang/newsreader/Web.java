package com.ruchiang.newsreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Web extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent intent = getIntent();

        int index = intent.getIntExtra("index", -1);

        WebView webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());


        if (DetectConnection.checkInternetConnection(getApplicationContext())) {
            if (MainActivity.urlsList.get(index).equals("URL not found")) {
                webView.loadData("<html><body><h1>Page not found</h1></body></html>", "html/text", "UTF-8");
            } else {
                webView.loadUrl(MainActivity.urlsList.get(index));
            }
        }else{
            Toast.makeText(getApplicationContext(), "No Internet!", Toast.LENGTH_SHORT).show();
            webView.loadData("<html><body><h1>No internt connection</h1></body></html>", "html/text", "UTF-8");

        }




    }
}
