package io.github.haohaozaici.httpdns.network.api;

import io.github.haohaozaici.httpdns.feature.bilibilipic.SplashPicRes;
import io.reactivex.Flowable;
import retrofit2.http.GET;

/**
 * Created by haoyuan on 2018/1/23.
 */

public interface APIService {


    String BiliBili_API_HOST = "https://app.bilibili.com/";


    @GET("/x/splash?plat=0&width=1080&height=1920")
    Flowable<SplashPicRes> getSplashPic();
}
