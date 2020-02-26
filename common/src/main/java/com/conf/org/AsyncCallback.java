package com.conf.org;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019/12/17 9:28 下午
 */
public interface AsyncCallback {

    /**
     * on success call back
     */
    default void onSuccess() {

    }

    /**
     * on fail call back
     */
    default void onFail() {

    }

    AsyncCallback DEFAULT_ASYNC_CALLBACK = new AsyncCallback() {};

}
