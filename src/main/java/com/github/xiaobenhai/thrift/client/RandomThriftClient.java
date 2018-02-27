/*
 * Copyright (c) 2014 yy.com. 
 *
 * All Rights Reserved.
 *
 * This program is the confidential and proprietary information of 
 * YY.INC. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with yy.com.
 */
package com.github.xiaobenhai.thrift.client;

import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.failover.FailoverCheckingStrategy;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import com.github.xiaobenhai.thrift.client.registry.Provider;
import com.github.xiaobenhai.thrift.client.utils.ThriftClientUtils;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.TServiceClient;

/**
 * @author  xiaobenhai
 */
public class RandomThriftClient extends DefaultThriftClient {

    public RandomThriftClient(Provider<ThriftServerInfo> provider) {
        this(provider, null, new GenericKeyedObjectPoolConfig(), null);
    }

    public RandomThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator) {
        this(provider, validator, new GenericKeyedObjectPoolConfig(), null);
    }

    public RandomThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, GenericKeyedObjectPoolConfig poolConfig) {
        this(provider, validator, poolConfig, null);
    }

    public RandomThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, GenericKeyedObjectPoolConfig poolConfig, String backupServers) {
        super(provider, validator, poolConfig, backupServers);
    }

    public RandomThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, FailoverCheckingStrategy<ThriftServerInfo> strategy,
                            GenericKeyedObjectPoolConfig poolConfig, int connTimeout, String backupServers) {
        super(provider, validator, strategy, poolConfig, connTimeout, backupServers);
    }

    public RandomThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, FailoverCheckingStrategy<ThriftServerInfo> strategy,
                              GenericKeyedObjectPoolConfig poolConfig, int connTimeout, String backupServers,  boolean enableChecker, long checkDelayMills, long checkPeriodMills) {
        super(provider, validator, strategy, poolConfig, connTimeout, backupServers, enableChecker, checkDelayMills, checkPeriodMills);
    }

    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
        return iface(ifaceClass, ThriftClientUtils.randomNextInt());
    }

}
