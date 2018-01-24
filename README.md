# http dns

> 当移动app遇到区域性出现 `unsolved host name xxx` 的时候才会想起来吧。。。

使用HTTP请求和HTTPDNS服务，(在高可用情况下)替代传统DNS协议，解析DNS。

**大部分内容节选至以下参考文章**：

[移动互联网时代，如何优化你的网络 —— 域名解析篇](https://yq.aliyun.com/articles/58967?spm=5176.doc30102.2.4.Hv1CZk)

[【鹅厂网事】全局精确流量调度新思路-HttpDNS服务详解](https://mp.weixin.qq.com/s?__biz=MzA3ODgyNzcwMw==&mid=201837080&idx=1&sn=b2a152b84df1c7dbd294ea66037cf262&scene=2&from=timeline&isappinstalled=0&utm_source=tuicool)

[腾讯云：移动解析HttpDNS](https://cloud.tencent.com/document/product/379/3523)

# 基础概念

DNS：

ISP：

localDNS：

IDC：

ICP：

GSLB： 全局负载均衡

DHCP：

NAT：

根域、顶级域、二级域：

权威DNS：

递归DNS：<br>
<p>

![](https://yqfile.alicdn.com/a33d57c0a93ebf9eacf090e6f81a98bcda023334.png)

公共DNS：

HTTPS


# 传统的域名解析面临的问题

问题根源：ISP的LocalDNS解析域名异常

## 域名缓存

LocalDNS缓存了腾讯的域名的解析结果，不向腾讯权威DNS发起递归。

![](http://mmbiz.qpic.cn/mmbiz/iamZnEnc9u9q6LL0FOG25M1sU1Vnss1M2fHGaHqkDkPJmibfslQZzIbAGWbDn5eg2f6j6bbfiblk52zRC6zeSib8gQ/640?tp=webp&wxfrom=5&wx_lazy=1)

为何LocalDNS要把域名解析结果进行缓存呢？原因有以下几个：

- 保证用户访问流量在本网内消化：国内的各互联网接入运营商的带宽资源、网间结算费用、IDC机房分布、网内ICP资源分布等存在较大差异。为了保证网内用户的访问质量，同时减少跨网结算，运营商在网内搭建了内容缓存服务器，通过把域名强行指向内容缓存服务器的IP地址，就实现了把本地本网流量完全留在了本地的目的。

- 推送广告：有部分LocalDNS会把部分域名解析结果的所指向的内容缓存，并替换成第三方广告联盟的广告。

这种类型的行为就是我们常说的域名缓存，域名缓存会导致用户产生以下的访问异常：

- A. 仅对80端口的http服务做了缓存，如果域名是通过https协议或其它端口提供服务的，用户访问就会出现失败。比如支付服务、游戏通过指定端口连接connect server服务等。

- B. 缓存服务器的运维水平参差不齐，时有出现缓存服务器故障导致用户访问异常的问题。

## 解析转发

除了域名缓存以外，运营商的LocalDNS还存在解析转发的现象。解析转发是指运营商自身不进行域名递归解析，而是把域名解析请求转发到其它运营商的递归DNS上的行为。正常的LocalDNS递归解析过程是这样的：

![](http://mmbiz.qpic.cn/mmbiz/iamZnEnc9u9rP9Q2Ziat5My88XufLnWAfVmWllOQKjLZlqJrPefqnxy5bFXJetW6QPv5vBZVhJyibib1T6em1t8R2A/640?tp=webp&wxfrom=5&wx_lazy=1)

而部分小运营商为了节省资源，就直接将解析请求转发到了其它运营的递归LocalDNS上去了

![](http://mmbiz.qpic.cn/mmbiz/iamZnEnc9u9rP9Q2Ziat5My88XufLnWAfV4aa4W4eEma2K19RnTIDuOx6TMW8oicuNq7XsFz2y3S0EOvkPPPCNXYw/640?tp=webp&wxfrom=5&wx_lazy=1)

这样的直接后果就是腾讯权威DNS收到的域名解析请求的来源IP就成了其它运营商的IP，最终导致用户流量被导向了错误的IDC，用户访问变慢。

## LocalDNS递归出口NAT

LocalDNS递归出口NAT指的是运营商的LocalDNS按照标准的DNS协议进行递归，但是因为在网络上存在多出口且配置了目标路由NAT，结果导致LocalDNS最终进行递归解析的时候的出口IP就有概率不为本网的IP地址

![](http://mmbiz.qpic.cn/mmbiz/iamZnEnc9u9rP9Q2Ziat5My88XufLnWAfVuc0D99eFptwClfLVm7IV9gWiaHq33b1icrCDvFichib7efLKv9pjD3wXwg/640?tp=webp&wxfrom=5&wx_lazy=1)

这样的直接后果就是GSLB DNS收到的域名解析请求的来源IP还是成了其它运营商的IP，最终导致用户流量被导向了错误的IDC，用户访问变慢。

## 解析生效滞后

部分业务场景下开发者对域名解析结果变更的生效时间非常敏感（这部分变更操作是开发者在权威DNS上完成的），比如当业务服务器受到攻击时，我们需要最快速地将业务IP切换到另一组集群上，这样的诉求在传统域名解析体系下是无法完成的。

Local DNS的部署是由各个地区的各个运营商独立部署的，因此各个Local DNS的服务质量参差不齐。在对域名解析缓存的处理上，各个独立节点的实现策略也有区别，比如部分节点为了节省开支忽略了域名解析结果的TTL时间限制，导致用户在权威DNS变更的解析结果全网生效的周期非常漫长（我们已知的最长生效时间甚至高达48小时）。这类延迟生效可能直接导致用户业务访问的异常。



# HTTPDNS

## 基本原理

HttpDNS是为移动客户端量身定做的基于Http协议和域名解析的流量调度解决方案，专治LocalDNS解析异常以及流量调度不准。详细介绍如下：

![](http://mmbiz.qpic.cn/mmbiz/iamZnEnc9u9rP9Q2Ziat5My88XufLnWAfVQLtXPAzCib2KWdncL2GHrzibKVzQ1JISJgbFGWuqsK9MThCWe6PFvsjA/640?tp=webp&wxfrom=5&wx_lazy=1)

HttpDNS的原理非常简单，主要有两步：

1. 客户端直接访问HttpDNS接口，获取业务在域名配置管理系统上配置的访问延迟最优的IP。（基于容灾考虑，还是保留次选使用运营商LocalDNS解析域名的方式）

2. 客户端向获取到的IP后就向直接往此IP发送业务协议请求。以Http请求为例，通过在header中指定host字段，向HttpDNS返回的IP发送标准的Http请求即可。

## 优点

[`SLA 服务级别协议`](https://zh.wikipedia.org/zh-hans/%E6%9C%8D%E5%8A%A1%E7%BA%A7%E5%88%AB%E5%8D%8F%E8%AE%AE) 保证

## 缺点

延迟：HTTP DNS基于TCP协议请求响应时间更长，但可以使用缓存解析结果、预加载、懒加载等方式，异步处理httpDNS的请求时间。

# 实践方案

移动APP的域名解析机制，新的流程参考如下：

![](https://mccdn.qcloud.com/static/img/21b1ec2b1656ead133d18ad136320dda/image.png)


需要注意的是，发起网络请求时，在本地无缓存，或缓存已过期的情况下，直接使用localDNS解析，并同时异步更新本地DNS缓存。

## Failed over策略「降级」

虽然HttpDNS已经接入BGP Anycast，并实现了多地跨机房容灾。但为了保证在最坏的情况下客户端域名解析依然不受影响。建议采用以下的fail over策略：

- 第一步先向HttpDNS发起域名查询请求
- 如果HttpDNS查询返回的结果不是一个IP地址（结果为空、结果非IP、连接超时等），则通过本地    LocalDNS进行域名解析。超时时间建议为5s。

不管是因为什么原因，当通过HTTPDNS服务无法获得域名对应的IP时，都必须降级：使用标准的DNS解析，通过Local DNS去解析域名。

## 缓存策略

移动互联网用户的网络环境比较复杂，为了尽可能地减少由于域名解析导致的延迟，建议在本地进行缓存。缓存规则如下：

- 缓存时间 <br>
  缓存时间建议设置为120s至600s，不可低于60s。

- 缓存更新 <br>
  缓存更新应在以下两种情形下进行：<br>
  **用户网络状态发生变化时**：
  移动互联网的用户的网络状态由3G切Wi-Fi，Wi-Fi切3G的情况下，其接入点的网络归属可能发生变化。所以用户的网络状态发生变化时，需要重新向HttpDNS发起域名解析请求，以获得用户当前网络归属下的最优指向。

  **缓存过期时**：<br>
  当域名解析的结果缓存时间到期时，客户端应该向HttpDNS重新发起域名解析请求以获取最新的域名对应的IP。为了减少用户在缓存过期后重新进行域名解析时的等待时间，建议在75%TTL时就开始进行域名解析。如本地缓存的TTL为600s，那么在第600*0.75=450s时刻,客户端就应该进行域名解析。


除了以上几点建议外，减少域名解析的次数也能有效的减少网络交互，提升用户访问体验。建议在业务允许的情况下，尽量减少域名的数量。如需区分不同的资源，建议通过url来进行区分。

## 异步请求、懒加载

异步请求策略：解析域名时，如果当前缓存中有TTL未过期的IP，可直接使用；如果没有，则立刻让此次请求降级走原生LocalDNS解析，同时另起线程异步地发起HTTPDNS请求进行解析，更新缓存，这样后续解析域名时就能命中缓存。

1. 查询注册的DNS解析列表，若未注册返回null
2. 查询缓存，若存在且未过期则返回结果，若不存在返回null并且进行异步域名解析更新缓存。
3. 若接口返回null，降级到local dns解析策略。

## 重试

访问HTTPDNS服务解析域名时，如果请求HTTPDNS服务端失败，即HTTP请求没有返回，可以进行重试。
大部分情况下，这种访问失败是由于网络原因引起的，重试可以解决。


## 预解析

在初始化阶段针对业务的热点域名在后台发起异步的HTTPDNS解析请求。这部分预解析结果在后续的业务请求中可以直接使用，进而消除首次业务请求的DNS解析开销，提升APP首页的加载速度。


## HTTPS情况

[HTTPS（含SNI）业务场景“IP直连”方案说明](https://help.aliyun.com/document_detail/30143.html?spm=5176.doc30140.6.562.9duHZH)

## webview

[Android Webview + HttpDns最佳实践](https://help.aliyun.com/document_detail/60181.html?spm=5176.doc30144.6.565.1qauiM)

## 代理情况

当存在中间HTTP代理时，客户端发起的请求中请求行会使用绝对路径的URL，在开启HTTPDNS并采用IP URL进行访问时，中间代理将识别您的IP信息并将其作为真实访问的HOST信息传递给目标服务器，这时目标服务器将无法处理这类无真实HOST信息的HTTP请求。

绝大多数场景下，在代理模式下关闭HTTPDNS功能。

## 注意事项

1. 设置的缓存TTL值不可太低（不可低于60s），防止频繁进行HtppDNS请求。
2. 接入HttpDNS的业务需要保留用户本地LocalDNS作为容灾通道，当HttpDNS无法正常服务时（移动网络不稳定或HttpDNS服务出现问题），可以使用LocalDNS进行解析。
3. https问题，需在客户端hook客户端检查证书的domain域和扩展域看是否包含本次请求的host的过程，将IP直接替换成原来的域名，再执行证书验证。或者忽略证书认证，类似于curl -k参数。

4. HttpDNS请求建议超时时间2-5s左右。
5. 在网络类型变化时，如4G切换到wifi，不同wifi间切换等，需要重新执行HttpDNS请求刷新本地缓存。
