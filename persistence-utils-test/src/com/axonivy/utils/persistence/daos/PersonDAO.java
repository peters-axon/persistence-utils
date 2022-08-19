package com.axonivy.utils.persistence.daos;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.axonivy.utils.persistence.Logger;
import com.axonivy.utils.persistence.dao.AuditableDAO;
import com.axonivy.utils.persistence.dao.CriteriaQueryContext;
import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.dao.ExpressionMap;
import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.daos.markers.AccessMarker;
import com.axonivy.utils.persistence.entities.Department_;
import com.axonivy.utils.persistence.entities.Person;
import com.axonivy.utils.persistence.entities.Person_;
import com.axonivy.utils.persistence.enums.AccessRestriction;
import com.axonivy.utils.persistence.enums.MaritalStatus;
import com.axonivy.utils.persistence.enums.PersonSearchField;
import com.axonivy.utils.persistence.search.AttributePredicates;
import com.axonivy.utils.persistence.search.FilterPredicate;
import com.axonivy.utils.persistence.service.IvyService;


public class PersonDAO extends AuditableDAO<Person_, Person> implements BaseDAO {
	private static final Logger LOG = Logger.getLogger(PersonDAO.class);
	private static final PersonDAO instance = new PersonDAO();

	private PersonDAO() {
	}

	public static PersonDAO getInstance() {
		return instance;
	}

	@Override
	protected Class<Person> getType() {
		return Person.class;
	}

	/**
	 * Find a user by it's Ivy user name.
	 *
	 * @param ivyUserName
	 * @param settings
	 * @return
	 */
	public Person findByIvyUserName(String ivyUserName, QuerySettings<Person> settings) {
		Person result = null;
		try (CriteriaQueryContext<Person> ctx = initializeQuery()) {
			Expression<String> nameExpr = getExpression(null, ctx.r, Person_.ivyUserName);
			ctx.whereEq(nameExpr, ivyUserName);
			if(settings != null) {
				ctx.setQuerySettings(settings);
			}
			result = forceSingleResult(findByCriteria(ctx));
		}
		return result;
	}

	@Override
	protected <U> void manipulateCriteriaFactory(CriteriaQueryGenericContext<Person, U> ctx) {
		super.manipulateCriteriaFactory(ctx);

		AccessMarker accessMarker = ctx.getQuerySettings().getMarker(AccessMarker.class);

		if(accessMarker == null) {
			accessMarker = AccessMarker.AUTHORIZED;
		}

		if(accessMarker.getAccessRestriction() == AccessRestriction.ALL || IvyService.isSystemUser()) {
			// unrestricted
		}
		else {
			ctx.where(getPersonRestrictionPredicate(ctx));
		}
	}

	@SuppressWarnings("unchecked")
	public static Predicate getPersonRestrictionPredicate(CriteriaQueryGenericContext<?, ?> ctx) {
		Root<?> root = ctx.getCurrentRoot();

		String userName = IvyService.getSessionUserName();

		ExpressionMap exmap = ctx.getCurrentExpressionMap();

		Expression<String> dpmntIdExpression = (Expression<String>) getExpressionGeneral(exmap, root, Person_.department, Department_.id);


		Subquery<String> dpmntQuery = ctx.q.subquery(String.class);
		Root<Person> root2 = dpmntQuery.from(Person.class);
		ctx.setTemporaryRoot(root2);
		ctx.setTemporaryExpressionMap(ExpressionMap.createNewExpressionMap());
		Expression<String> ivyUserNameExpression = getExpression(ctx.getCurrentExpressionMap(), root2, Person_.ivyUserName);
		dpmntQuery.where(ctx.c.equal(ivyUserNameExpression, userName));
		Expression<String> dpmntId = getExpression(ctx.getCurrentExpressionMap(), root2, Person_.department, Department_.id);
		dpmntQuery.select(dpmntId);

		ctx.resetTemporaryExpressionMap();
		ctx.resetTemporaryRoot();

		Predicate personPred = ctx.c.equal(dpmntIdExpression, dpmntQuery);
		return personPred;
	}

