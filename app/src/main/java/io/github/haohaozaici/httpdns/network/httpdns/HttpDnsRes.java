package io.github.haohaozaici.httpdns.network.httpdns;

import java.util.Date;
import java.util.List;

/**
 * Created by haoyuan on 2018/1/23.
 */

public class HttpDnsRes {

/*{
    "dns": [
        {
            "host": "api.bilibili.com",
            "client_ip": "58.49.114.214",
            "ips": [
                "116.207.118.12"
            ],
            "ttl": 95,
            "origin_ttl": 180
        },
        {
            "host": "app.bilibili.com",
            "client_ip": "58.49.114.214",
            "ips": [
                "116.207.118.12"
            ],
            "ttl": 180,
            "origin_ttl": 180
        }
    ]
}*/

    private List<DnsBean> dns;

    public List<DnsBean> getDns() {
        return dns;
    }

    public void setDns(List<DnsBean> dns) {
        this.dns = dns;
    }

    public static class DnsBean {

        public DnsBean() {
            this.date = new Date();
        }

        /**
         * host : api.bilibili.com
         * client_ip : 58.49.114.214
         * ips : ["116.207.118.12"]
         * ttl : 123
         * origin_ttl : 180
         */


        private String host;
        private String client_ip;
        private int ttl;
        private int origin_ttl;
        private List<String> ips;
        private Date date;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getClient_ip() {
            return client_ip;
        }

        public void setClient_ip(String client_ip) {
            this.client_ip = client_ip;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public int getOrigin_ttl() {
            return origin_ttl;
        }

        public void setOrigin_ttl(int origin_ttl) {
            this.origin_ttl = origin_ttl;
        }

        public List<String> getIps() {
            return ips;
        }

        public void setIps(List<String> ips) {
            this.ips = ips;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
