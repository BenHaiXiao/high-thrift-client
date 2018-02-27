##thrift-client
目前在大公司生产环境使用。
1.	简要说明
Thrift-client提供自动failover的高可用Thrift连接池，支持顺序（Default）、随机（Random）、加权（Weight）、哈希（Hash）等多种客户端。
目前提供手动配置服务列表方式。预留服务注册和发现扩展接口
- 支持连接池
- 支持thrift描述文件的ping校验
- 支持负载均衡策略
- 支持自动熔断策略
- 预留配置中心的服务发现接口，使用方自行扩展

##quick start

```
使用示例：
``` java
   // 连接校验方法，pool 的validate也会调用改isValid方法，（这里主要用于支持业务层ping方法的thrift协议）
        ConnectionValidator connectionValidator = new ConnectionValidator() {
            @Override
            public boolean isValid(TTransport object) {
                // 如果需要支持ping校验，请在这里实现thrift协议的ping方法
                return true;
            }
        };
        // 连接池的配置，thrift TTransport连接是基于common pool2 来管理
        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        // 后备服务配置，当Register中获取不到，会降级使用这里的配置
        String backupServers = "localhost:14465";
        /**
         * 
         * 这里后续基于配置中心的Register ,使用方自行扩展
         */
        Registry registry = new LocalConfigProvider("127.0.0.1:14465,127.0.0.1:14465");
        int timeout = 5000;//socket连接超时时间
        // 客户端熔断策略，默认1min中出现10次异常，则自动隔离，默认恢复时间为1min
        FailoverCheckingStrategy<ThriftServerInfo> strategy = new FailoverCheckingStrategy<>();

        /**
         * 这里实现了
         * DefaultThriftClient
         * HashThriftClient
         * RandomThriftClient
         * WeightThriftClient
         */
        RandomThriftClient randomThriftClient = new RandomThriftClient(registry, connectionValidator, strategy, poolConfig, timeout, backupServers);

        // iface方法会通过代理生成thrift 具体的client，iface内封装了错误统计，回池等操作，故每次使用时，都需要调用iface拿取连接
        DemoService.Client client = randomThriftClient.iface(DemoService.Client.class);
```

## user guide
### 本地服务发现
如果不想使用配置中心或者s2s，只想简单的写死配置，可以直接使用
connStr:格式如下：ip:port,ip:port
```
new LocalConfigRegister(String connStr) ;
```
