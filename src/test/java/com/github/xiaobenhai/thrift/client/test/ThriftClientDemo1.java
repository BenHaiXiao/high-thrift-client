package com.github.xiaobenhai.thrift.client.test;

import com.github.xiaobenhai.thrift.client.*;
import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.pool.ThriftServerInfo;
import com.github.xiaobenhai.thrift.client.registry.Provider;
import com.github.xiaobenhai.thrift.client.registry.local.LocalConfigProvider;
import com.github.xiaobenhai.thrift.client.test.protocol.DemoService;
import com.github.xiaobenhai.thrift.client.test.thrift.ThriftServer;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author  xiaobenhai
 */
public class ThriftClientDemo1 {
	private static final Logger LOG = LoggerFactory.getLogger(ThriftClientDemo1.class);
	private  ThriftClient randomThriftClient;
	private  ThriftClient defaultThriftClient;
	private  ThriftClient hashThriftClient;
	private  ThriftClient weightThriftClient;
     @Before
	public  void before() throws Exception {
		 ConnectionValidator connectionValidator = new ConnectionValidator() {
			 @Override
			 public boolean isValid(TTransport object) {
				 return object.isOpen();
			 }
		 };

		 GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
		 String backupServers = "127.0.0.1:14465,127.0.0.1:14465";
		 Provider<ThriftServerInfo> registry = new LocalConfigProvider("127.0.0.1:14465,127.0.0.1:14465");
		 randomThriftClient = new RandomThriftClient(registry, connectionValidator, poolConfig,backupServers);
		 defaultThriftClient = new DefaultThriftClient(registry, connectionValidator, poolConfig,backupServers);
		 hashThriftClient = new HashThriftClient(registry, connectionValidator, poolConfig,backupServers);
		 weightThriftClient =  new WeightThriftClient(registry, connectionValidator, poolConfig,backupServers);
     }
     @Test
	public  void randomThriftClient(  ) throws Exception {
		 doAction(randomThriftClient);
	}
	@Test
	public  void defaultThriftClient(  ) throws Exception {
		doAction(defaultThriftClient);
	}
	@Test
	public  void hashThriftClient(  ) throws Exception {
		doAction(hashThriftClient);
	}
	@Test
	public  void weightThriftClient(  ) throws Exception {
		doAction(weightThriftClient);
	}

	public  void doAction(final ThriftClient  thriftClient) throws Exception {

		//调用thrift-client
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				//打印从thriftClient获取到的可用服务列表
				List<ThriftServerInfo> servers = weightThriftClient.getAvaliableServers();
				LOG.info("available servers:{}", Arrays.toString(servers.toArray()));
				//测试服务是否可用
				for (int i = 0; i < 10000; i++) {
					try {
						TimeUnit.SECONDS.sleep(1);
						DemoService.Client client = thriftClient.iface(DemoService.Client.class);
						String result = client.test("I am Ok");
						System.out.println("result is "+ result);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}

			}
		}, 0, 1, TimeUnit.MINUTES);
		
		//启动thrift服务
		Executors.newSingleThreadExecutor().execute(new ThriftServer());
	   System.in.read();
     }


}
