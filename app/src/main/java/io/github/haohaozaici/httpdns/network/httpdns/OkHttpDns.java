package io.github.haohaozaici.httpdns.network.httpdns;

import android.support.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by haoyuan on 2018/1/23.
 */

public class OkHttpDns implements Dns {

    private static final String TAG = "OkHttpDns";

    private HttpDnsCache mDnsCache = HttpDnsCache.getInstance();


    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {

        String ip = mDnsCache.getIpByHostAsync(hostname);

        if (ip != null) {
            //如果ip不为null，直接使用该ip进行网络请求
            return Arrays.asList(InetAddress.getAllByName(ip));
        }
        //如果返回null，走系统DNS服务解析域名
        return Dns.SYSTEM.lookup(hostname);

    }

}
