package com.github.xiaobenhai.thrift.client.test.sample;

import com.github.xiaobenhai.thrift.client.failover.ConnectionValidator;
import com.github.xiaobenhai.thrift.client.test.protocol.DemoService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  xiaobenhai
 */
public class ThriftConnectionValidator implements ConnectionValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftConnectionValidator.class);

    @Override
    public boolean isValid(TTransport transport) {
        try {
            TProtocol protocol = new TBinaryProtocol(transport);
            DemoService.Client client = new DemoService.Client(protocol);
            long time = client.ping();
            LOGGER.info("thrift service ping success : time = {}", time);
            return true;
        } catch (TException e) {
            LOGGER.warn("thrift service error", e);
        }
        return false;
    }
}
