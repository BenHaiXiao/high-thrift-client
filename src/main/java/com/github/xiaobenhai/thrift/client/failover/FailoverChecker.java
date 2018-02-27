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
package com.github.xiaobenhai.thrift.client.failover;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.github.xiaobenhai.thrift.client.pool.ThriftConnectionPool;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;

/**
 * @author  xiaobenhai
 */
public class FailoverChecker {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private volatile List<ThriftServerInfo> serverInfoList;

	private List<ThriftServerInfo> backupServerInfoList;

	private FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy;

	private ThriftConnectionPool pool;

	private ConnectionValidator connectionValidator;

	private ScheduledExecutorService checkExecutor;

	private boolean enableChecker = false;

	private long checkDelayTime = 10000;

	private long checkPeriodTime = 5000;

	public FailoverChecker(ConnectionValidator connectionValidator) {
		this(connectionValidator, new FailoverCheckingStrategy<ThriftServerInfo>());
	}

	public FailoverChecker(ConnectionValidator connectionValidator, FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy) {
		this.connectionValidator = connectionValidator;
		this.failoverCheckingStrategy = failoverCheckingStrategy;
	}

	public void startChecking() {
		if (connectionValidator != null && enableChecker) {
			ThreadFactory bossThreadFactory = new ThreadFactoryBuilder().setNameFormat("Fail Checking Worker").build();
			checkExecutor = Executors.newSingleThreadScheduledExecutor(bossThreadFactory);
			checkExecutor.scheduleAtFixedRate(new Checker(), checkDelayTime, checkPeriodTime, TimeUnit.MILLISECONDS);
		}
	}

	public void stopChecking() {
		if (checkExecutor != null)
			checkExecutor.shutdown();

	}

	public List<ThriftServerInfo> getAvailableServers() {
		return getAvailableServers(false);
	}

	private List<ThriftServerInfo> getAvailableServers(boolean all) {
		List<ThriftServerInfo> returnList = new ArrayList<>();
		Set<ThriftServerInfo> failedServers = failoverCheckingStrategy.getFailed();
		for (ThriftServerInfo thriftServerInfo : serverInfoList) {
			if (!failedServers.contains(thriftServerInfo))
				returnList.add(thriftServerInfo);
		}
		if ((all || returnList.isEmpty()) && !backupServerInfoList.isEmpty()) {
			for (ThriftServerInfo thriftServerInfo : backupServerInfoList) {
				if (!failedServers.contains(thriftServerInfo))
					returnList.add(thriftServerInfo);
			}
		}
		// 如果所有的服务都命中隔离策略，那就降级使用serverInfoList,防止所有的服务都不稳定的情况，无服务可用
		if (returnList.isEmpty()) {
			returnList.addAll(serverInfoList);
		}
		return returnList;
	}

	private class Checker implements Runnable {
		@Override
		public void run() {
			for (ThriftServerInfo thriftServerInfo : getAvailableServers(true)) {
				TTransport tt = null;
				boolean valid = false;
				try {
					tt = pool.getConnection(thriftServerInfo);
					valid = connectionValidator.isValid(tt);
				} catch (Exception e) {
					valid = false;
					logger.warn(e.getMessage(), e);
				} finally {
					if (tt != null) {
						if (valid) {
							pool.returnConnection(thriftServerInfo, tt);
						} else {
							failoverCheckingStrategy.fail(thriftServerInfo);
							pool.returnBrokenConnection(thriftServerInfo, tt);
						}
					} else {
						failoverCheckingStrategy.fail(thriftServerInfo);
					}
				}
			}
		}
	}

	public void setConnectionPool(ThriftConnectionPool pool) {
		this.pool = pool;
	}

	public void setServerInfoList(List<ThriftServerInfo> serverInfoList) {
		this.serverInfoList = serverInfoList;
	}

	public void setBackupServerInfoList(List<ThriftServerInfo> backupServerInfoList) {
		this.backupServerInfoList = backupServerInfoList;
	}

	public FailoverCheckingStrategy<ThriftServerInfo> getFailoverCheckingStrategy() {
		return failoverCheckingStrategy;
	}

	public ConnectionValidator getConnectionValidator() {
		return connectionValidator;
	}

	public void setEnableChecker(boolean enableChecker) {
		this.enableChecker = enableChecker;
	}

	public void setCheckDelayTime(long checkDelayTime) {
		this.checkDelayTime = checkDelayTime;
	}

	public void setCheckPeriodTime(long checkPeriodTime) {
		this.checkPeriodTime = checkPeriodTime;
	}
}
