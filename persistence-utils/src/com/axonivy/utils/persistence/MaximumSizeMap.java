package com.axonivy.utils.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link Map} with a maximum size.
 *
 * If more elements are inserted, then the oldest are removed.
 *
 * The default size is 100.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MaximumSizeMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	int capacity = 100;
	int index = 0;

	/**
	 * Constructor.
	 *
	 * @param capacity maximum size of map
	 */
	public MaximumSizeMap(int capacity) {
		super(capacity); // preallocate capacity
		this.capacity = capacity;
	}

	/* (non-Javadoc)
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > capacity;
	}
}
