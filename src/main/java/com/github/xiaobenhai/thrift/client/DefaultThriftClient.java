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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.failover.FailoverChecker;
import com.github.xiaobenhai.thrift.client.failover.FailoverCheckingStrategy;
import com.github.xiaobenhai.thrift.client.pool.DefaultThriftConnectionPool;
import com.github.xiaobenhai.thrift.client.pool.ThriftConnectionFactory;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import com.github.xiaobenhai.thrift.client.registry.Provider;
import com.github.xiaobenhai.thrift.client.registry.ProviderListener;
import com.github.xiaobenhai.thrift.client.utils.ThriftClientUtils;

/**
 * @author  xiaobenhai
 */
public class DefaultThriftClient implements ThriftClient, ProviderListener<ThriftServerInfo> {

	private final static Logger logger = LoggerFactory.getLogger(DefaultThriftClient.class);

	private AtomicInteger i = new AtomicInteger(0);

	private FailoverChecker failoverChecker;

	private DefaultThriftConnectionPool pool;

	private Provider<ThriftServerInfo> provider;

	private final static int DEFAULT_CONN_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);

	public DefaultThriftClient(Provider<ThriftServerInfo> provider) {
		this(provider, null, new GenericKeyedObjectPoolConfig(), null);
	}

	public DefaultThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator) {
		this(provider, validator, new GenericKeyedObjectPoolConfig(), null);
	}

	public DefaultThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, GenericKeyedObjectPoolConfig poolConfig) {
		this(provider, validator, poolConfig, null);
	}

	public DefaultThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator, GenericKeyedObjectPoolConfig poolConfig,
			String backupServers) {
		this(provider, validator, new FailoverCheckingStrategy<ThriftServerInfo>(), poolConfig, DEFAULT_CONN_TIMEOUT, backupServers);
	}

	public DefaultThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator,
							   FailoverCheckingStrategy<ThriftServerInfo> strategy, GenericKeyedObjectPoolConfig poolConfig, int connTimeout,
							   String backupServers) {
		this(provider, validator, strategy, poolConfig, connTimeout, backupServers, false, 10000, 5000);
	}

	public DefaultThriftClient(Provider<ThriftServerInfo> provider, ConnectionValidator validator,
			FailoverCheckingStrategy<ThriftServerInfo> strategy, GenericKeyedObjectPoolConfig poolConfig, int connTimeout,
			String backupServers, boolean enableChecker, long checkDelayMills, long checkPeriodMills) {
		this.provider = provider;
		provider.addListener(this);
		this.failoverChecker = new FailoverChecker(validator, strategy);
		this.pool = new DefaultThriftConnectionPool(new ThriftConnectionFactory(failoverChecker, connTimeout), poolConfig);
		failoverChecker.setConnectionPool(pool);
		Collection<ThriftServerInfo> serverInfos = provider.list();
		for (ThriftServerInfo thriftServerInfo : serverInfos) {
			logger.info("SET THRIFT SERVER INFO + {}", thriftServerInfo);
		}
		failoverChecker.setServerInfoList(serverInfos.isEmpty() ? new ArrayList<ThriftServerInfo>() : Lists.newArrayList(serverInfos));
		List<ThriftServerInfo> serverInfoList = Strings.isNullOrEmpty(backupServers) ? new ArrayList<ThriftServerInfo>() : ThriftServerInfo
				.ofs(backupServers);
		failoverChecker.setBackupServerInfoList(serverInfoList);
		failoverChecker.setEnableChecker(enableChecker);
		if(enableChecker && checkDelayMills > 0){
			failoverChecker.setCheckDelayTime(checkDelayMills);
		}
		if(enableChecker && checkPeriodMills > 0){
			failoverChecker.setCheckPeriodTime(checkPeriodMills);
		}
		failoverChecker.startChecking();
	}

	@Override
	public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
		return iface(ifaceClass, i.getAndDecrement());
	}

	protected <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash) {
		List<ThriftServerInfo> servers = getAvaliableServers();
		if (servers == null || servers.isEmpty()) {
			throw new NullPointerException("servers could not be null");
		}
		hash = Math.abs(hash);
		final ThriftServerInfo selected = servers.get(hash % servers.size());
		return iface(ifaceClass, selected);
	}

	@SuppressWarnings("unchecked")
	protected <X extends TServiceClient> X iface(final Class<X> ifaceClass, final ThriftServerInfo selected) {
		final TTransport transport;
		try {
			transport = pool.getConnection(selected);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof TTransportException)
				failoverChecker.getFailoverCheckingStrategy().fail(selected);
			throw e;
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(ifaceClass);
		factory.setFilter(new MethodFilter() {
			@Override
			public boolean isHandled(Method m) {
				return ThriftClientUtils.getInterfaceMethodNames(ifaceClass).contains(m.getName());
			}
		});
		try {
			X x = (X) factory.create(new Class[] { TProtocol.class }, new Object[] { protocol });
			((Proxy) x).setHandler(new MethodHandler() {
				@Override
				public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {

					boolean success = false;
					try {
						Object result = proceed.invoke(self, args);
						success = true;
						return result;
					} finally {
						if (success) {
							pool.returnConnection(selected, transport);
						} else {
							failoverChecker.getFailoverCheckingStrategy().fail(selected);
							pool.returnBrokenConnection(selected, transport);
							logger.warn("<<<< fail thrift server >>>> {}", selected);
						}
					}
				}
			});
			return x;
		} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException("fail to create proxy.", e);
		}
	}

	@Override
	public void close() {
		failoverChecker.stopChecking();
		pool.close();
	}

	@Override
	public List<ThriftServerInfo> getAvaliableServers() {
		return failoverChecker.getAvailableServers();
	}

	@Override
	public void onFresh() {
		Collection<ThriftServerInfo> serverInfoList = provider.list();
		logger.info("ON_FRESH SERVER_INFO_LIST:{}", serverInfoList);
		if (serverInfoList.isEmpty()) {
			logger.warn("onFresh event: serverInfo is empty.");
		} else {
			failoverChecker.setServerInfoList(Lists.newArrayList(serverInfoList));
		}
	}

}
