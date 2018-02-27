package com.github.xiaobenhai.thrift.client.registry.local;

import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import com.github.xiaobenhai.thrift.client.registry.Provider;
import com.github.xiaobenhai.thrift.client.registry.ProviderListener;

import java.util.Collection;
import java.util.List;

/**
 * @author  xiaobenhai
 *
 * 支持读本地配置文件
 */
public class LocalConfigProvider implements Provider<ThriftServerInfo> {

	private List<ThriftServerInfo> serverInfoList;

	public LocalConfigProvider(String connStr) {
		serverInfoList = ThriftServerInfo.ofs(connStr);
	}

	@Override
	public Collection<ThriftServerInfo> list() {
		return serverInfoList;
	}

	@Override
	public void addListener(ProviderListener<ThriftServerInfo> listener) {

	}
}
