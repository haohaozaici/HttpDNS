package io.github.haohaozaici.httpdns.feature.bilibilipic;

import java.util.List;

/**
 * Created by haoyuan on 2018/1/7.
 */

public class SplashPicRes {


  /**
   * code : 0 data : [{"id":1001,"animate":1,"duration":3,"start_time":1515254400,"end_time":1515340740,"image":"http://i0.hdslb.com/bfs/archive/fedf1d0b2a88b7f33cfbea31877ed122d75d360c.jpg","key":"3a28f7cc6f8eb95e8fe92f795e9e6c01","times":5,"type":1,"param":"https://bangumi.bilibili.com/anime/21421","skip":1},{"id":998,"animate":1,"duration":3,"start_time":1515168000,"end_time":1515254340,"image":"http://i0.hdslb.com/bfs/archive/7d8ff668b8c3b655df151e0125e67dc5ecb5e9ab.jpg","key":"92062d2aa5f0898f13ca652fb4432148","times":5,"type":1,"param":"https://www.bilibili.com/blackboard/topic/activity-rJKVuOsmf.html","skip":1},{"id":994,"animate":1,"duration":3,"start_time":1515081600,"end_time":1515167940,"image":"http://i0.hdslb.com/bfs/archive/cf6d262a417bd3fd1bfe1c8d548ddfac219441fa.jpg","key":"799c6be9ff1000f0cfa72e6d10627ebd","times":5,"type":1,"param":"https://bangumi.bilibili.com/anime/21728?from=search&seid=4480104428473247835","skip":1}]
   * version : 9223372036854775807
   */

  private int code;
  private long version;
  private List<DataBean> data;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<DataBean> getData() {
    return data;
  }

  public void setData(List<DataBean> data) {
    this.data = data;
  }

  public static class DataBean {

    /**
     * id : 1001
     * animate : 1
     * duration : 3
     * start_time : 1515254400
     * end_time : 1515340740
     * image : http://i0.hdslb.com/bfs/archive/fedf1d0b2a88b7f33cfbea31877ed122d75d360c.jpg
     * key : 3a28f7cc6f8eb95e8fe92f795e9e6c01
     * times : 5
     * type : 1
     * param : https://bangumi.bilibili.com/anime/21421
     * skip : 1
     */

    private int id;
    private int animate;
    private int duration;
    private int start_time;
    private int end_time;
    private String image;
    private String key;
    private int times;
    private int type;
    private String param;
    private int skip;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public int getAnimate() {
      return animate;
    }

    public void setAnimate(int animate) {
      this.animate = animate;
    }

    public int getDuration() {
      return duration;
    }

    public void setDuration(int duration) {
      this.duration = duration;
    }

    public int getStart_time() {
      return start_time;
    }

    public void setStart_time(int start_time) {
      this.start_time = start_time;
    }

    public int getEnd_time() {
      return end_time;
    }

    public void setEnd_time(int end_time) {
      this.end_time = end_time;
    }

    public String getImage() {
      return image;
    }

    public void setImage(String image) {
      this.image = image;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public int getTimes() {
      return times;
    }

    public void setTimes(int times) {
      this.times = times;
    }

    public int getType() {
      return type;
    }

    public void setType(int type) {
      this.type = type;
    }

    public String getParam() {
      return param;
    }

    public void setParam(String param) {
      this.param = param;
    }

    public int getSkip() {
      return skip;
    }

    public void setSkip(int skip) {
      this.skip = skip;
    }
  }
}
