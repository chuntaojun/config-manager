package com.lessspring.org.watch;

import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.LifeCycle;
import com.lessspring.org.model.dto.ConfigInfo;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 13:12
 */
public interface WatchWorker extends LifeCycle {

    /**
     * 将配置管理{@link CacheConfigManager}注入{@link WatchWorker}
     *
     * @param configManager
     */
    void setConfigManager(CacheConfigManager configManager);

    /**
     * 通知 {@link com.lessspring.org.AbstractListener} 发生了配置变更
     *
     * @param configInfo
     */
    void updateAndNotify(ConfigInfo configInfo);

    /**
     * 当添加了新配置的监听者时触发此函数，重新构建{@link WatchWorker}
     */
    void onChange();

    /**
     * 创建配置监听者
     */
    void createWatcher();

}
