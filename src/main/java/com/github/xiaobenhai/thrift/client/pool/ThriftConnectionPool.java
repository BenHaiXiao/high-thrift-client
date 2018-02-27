package com.github.xiaobenhai.thrift.client.pool;

import org.apache.thrift.transport.TTransport;
/**
 * @author  xiaobenhai
 */
public interface ThriftConnectionPool {

    TTransport getConnection(ThriftServerInfo thriftServerInfo);

    void returnConnection(ThriftServerInfo thriftServerInfo, TTransport transport);

    void returnBrokenConnection(ThriftServerInfo thriftServerInfo, TTransport transport);

	void close();

    void clear(ThriftServerInfo thriftServerInfo);

}
