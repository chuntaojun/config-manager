package com.lessspring.org.watch;

import com.lessspring.org.AbstractListener;
import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.ClassLoaderSwitchUtils;
import com.lessspring.org.Configuration;
import com.lessspring.org.executor.NameThreadFactory;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.pojo.CacheItem;
import com.lessspring.org.utils.MD5Utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @Created at 2019-12-01 13:22
 */
public abstract class AbstractWatchWorker implements WatchWorker {

    protected CacheConfigManager configManager;
    protected final HttpClient httpClient;
    protected final Configuration configuration;
    protected final ConfigFilterManager configFilterManager;

    protected static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            new NameThreadFactory("com.lessspring.org.config-manager.client.watcher-"));

    public AbstractWatchWorker(HttpClient httpClient, Configuration configuration,
                               ConfigFilterManager configFilterManager) {
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.configFilterManager = configFilterManager;
    }

    @Override
    public void setConfigManager(CacheConfigManager configManager) {
        this.configManager = configManager;
        EXECUTOR.schedule(this::createWatcher, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void updateAndNotify(ConfigInfo configInfo) {
        final String groupId = configInfo.getGroupId();
        final String dataId = configInfo.getDataId();
        final String lastMd5 = MD5Utils.md5Hex(configInfo.toBytes());
        final CacheItem oldItem = configManager.getCacheItem(groupId, dataId);
        if (Objects.nonNull(oldItem) && oldItem.isChange(lastMd5)
                && oldItem.isNew(configInfo)) {
            oldItem.setLastMd5(lastMd5);
            notifyWatcher(configInfo);
        }
    }

    protected void notifyWatcher(ConfigInfo configInfo) {
        final String groupId = configInfo.getGroupId();
        final String dataId = configInfo.getDataId();
        Optional<List<AbstractListener>> listeners = Optional
                .ofNullable(configManager.getCacheItem(groupId, dataId).listListener());

        // do some processor to configInfo by filter chain
        configFilterManager.doFilter(configInfo);

        listeners.ifPresent(abstractListeners -> {
            long a = abstractListeners.stream().peek(listener -> {
                Runnable job = () -> {
                    // In order to make the spi mechanisms can work better
                    ClassLoaderSwitchUtils.transfer(listener);
                    listener.onReceive(configInfo);
                    ClassLoaderSwitchUtils.recover();
                };
                Executor userExecutor = listener.executor();
                if (Objects.isNull(userExecutor)) {
                    job.run();
                }
                else {
                    userExecutor.execute(job);
                }
            }).count();
            configManager.getCacheItem(groupId, dataId).setConfigInfo(configInfo);
        });
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        configManager = null;
    }

    @Override
    public boolean isInited() {
        return false;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
