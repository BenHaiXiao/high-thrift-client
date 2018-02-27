package com.github.xiaobenhai.thrift.client.test.thrift;

import com.github.xiaobenhai.thrift.client.test.protocol.DemoService;
import com.github.xiaobenhai.thrift.client.test.protocol.DemoServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.util.concurrent.Executors;

/**
 * @author  xiaobenhai
 */
public class ThriftServer implements Runnable{
	private static final Log LOGGER = LogFactory.getLog(ThriftServer.class);
	private final static int DEFAULT_PORT = 14465;
	private static TServer server = null;
	@Override
	public void run() {
		try {

			//start thrift server
			LOGGER.info("start thrift server");
			TNonblockingServerSocket socket = new TNonblockingServerSocket(DEFAULT_PORT);
			DemoService.Processor processor = new DemoService.Processor(new DemoServiceImpl());
			TNonblockingServer.Args arg = new TNonblockingServer.Args(socket);
			arg.protocolFactory(new TBinaryProtocol.Factory());
			arg.transportFactory(new TFramedTransport.Factory());
			arg.processorFactory(new TProcessorFactory(processor));
			server = new TNonblockingServer (arg);
			server.serve();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)throws Exception{
		Executors.newSingleThreadExecutor().execute(new ThriftServer());
		System.in.read();
	}
	
}
