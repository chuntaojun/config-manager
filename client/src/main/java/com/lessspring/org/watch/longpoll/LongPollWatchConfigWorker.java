package com.lessspring.org.watch.longpoll;

import com.lessspring.org.CacheConfigManager;
import com.lessspring.org.Configuration;
import com.lessspring.org.filter.ConfigFilterManager;
import com.lessspring.org.http.HttpClient;
import com.lessspring.org.watch.AbstractWatchWorker;

/**
 * @author <a href="mailto:liaochuntao@youzan.com">liaochuntao</a>
 * @since 0.0.1
 * @Created at 2019-12-01 13:21
 */
public class LongPollWatchConfigWorker extends AbstractWatchWorker {

	public LongPollWatchConfigWorker(HttpClient httpClient, Configuration configuration,
			ConfigFilterManager configFilterManager) {
		super(httpClient, configuration, configFilterManager);
	}

	@Override
	public void setConfigManager(CacheConfigManager configManager) {
	    super.setConfigManager(configManager);
	}

    @Override
	public void onChange() {

	}

	@Override
	public void createWatcher() {

	}

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

	private static class SubWorker implements Runnable {

        /**
         *
         */
	    private final int index;

        public SubWorker(int index) {
            this.index = index;
        }

        @Override
        public void run() {

        }
    }

}
