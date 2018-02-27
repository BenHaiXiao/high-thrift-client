package com.github.xiaobenhai.thrift.client;

import java.util.List;

import org.apache.thrift.TServiceClient;

import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;


/**
 * @author  xiaobenhai
 */

public interface ThriftClient {

	public <X extends TServiceClient> X iface(Class<X> ifaceClass);

	public List<ThriftServerInfo> getAvaliableServers();

	public void close();
}
