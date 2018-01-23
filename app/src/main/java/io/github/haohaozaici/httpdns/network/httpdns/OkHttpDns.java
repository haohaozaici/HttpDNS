package io.github.haohaozaici.httpdns.network.httpdns;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.haohaozaici.httpdns.network.httpdns.HttpDnsRes.DnsBean;

/**
 * Created by haoyuan on 2018/1/23.
 */

public class OkHttpDns implements Dns {

    private static final String TAG = "OkHttpDns";

    public static final String HttpDnsServerIp = "203.107.1.33";
    public static final int ACCOUNT_ID = 191607;

    private static final int RETRY_TIMES = 2;
    private int retryTimes = 0;

    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {

        if (useDnsCache(HttpDnsCache.AppDnsCache)) {
            return Arrays.asList(InetAddress.getAllByName(HttpDnsCache.AppDnsCache.getIps().get(0)));
        } else {
            return Dns.SYSTEM.lookup(hostname);
        }

    }

    private boolean useDnsCache(DnsBean dnsBean) {
        if (dnsBean != null) {
            long timeSpan = Math.abs(System.currentTimeMillis() - dnsBean.getDate().getTime());
            if (timeSpan < dnsBean.getTtl() * 1000) {
                return true;
            } else {
                updateIpByHostAsync(dnsBean.getHost());
                return false;
            }
        } else {
            // TODO: 2018/1/23 load dns cache
            return false;
        }
    }

    /**
     * 更新DNS缓存
     */
    private void updateIpByHostAsync(String hostname) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        //选择http 或 https
        String http_dns_url = "http://" + HttpDnsServerIp + "/191607/d?host=" + hostname;

        Request request = new Request.Builder()
                .url(http_dns_url)
                .build();

        retryTimes = 0;
        call(client, request, hostname);


    }


    private void call(OkHttpClient client, Request request, String hostname) {
        retryTimes++;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (retryTimes < RETRY_TIMES) {
                    call(client, request, hostname);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();

                Log.d(TAG, "onResponse: " + res);
            }
        });
    }

    private void updateIpByHostSync(String hostname) {
        //httpdns 解析

        //

    }
}
