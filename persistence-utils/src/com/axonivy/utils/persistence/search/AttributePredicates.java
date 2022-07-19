package com.axonivy.utils.persistence.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

/**
 * Predicates, selections and templates for ordering.
 *
 * These are typically created by handling a {@link FilterPredicate}. A single
 * {@link FilterPredicate} might produce multiple {@link Predicate}s and
 * {@link Selection}s.
 *
 * Additionally order objects are created which <i>can</i> but need not be used
 * for ordering. The order objects will have a standard ascending order and, if
 * used, must be converted to the order wanted.
 *
 * Note: if a filter generates multiple selections for a single search enum,
 * it is assumed, that the same order will be applied to every selection.
 */
public class AttributePredicates {

	List<Predicate> predicates = new ArrayList<>();
	List<Selection<?>> selections = new ArrayList<>();
	List<Order> orders = new ArrayList<>();
	private boolean noValue;

	/**
	 * Add everything from another {@link AttributePredicates} object.
	 *
	 * @param attributePredicates object
	 */
	public void add(AttributePredicates attributePredicates) {
		predicates.addAll(attributePredicates.predicates);
		selections.addAll(attributePredicates.selections);
		orders.addAll(attributePredicates.orders);
	}

	/**
	 * Add a {@link List} or {@link Predicate}s.
	 *
	 * @param predicates a list
	 */
	public void addPredicates(List<Predicate> predicates) {
		this.predicates.addAll(predicates);
	}

	/**
	 * Add a {@link List} of {@link Selection}s.
	 * @param selections a list defines items that is to be
	 * returned in a query result.
	 */
	public void addSelections(List<Selection<?>> selections) {
		this.selections.addAll(selections);
	}

	/**
	 * Add a {@link List} of {@link Order}s.
	 * @param orders a list defines ordering over the query results
	 */
	public void addOrders(List<Order> orders) {
		this.orders.addAll(orders);
	}

	/**
	 * Add a single {@link Predicate}.
	 *
	 * @param predicate the type of a simple or compound predicate
	 * @return boolean
	 */
	public boolean addPredicate(Predicate predicate) {
		return predicates.add(predicate);
	}

	/**
	 * Add a single {@link Selection}.
	 *
	 * @param selection interface defines an item that is to be
	 * returned in a query result.
	 * @return boolean
	 */
	public boolean addSelection(Selection<? extends Object> selection) {
		return selections.add(selection);
	}

	/**
	 * Add a single {@link Order}.
	 *
	 * @param order an object that defines an ordering over the query results
	 * @return boolean
	 */
	public boolean addOrder(Order order) {
		return orders.add(order);
	}

	/**
	 * Get the array of {@link Predicate}s usable in a {@link CriteriaQuery}.
	 *
	 * @return the predicates array
	 */
	public Predicate[] getPredicatesArray() {
		return predicates.toArray(new Predicate[0]);
	}

	/**
	 * Get {@link List} of {@link Predicate}s.
	 *
	 * @return the predicates
	 */
	public List<Predicate> getPredicates() {
		return Collections.unmodifiableList(predicates);
	}

	/**
	 * Get the array of {@link Selection}s usable for a {@link CriteriaQuery}.
	 *
	 * @return the selections array
	 */
	@SuppressWarnings("unchecked")
	public Selection<Object>[] getSelectionsArray() {
		return selections.toArray(new Selection[0]);
	}

	/**
	 * Get {@link List} of {@link Selection}s.
	 *
	 * @return the selections
	 */
	public List<Selection<?>> getSelections() {
		return Collections.unmodifiableList(selections);
	}

	/**
	 * Get the array of {@link Order}s usable for a {@link CriteriaQuery}.
	 *
	 * @return the orders array
	 */
	public Order[] getOrdersArray() {
		return orders.toArray(new Order[0]);
	}

	/**
	 * Get {@link List} of {@link Order}s.
	 *
	 * @return the orders
	 */
	public List<Order> getOrders() {
		return Collections.unmodifiableList(orders);
	}

	/**
	 * Quick check, whether there are any {@link Predicate}s or {@link Selection}s.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return predicates.isEmpty() && selections.isEmpty() && orders.isEmpty() && !isNoValue() ;
	}

	/**
	 * Sets the no value.
	 *
	 * @param bool the new no value
	 */
	public void setNoValue(boolean bool) {
		noValue = bool;

	}

	/**
	 * Mark if an empty value is set.
	 *
	 * @return the noValue
	 */
	public boolean isNoValue() {
		return noValue;
	}
}

