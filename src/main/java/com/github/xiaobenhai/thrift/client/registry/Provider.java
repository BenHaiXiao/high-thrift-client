package com.github.xiaobenhai.thrift.client.registry;

import java.util.Collection;

/**
 * @author  xiaobenhai
 */
public interface Provider<T> {

    Collection<T> list();

    void addListener(ProviderListener<T> listener);

}
