package edu.monash.fit2081.countryinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebWiki extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_wiki);
        getSupportActionBar().setTitle("WIKI");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String countryName = getIntent().getStringExtra("countryName");
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://en.wikipedia.org/wiki/" + countryName);
    }
}
