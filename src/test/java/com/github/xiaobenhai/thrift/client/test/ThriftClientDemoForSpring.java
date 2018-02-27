package com.github.xiaobenhai.thrift.client.test;

import com.github.xiaobenhai.thrift.client.test.thrift.ThriftSdk;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author  xiaobenhai
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/application-context-client.xml")
public class ThriftClientDemoForSpring {
    @Autowired
    private ThriftSdk thriftSdk;
    @Test
    public void test()throws  Exception{
        String value = "ioioiodfdfdfd";
        Assert.assertEquals(value,thriftSdk.test(value));
    }

    @Test
    public void test1()throws  Exception{
        String value = "ioioiodfdfdfd";
        while (true){
            Thread.sleep(100L);
            System.out.println(thriftSdk.test(value));
        }
    }
}
