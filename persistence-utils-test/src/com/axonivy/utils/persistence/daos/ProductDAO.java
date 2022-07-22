package com.axonivy.utils.persistence.daos;

import javax.persistence.criteria.Expression;

import com.axonivy.utils.persistence.dao.AuditableDAO;
import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.dao.ExpressionMap;
import com.axonivy.utils.persistence.entities.Product;
import com.axonivy.utils.persistence.entities.Product_;
import com.axonivy.utils.persistence.enums.ProductSearchField;
import com.axonivy.utils.persistence.logging.Logger;
import com.axonivy.utils.persistence.search.AttributePredicates;
import com.axonivy.utils.persistence.search.FilterPredicate;

public class ProductDAO extends AuditableDAO<Product_, Product> implements BaseDAO {

	private static final Logger LOG = Logger.getLogger(ProductDAO.class);

	@Override
	protected Class<Product> getType() {
		return Product.class;
	}

	@Override
	protected AttributePredicates getAttributePredicate(CriteriaQueryGenericContext<Product, ?> query, FilterPredicate filterPredicate, ExpressionMap expressionMap) {

		AttributePredicates result = new AttributePredicates();

		Enum<?> searchFilter = filterPredicate.getSearchFilter();

		if (searchFilter instanceof ProductSearchField) {

			ProductSearchField ivyPersistanceSearchField = (ProductSearchField) searchFilter;

			switch (ivyPersistanceSearchField) {

			case FILTER_NAME_PRODUCT:
				Expression<String> expression = getExpression(expressionMap, query.r, Product_.name);
				addSelectionOrderAndLike(query, filterPredicate, result, expression);
				break;
			default:
				break;
			}
		}
		if (result.isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid filter '" + searchFilter + "' or not yet implemented: " + filterPredicate);
		}

		return result;
	}
}