	@Override
	protected AttributePredicates getAttributePredicate(CriteriaQueryGenericContext<Person, ?> query, FilterPredicate filterPredicate, ExpressionMap expressionMap) {
		AttributePredicates attributePredicate = new AttributePredicates();

		Enum<?> searchFilter = filterPredicate.getSearchFilter();

		if(searchFilter instanceof PersonSearchField) {
			switch((PersonSearchField)searchFilter) {
			case BIRTHDATE:
				Expression<Date> birthdateExpresssion = getExpression(expressionMap, query.r, Person_.birthdate);
				attributePredicate.addSelection(birthdateExpresssion);
				attributePredicate.addOrder(query.c.asc(birthdateExpresssion));
				String searchDateString = filterPredicate.getValue();
				if (searchDateString != null) {
					Date searchDate = new Date(Long.parseLong(searchDateString));
					attributePredicate.addPredicate(query.c.equal(birthdateExpresssion, searchDate));
				}
				break;
			case DEPARTMENT_NAME:
				addSelectionOrderAndLike(query,
						filterPredicate,
						attributePredicate,
						getExpression(expressionMap, query.r, Person_.department, Department_.name));
				break;
			case FIRST_NAME:
				addSelectionOrderAndLike(query,
						filterPredicate,
						attributePredicate,
						getExpression(expressionMap, query.r, Person_.firstName));
				break;
			case ID:
				addSelectionOrderAndLike(query,
						filterPredicate,
						attributePredicate,
						getExpression(expressionMap, query.r, Person_.id));
				break;
			case IVY_USER_NAME:
				addSelectionOrderAndLike(query,
						filterPredicate,
						attributePredicate,
						getExpression(expressionMap, query.r, Person_.ivyUserName));
				break;
			case LAST_NAME:
				addSelectionOrderAndLike(query,
						filterPredicate,
						attributePredicate,
						getExpression(expressionMap, query.r, Person_.lastName));
				break;
			case MARITAL_STATUS:
				Expression<MaritalStatus> expression = getExpression(expressionMap, query.r, Person_.maritalStatus);
				
				attributePredicate.addSelection(expression);
				attributePredicate.addOrder(query.c.asc(expression));
				String name = filterPredicate.getValue();
				if (name != null) {
					MaritalStatus maritalStatus = MaritalStatus.valueOf(name);
					attributePredicate.addPredicate(query.c.equal(expression, maritalStatus));
				}
				break;
			case SALARY:
				Expression<BigDecimal> salaryExpresssion = getExpression(expressionMap, query.r, Person_.salary);
				attributePredicate.addSelection(salaryExpresssion);
				attributePredicate.addOrder(query.c.asc(salaryExpresssion));
				String minSalaryString = filterPredicate.getValue();
				if (minSalaryString != null) {
					try {
						BigDecimal minSalary = new BigDecimal(minSalaryString);
						attributePredicate.addPredicate(query.c.ge(salaryExpresssion, minSalary));
					} catch (NumberFormatException e) {
						LOG.error("Could not convert value ''{0}'' to a number to search for minimal salary.", minSalaryString);
					}
				}
				break;
			case SYNC_TO_IVY:
				Expression<Boolean> syncToIvyExpresssion = getExpression(expressionMap, query.r, Person_.syncToIvy);
				attributePredicate.addSelection(syncToIvyExpresssion);
				attributePredicate.addOrder(query.c.asc(syncToIvyExpresssion));
				String syncToIvyString = filterPredicate.getValue();
				if (syncToIvyString != null) {
					if(Boolean.valueOf(syncToIvyString)) {
						attributePredicate.addPredicate(query.c.isTrue(syncToIvyExpresssion));
					} else {
						attributePredicate.addPredicate(query.c.isFalse(syncToIvyExpresssion));
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Person search does not support search filter '" +  searchFilter + "'");
			}
		}
		return attributePredicate;
	}
}
