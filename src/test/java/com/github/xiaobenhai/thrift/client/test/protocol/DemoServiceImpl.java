package com.github.xiaobenhai.thrift.client.test.protocol;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  xiaobenhai
 */
public class DemoServiceImpl implements DemoService.Iface {
    private static final Logger LOG = LoggerFactory.getLogger(DemoServiceImpl.class);
    @Override
    public String test(String value) throws org.apache.thrift.TException{
        LOG.info("[test] is doing,value:{}"+value);
        try {
            Thread.sleep(1000L);
        }catch (Exception e){
        }
        return value;
    }
    @Override
    public long ping() throws TException {
        LOG.info("ping-thrift...");
        return System.currentTimeMillis();
    }

}
