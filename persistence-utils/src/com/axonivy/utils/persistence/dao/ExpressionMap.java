package com.axonivy.utils.persistence.dao;

import java.util.HashMap;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;

/**
 * Class used to store all {@link Join}s of a query.
 *
 * This helps generic functions to avoid joining tables multiple times if
 * many fields of a {@link Join} destination are accessed.
 */
public class ExpressionMap extends HashMap<String, Expression<?>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Private to avoid calling new() by mistake.
	 */
	private ExpressionMap() {
	}

	/**
	 * Trap to avoid calling new() by mistake.
	 *
	 * Most of the time there should only be a single {@link ExpressionMap}
	 * for a whole query ({@link CriteriaQueryGenericContext}) and it should
	 * be fetched by calling {@link CriteriaQueryGenericContext#getCurrentExpressionMap()}.
	 *
	 * Note, that {@link ExpressionMap}s can also be used for sub-queries. Since
	 * the sub-queries start with a new root object, they will not interfere with
	 * the outer query in the map.
	 *
	 * Nevertheless, if you really need an additional {@link ExpressionMap},
	 * here you are!
	 *
	 * @return expression map
	 */
	public static ExpressionMap createNewExpressionMap() {
		return new ExpressionMap();
	}
}
