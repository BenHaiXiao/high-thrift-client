package com.github.xiaobenhai.thrift.client.config;

import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.failover.FailoverCheckingStrategy;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  xiaobenhai
 */
public class ThriftClientConfigure {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftClientConfigure.class);
    private static final int CONNECTION_TIMEOUT_MILLIS = 3000;
    private ConnectionValidator validator;
    private FailoverCheckingStrategy<ThriftServerInfo> strategy;
    private GenericKeyedObjectPoolConfig poolConfig;

    /**
     * 采用直连时的服务器列表（ip:port，用逗号分隔）
     */
    private String servers;
    /**
     * 备用服务器列表（ip:port，用逗号分隔）（可选）
     */
    private String backupServers;

    private int timeout = CONNECTION_TIMEOUT_MILLIS;

    public ThriftClientConfigure() {
    }

    public ConnectionValidator getValidator() {
        return validator;
    }

    public void setValidator(ConnectionValidator validator) {
        this.validator = validator;
    }

    public FailoverCheckingStrategy<ThriftServerInfo> getStrategy() {
        return strategy;
    }

    public void setStrategy(FailoverCheckingStrategy<ThriftServerInfo> strategy) {
        this.strategy = strategy;
    }

    public GenericKeyedObjectPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(GenericKeyedObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getBackupServers() {
        return backupServers;
    }

    public void setBackupServers(String backupServers) {
        this.backupServers = backupServers;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "ThriftClientConfigure{" +
                "validator=" + validator +
                ", strategy=" + strategy +
                ", poolConfig=" + poolConfig +
                ", servers='" + servers + '\'' +
                ", backupServers='" + backupServers + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
