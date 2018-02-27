package com.github.xiaobenhai.thrift.client.registry;


/**
 * @author  xiaobenhai
 */
public interface ProviderListener<T> {
    /**
     * 仅仅通知变化事件，不关心具体变化 ,
     * 用于动态修改可用服务
     * 如某个服务宕机，动态从当前服务列表中去除
     */
    void onFresh();

}
