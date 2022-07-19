package com.axonivy.utils.persistence.cache;

/**
 * Base class for simple caches.
 *
 * Simple caches are caches which can be validated or invalidated.
 * They operate eager, which means, they are loaded once and then
 * used for a certain time. They need to be invalidated or refreshed
 * from time to time (e.g. when data changes).
 */
public abstract class AbstractCache {

	private boolean valid = false;
	private long hits = 0L;

	/**
	 * Build the cache.
	 *
	 * This function will be called whenever the cache needs to be built.
	 * For example initially or after a refresh.
	 *
	 * Caches based on this class must implement this function.
	 *
	 * @return boolean
	 */
	public abstract boolean build();

	/**
	 * Check case is valid or not
	 * @return boolean
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Return number of cache hits since last build.
	 * @return long
	 */
	public long getHits() {
		return hits;
	}

	/**
	 * Validate the cache.
	 *
	 * If needed, then this cache is rebuilt.
	 *
	 * @return whether the cache is now valid.
	 */
	public boolean validate() {
		if (!valid) {
			valid = build();
			hits = 0;
		} else {
			hits++;
		}

		return valid;
	}

	/**
	 * Invalidate the cache.
	 */
	public void invalidate() {
		valid = false;
	}

	/**
	 * Refresh the cache.
	 */
	public void refresh() {
		invalidate();
		validate();
	}
}
