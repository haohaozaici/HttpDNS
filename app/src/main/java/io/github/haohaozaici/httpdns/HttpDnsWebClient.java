package io.github.haohaozaici.httpdns;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by haoyuan on 2018/1/25.
 */

public class HttpDnsWebClient extends WebViewClient {

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {



        return super.shouldInterceptRequest(view, url);
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {



        return super.shouldInterceptRequest(view, request);
    }
}
