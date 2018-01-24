package io.github.haohaozaici.httpdns;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.elvishew.xlog.XLog;

import io.github.haohaozaici.httpdns.network.httpdns.HttpDnsCache;

/**
 * Created by haoyuan on 2018/1/23.
 */

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        XLog.init();

        //preload dns cache
        HttpDnsCache dnsCache = HttpDnsCache.getInstance();
        dnsCache.init(191607, "app.bilibili.com", "api.bilibili.com");
        dnsCache.loadDnsCache();

    }
}
