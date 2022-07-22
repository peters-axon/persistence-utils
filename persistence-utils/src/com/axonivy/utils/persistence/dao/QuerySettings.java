package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Order;
import javax.persistence.metamodel.SingularAttribute;

import com.axonivy.utils.persistence.dao.markers.QueryMarker;

/**
 * Various settings which should be passed to a JPA query.
 *
 * @param <T> entity
 */
public class QuerySettings<T extends Serializable> {

	protected Integer firstResult;
	protected Integer maxResults;
	protected Map<String, QueryMarker> markers = new HashMap<>();
	protected List<Order> orders = new ArrayList<>();
	protected List<SingularAttribute<? super T, ?>> orderAttributes = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public QuerySettings() {
		super();
	}

	/**
	 * Alternative to create a {@link QuerySettings} object.
	 *
	 * @return object
	 * @param <T> the type of the represented object
	 */
	public static <T extends Serializable> QuerySettings<T> create() {
		return new QuerySettings<>();
	}

	/**
	 * Skip results and start with this one starting from 0.
	 *
	 * @param firstResult integer
	 * @return object
	 */
	public QuerySettings<T> withFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
		return this;
	}

	/**
	 * maximum number of results to return.
	 *
	 * @param maxResults integer
	 * @return object
	 */
	public QuerySettings<T> withMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * Add {@link QueryMarker}s to the query.
	 *
	 * @param markers markers
	 * @return object
	 */
	public QuerySettings<T> withMarkers(QueryMarker...markers) {

		for (QueryMarker marker : markers) {
			if(marker != null) {
				this.markers.put(marker.getClass().getCanonicalName(), marker);
			}
		}

		return this;
	}

	/**
	 * Add {@link Order}s to the query.
	 *
	 * Note, that {@link Order}s have precedence before attributes defined with {@link #withOrderAttributes(SingularAttribute...)}.
	 * 
	 * @param orders an object that defines an ordering over the query results
	 * @return object
	 */
	public QuerySettings<T> withOrders(Order...orders) {
		for (Order order : orders) {
			this.orders.add(order);
		}

		return this;
	}

	/**
	 * Add order attributes to the query.
	 *
	 * Sorting is done ascending. Attributes defined with this function will come AFTER {@link Order}s
	 * defined with {@link #withOrders(Order...)}! Order attributes can only be used for the class or
	 * super classes of the class defined for {@link QuerySettings}.
	 *
	 * @param orderAttributes represents persistent single-valued properties or fields
	 * @return object
	 */
	@SafeVarargs
	public final QuerySettings<T> withOrderAttributes(SingularAttribute<? super T, ?>...orderAttributes) {
		for (SingularAttribute<? super T, ?> orderAttribute : orderAttributes) {
			this.orderAttributes.add(orderAttribute);
		}

		return this;
	}

	/**
	 * Get the number of the first result to return.
	 *
	 * @return the firstResult
	 */
	public Integer getFirstResult() {
		return firstResult;
	}

	/**
	 * Get the number of results to return.
	 *
	 * @return the maxResults
	 */
	public Integer getMaxResults() {
		return maxResults;
	}

	/**
	 * Get the map of {@link QueryMarker}s.
	 *
	 * @return markers
	 */
	public Map<String, QueryMarker> getMarkers() {
		return markers;
	}

	/**
	 * Get a specific {@link QueryMarker}.
	 *
	 * @param markerClass class
	 * @param <M> the type of the represented object
	 * @return markers
	 */
	@SuppressWarnings("unchecked")
	public <M extends QueryMarker> M getMarker(Class<M> markerClass) {
		return (M)markers.get(markerClass.getCanonicalName());
	}

	/**
	 * Get a specific {@link QueryMarker} or a default.
	 *
	 * @param markerClass class
	 * @param def default value
	 * @param <M> the type of the represented object
	 * @return markers
	 */
	@SuppressWarnings("unchecked")
	public <M extends QueryMarker> M getMarker(Class<M> markerClass, M def) {
		M result = (M) markers.get(markerClass.getCanonicalName());
		return result != null ? result : def;
	}


	/**
	 * Get the {@link Order}s.
	 *
	 * @return the orders
	 */
	public List<Order> getOrders() {
		return orders;
	}

	/**
	 * Get the order attributes.
	 *
	 * @return the orderAttributes
	 */
	public List<SingularAttribute<? super T, ?>> getOrderAttributes() {
		return orderAttributes;
	}
}