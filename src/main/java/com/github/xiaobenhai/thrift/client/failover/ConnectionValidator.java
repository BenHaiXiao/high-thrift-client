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

import org.apache.thrift.transport.TTransport;

/**
 * @author  xiaobenhai
 */
public interface ConnectionValidator {
	boolean isValid(TTransport object);
}
