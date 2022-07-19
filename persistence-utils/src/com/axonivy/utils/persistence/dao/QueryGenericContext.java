package com.axonivy.utils.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Query Generic context for serializable type T
 *
 * @param <T> entity
 */
public abstract class QueryGenericContext<T extends  Serializable> implements AutoCloseable {

	/**
	 * CriteriaBuilder for whole context
	 */
	public final CriteriaBuilder c;
	
	/**
	 * Root object T with which the query is started
	 */
	public final Root<T> r;
	
	QueryGenericContext(CriteriaBuilder c, Root<T> r) {
		this.c = c;
		this.r = r;
	}

	/**
	 * Maximum parameters for an IN clause 
	 */
	protected static final int MAX_IN_PARAMETER = 999;
	
	/**
	 * Return a {@link Predicate} which evaluates to false.
	 *
	 * This is done by using {@link CriteriaBuilder#or(Predicate...)}, see there.
	 *
	 * @return predicate
	 */
	public Predicate alwaysFalse() {
		return c.or();
	}

	/**
	 * Return a {@link Predicate} which evaluates to true.
	 *
	 * This is done by using {@link CriteriaBuilder#and(Predicate...)}, see there.
	 *
	 * @return predicate
	 */
	public Predicate alwaysTrue() {
		return c.and();
	}

	/**
	 * key = value condition for enums
	 * @param key represents persistent single-valued properties or fields
	 * @param value object
	 * @param <E> the type of the represented object
	 * @return equal predicate for enum with value
	 */
	public <E extends Enum<E>> Predicate eq(SingularAttribute<? super T,E> key, E value){
		return this.c.equal(this.r.get(key),value);
	}

	/**
	 * key value condition for enums
	 * @param key represents persistent single-valued properties or fields
	 * @param value object
	 * @param <E> the type of the represented object
	 * @return not equal predicate for enum with value
	 */
	public <E extends Enum<E>> Predicate notEq(SingularAttribute<? super T,E> key, E value){
		return this.c.notEqual(this.r.get(key),value);

	}

	/**
	 * key = value condition
	 * @param key represents persistent single-valued properties or fields
	 * @param value string
	 * @return equal predicate for String field
	 */
	public Predicate eq(SingularAttribute<? super T,String> key, String value){
		return this.c.equal(this.r.get(key),value);
	}
	
	/**
	 * key like  value condition
	 * @param key represents persistent single-valued properties or fields
	 * @param value string
	 * @return like predicate for string Field
	 */
	public Predicate like(SingularAttribute<? super T,String> key, String value){
		return this.c.like(this.r.get(key),"%"+value+"%");
	}

	/**
	 * key like value condition
	 * @param key string expression
	 * @param value string
	 * @return like predicate for String expression
	 */
	public Predicate like(Expression<String> key, String value){
		return this.c.like(key,value);
	}

	/**
	 * Shorthand for get singular Attribute
	 * @param singularAttribute single-valued attribute
	 * @param <Y> the type of the represented object
	 * @return specified attribute from root object
	 */
	public <Y> Path<Y> get(SingularAttribute<? super T, Y> singularAttribute){
		return this.r.get(singularAttribute);
	}

	/**
	 * Shorthand for null check
	 * @param key single-valued attribute
	 * @return null attribute from root
	 */
	public Predicate isNull(SingularAttribute<? super T,?> key){
		return this.r.get(key).isNull();
	}
	/**
	 * Shorthand for null check
	 * @param key single-valued attribute
	 * @return not null field from root
	 */
	public Predicate isNotNull(SingularAttribute<? super T,?> key){
		return this.r.get(key).isNotNull();
	}

	/**
	 * Unwrap the query string from hibernate, its not the sql but HQL query string
	 * 
	 * @param query interface used to control query execution
	 * @return HQL string
	 */
	public String getQueryString(Query query) {
		return query.unwrap(org.hibernate.Query.class).getQueryString();
	}
	
	/**
	 * In criteria, if values size greater than MAX_IN_PARAMETER, it will be split into OR of many IN clauses. 
	 * 
	 * @param key expression
	 * @param values a collection
	 * @return IN predicate or OR predicate
	 */
	public Predicate in(Expression<?> key, Set<?> values){
		List<Predicate> predicates = new ArrayList<>();

		Set<Object> set = new HashSet<>();
		int count = 0;

		for (Object value : values) {
			set.add(value);
			count++;

			if (count % MAX_IN_PARAMETER == 0 || count == values.size()) {
				predicates.add(key.in(set));
				set = new HashSet<>();
			}
		}

		if (predicates.size() > 1) {
			return c.or(predicates.toArray(new Predicate[predicates.size()]));
		}

		return predicates.get(0);
	}

	/**
	 * Close method which is called at end of try with resources section of this autocloseable.
	 * It Should end the session, or transaction, etc...
	 */
	@Override
	public abstract void close();
	
}
