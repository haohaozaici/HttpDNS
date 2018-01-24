package io.github.haohaozaici.httpdns.network.httpdns;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.haohaozaici.httpdns.network.httpdns.HttpDnsRes.DnsBean;

/**
 * Created by haoyuan on 2018/1/23.
 * <p>
 * accountID 错误返回404
 * host错误返回empty ips
 * <p>
 * todo 1.同步请求自动重试
 * todo 2.网络切换更新缓存
 * todo 3.允许HTTP DNS返回TTL过期的域名
 * todo 4.使用代理情况
 */

public class HttpDnsCache {

    private static HttpDnsCache instance;

    private HttpDnsCache() {

    }

    public static synchronized HttpDnsCache getInstance() {
        if (instance == null) {
            instance = new HttpDnsCache();
        }
        return instance;
    }

    private static final String TAG = "HttpDnsCache";

    private String[] mHosts;
    private Map<String, DnsBean> mDnsCaches = new HashMap<>();

    private OkHttpClient client;

    private String HttpDnsServerIp = "203.107.1.33";
    private int account_id = 191607;

    private static final int RETRY_TIMES = 2;
    private int retryTimes = 0;

    private boolean preLoadSync = false;


    /**
     * 初始化accountID，域名解析列表
     *
     * @param accountID ali dns accountID
     * @param hosts     httpDNS 解析域名列表
     */
    public void init(int accountID, String... hosts) {
        account_id = accountID;
        mHosts = hosts;

        client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build();
    }


    /**
     * 首次同步加载
     * <p>
     * maxSize = 5
     */
    public boolean loadDnsCache() {

        if (mHosts == null || mHosts.length == 0)
            throw new NullPointerException("preload dns, hostname can not be null or empty");

        //选择http 或 https
        StringBuilder http_dns_url = new StringBuilder("http://" + HttpDnsServerIp + "/" + account_id);

        if (mHosts.length == 1) {
            http_dns_url.append("/d?host=").append(mHosts[0]);
        } else {
            http_dns_url.append("/resolve?host=");
            for (String hostname : mHosts) {
                http_dns_url.append(hostname).append(",");
            }
            http_dns_url.deleteCharAt(http_dns_url.length() - 1);
        }

        Request request = new Request.Builder()
                .url(http_dns_url.toString())
                .build();

        if (preLoadSync) {
            return requestCacheSync(request);
        } else {
            requestCache(request);
            return true;
        }

    }

    private boolean requestCacheSync(Request request) {

        try (Response response = client.newCall(request).execute()) {
            return handleResponse(request, response);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "requestCacheSync: " + e.getMessage());
            return false;
        }
    }

    private boolean handleResponse(Request request, Response response) throws IOException {
        if (response.isSuccessful()) {
            String res = response.body().string();

            if (request.url().encodedPath().contains("d")) {
                DnsBean dnsBean = new Gson().fromJson(res, DnsBean.class);
                saveCache(dnsBean);
            } else if (request.url().encodedPath().contains("resolve")) {
                HttpDnsRes dnsRes = new Gson().fromJson(res, HttpDnsRes.class);
                if (dnsRes.getDns().size() == 0)
                    throw new NullPointerException("dns resolved return null");

                for (DnsBean bean : dnsRes.getDns()) {
                    saveCache(bean);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * 异步解析接口
     * 1. 查询注册的DNS解析列表，若未注册返回null
     * 2. 查询缓存，若存在且未过期则返回结果，若不存在返回null并且进行异步域名解析更新缓存。
     * 3. 若接口返回null，，为避免影响业务请降级到local dns解析策略。
     *
     * @param hostname 域名(如www.aliyun.com)
     * @return 域名对应的解析结果
     */
    public String getIpByHostAsync(String hostname) {

        if (mHosts == null || mHosts.length == 0) return null;

        for (String host : mHosts) {
            if (host.equals(hostname)) {
                if (mDnsCaches == null || mDnsCaches.isEmpty()) {
                    updateIpByHostAsync(hostname);
                    //无缓存
                    return null;
                } else {
                    DnsBean dnsBean = mDnsCaches.get(hostname);
                    if (dnsBean != null) {
                        long timeSpan = Math.abs(System.currentTimeMillis() - dnsBean.getTime());
                        if (timeSpan < dnsBean.getTtl() * 1000) {
                            if (!dnsBean.getIps().isEmpty()) {
                                //正确返回缓存ip
                                return dnsBean.getIps().get(0);
                            } else {
                                //无缓存
                                return null;
                            }
                        } else {
                            updateIpByHostAsync(dnsBean.getHost());
                            //缓存过期
                            return null;
                        }
                    } else {
                        updateIpByHostAsync(hostname);
                        //无缓存
                        return null;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 更新DNS缓存
     */

    private void updateIpByHostAsync(String hostname) {

        //选择http 或 https
        Request request = new Request.Builder()
                .url("http://" + HttpDnsServerIp + "/" + account_id + "/d?host=" + hostname)
                .build();

        retryTimes = 0;
        requestCache(request);

    }


    private void requestCache(Request request) {
        retryTimes++;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (retryTimes < RETRY_TIMES) {
                    requestCache(request);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleResponse(request, response);

            }


        });
    }


    private void saveCache(DnsBean dnsBean) {
        mDnsCaches.put(dnsBean.getHost(), dnsBean);
    }

    /**
     * 设置自定义请求超时时间,默认为2S
     *
     * @param timeoutInterval 单位是毫秒（ms）
     */
    public void setTimeoutInterval(int timeoutInterval) {
        client = new OkHttpClient.Builder()
                .connectTimeout(timeoutInterval, TimeUnit.SECONDS)
                .readTimeout(timeoutInterval, TimeUnit.SECONDS)
                .build();
    }


    public void setPreLoadSync(boolean preLoadSync) {
        this.preLoadSync = preLoadSync;
    }

    public String getHttpDnsServerIp() {
        return HttpDnsServerIp;
    }

    public void setHttpDnsServerIp(String httpDnsServerIp) {
        HttpDnsServerIp = httpDnsServerIp;
    }
}
