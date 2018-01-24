package io.github.haohaozaici.httpdns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;

import org.reactivestreams.Subscription;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.haohaozaici.httpdns.feature.bilibilipic.SplashPicRes;
import io.github.haohaozaici.httpdns.network.Network;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.query) Button query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        query.setOnClickListener(v -> {
            Network.getInstance().getApiService().getSplashPic().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new FlowableSubscriber<SplashPicRes>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(1);
                        }

                        @Override
                        public void onNext(SplashPicRes splashPicRes) {
                            XLog.json(new Gson().toJson(splashPicRes));
                        }

                        @Override
                        public void onError(Throwable t) {
                            XLog.d(t);
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        });


    }
}
