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
package com.github.xiaobenhai.thrift.client.pool;

import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.failover.FailoverChecker;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author  xiaobenhai
 */
public class ThriftConnectionFactory implements KeyedPooledObjectFactory<ThriftServerInfo, TTransport> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultThriftConnectionPool.class);

	private int timeout;

	private FailoverChecker checker;

	public ThriftConnectionFactory(int timeout) {
		this.timeout = timeout;
	}

	public ThriftConnectionFactory() {
		this((int) TimeUnit.SECONDS.toMillis(5));
	}

	public ThriftConnectionFactory(FailoverChecker checker, int timeout) {
		this.checker = checker;
		this.timeout = timeout;
	}
	public ThriftConnectionFactory(FailoverChecker checker) {
		this((int) TimeUnit.SECONDS.toMillis(5));
		this.checker = checker;
	}


	@Override
	public PooledObject<TTransport> makeObject(ThriftServerInfo info) throws Exception {
		TSocket tsocket = new TSocket(info.getHost(), info.getPort());
		tsocket.setTimeout(timeout);
		TFramedTransport transport = new TFramedTransport(tsocket);

		transport.open();
		DefaultPooledObject<TTransport> result = new DefaultPooledObject<TTransport>(transport);
		logger.trace("make new thrift connection:{}", info);
		return result;
	}

	@Override
	public void destroyObject(ThriftServerInfo info, PooledObject<TTransport> p) throws Exception {
		TTransport transport = p.getObject();
		if (transport != null) {
			transport.close();
			logger.trace("close thrift connection:{}", info);
		}
	}

	@Override
	public boolean validateObject(ThriftServerInfo info, PooledObject<TTransport> p) {
		boolean isValidate;
		try {
			if (checker == null) {
				isValidate = p.getObject().isOpen();
			} else {
				ConnectionValidator validator = checker.getConnectionValidator();
				isValidate = p.getObject().isOpen() && (validator == null || validator.isValid(p.getObject()));
			}
		} catch (Throwable e) {
			logger.warn("fail to validate tsocket:{}", info, e);
			isValidate = false;
		}
		//
		if (checker != null && !isValidate) {
			checker.getFailoverCheckingStrategy().fail(info);
		}
		logger.info("validateObject isValidate:{}", isValidate);
		return isValidate;
	}

	@Override
	public void activateObject(ThriftServerInfo info, PooledObject<TTransport> p) throws Exception {
		// do nothing
	}

	@Override
	public void passivateObject(ThriftServerInfo info, PooledObject<TTransport> p) throws Exception {
		// do nothing
	}

}
