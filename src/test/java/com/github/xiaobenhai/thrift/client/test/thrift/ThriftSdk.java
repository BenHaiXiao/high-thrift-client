package com.github.xiaobenhai.thrift.client.test.thrift;

import com.github.xiaobenhai.thrift.client.ThriftClient;
import com.github.xiaobenhai.thrift.client.config.CommonThriftWrapper;
import com.github.xiaobenhai.thrift.client.test.protocol.DemoService;
import org.apache.thrift.TException;

/**
 * @author  xiaobenhai
 */
public class ThriftSdk {
    private ThriftClient thriftClient;

    public ThriftSdk(CommonThriftWrapper commonThriftWrapper) {
        if (commonThriftWrapper == null) {
            throw new IllegalArgumentException("commonThriftWrapper must not be null!");
        }
        thriftClient = commonThriftWrapper.getThriftClient();
    }

    public String test(String value)throws TException{
        DemoService.Iface iface =thriftClient.iface(DemoService.Client.class);
        String result = iface.test(value);;
        return  result;
    }
}
