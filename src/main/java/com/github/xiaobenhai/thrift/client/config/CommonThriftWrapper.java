package com.github.xiaobenhai.thrift.client.config;

import com.github.xiaobenhai.thrift.client.RandomThriftClient;
import com.github.xiaobenhai.thrift.client.ThriftClient;
import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.failover.FailoverCheckingStrategy;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import com.github.xiaobenhai.thrift.client.registry.Provider;
import com.github.xiaobenhai.thrift.client.registry.local.LocalConfigProvider;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
/**
 * @author  xiaobenhai
 */
public class CommonThriftWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonThriftWrapper.class);

    private ThriftClient thriftClient;

    public CommonThriftWrapper(ThriftClientConfigure config) {

        LOGGER.info("thriftClientConfig : {}", config);

        Provider provider = getProvider(config);
        ConnectionValidator validator = getValidator(config);
        FailoverCheckingStrategy<ThriftServerInfo> strategy = getStrategy(config);
        GenericKeyedObjectPoolConfig poolConfig = getPoolConfig(config);

        thriftClient = new RandomThriftClient(provider, validator, strategy, poolConfig, config.getTimeout(), config.getBackupServers());

        LOGGER.info("get available server lists:{}", thriftClient.getAvaliableServers());
    }

    private Provider getProvider(ThriftClientConfigure config) {
        return new LocalConfigProvider(config.getServers());
    }

    private ConnectionValidator getValidator(ThriftClientConfigure config) {
        return config.getValidator();
    }

    private FailoverCheckingStrategy<ThriftServerInfo> getStrategy(ThriftClientConfigure config) {
        return config.getStrategy() != null ? config.getStrategy() : new FailoverCheckingStrategy();
    }

    private GenericKeyedObjectPoolConfig getPoolConfig(ThriftClientConfigure config) {
        if (config.getPoolConfig() != null) {
            return config.getPoolConfig();
        }

        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setMaxTotal(Integer.MAX_VALUE);
        poolConfig.setMaxTotalPerKey(10);
        poolConfig.setMaxIdlePerKey(10);
        poolConfig.setMinIdlePerKey(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setJmxEnabled(false);

        return poolConfig;
    }

    public void close() {
        if (thriftClient != null) {
            thriftClient.close();
        }
    }

    public List getAvaliableServers() {
        return thriftClient.getAvaliableServers();
    }

    public ThriftClient getThriftClient() {
        return thriftClient;
    }

}
