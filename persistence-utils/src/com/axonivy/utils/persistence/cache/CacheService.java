package com.axonivy.utils.persistence.cache;

import java.util.HashMap;
import java.util.Map;

import com.axonivy.utils.persistence.logging.Logger;

/**
 * Maintain caches and allow for mass operations.
 */
public class CacheService {

	private static final Logger LOG = Logger.getLogger(CacheService.class);

	private static CacheService singleton = null;

	protected Map<String, AbstractCache> longTermCacheMap = new HashMap<>();

	/**
	 * Hide public constructor.
	 */
	protected CacheService() {};

	/**
	 * Get singleton instance.
	 *
	 * @return singleton instance
	 */
	public static synchronized CacheService getInstance() {
		if(singleton == null) {
			singleton = new CacheService();
		}

		return singleton;
	}

	/**
	 * Register a "long term" cache.
	 *
	 * Long term caches are refreshed by a function call.
	 *
	 * @param cache object
	 * @param <T> the type of the represented object
	 * @return cache
	 */
	public <T extends AbstractCache> T registerLongTermCache(T cache) {
		LOG.info("register long term cache of type " + cache.getClass());
		longTermCacheMap.put(cache.getClass().getCanonicalName(), cache);

		return cache;
	}

	/**
	 * Refresh all long term caches.
	 *
	 * Make sure, that the correct persistence unit is available
	 * when calling this function, especially when calling via a
	 * process!
	 */
	public void refreshAllLongTermCaches() {
		LOG.info("refreshing all long term caches");
		longTermCacheMap.entrySet().stream().forEach(entry -> {
			LOG.info("refresh long term cache: {0}", entry.getKey());
			entry.getValue().refresh();
		});
	}

	/**
	 * Invalidate all long term caches.
	 */
	public void invalidateAllLongTermCaches() {
		LOG.info("invalidating all long term caches");
		longTermCacheMap.entrySet().stream().forEach(entry -> {
			LOG.info("invalidating long term cache: {0}", entry.getKey());
			entry.getValue().invalidate();
		});
	}
}
