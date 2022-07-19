package com.axonivy.utils.persistence.history.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.criteria.Expression;

import org.hibernate.criterion.Order;

import com.axonivy.utils.persistence.beans.GenericEntity;
import com.axonivy.utils.persistence.dao.CriteriaQueryContext;
import com.axonivy.utils.persistence.dao.GenericDAO;
import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.history.beans.History;
import com.axonivy.utils.persistence.history.beans.HistoryPK_;
import com.axonivy.utils.persistence.history.beans.History_;

/**
 * DAO for create history in History Table
 * 
 * @author hunghx
 *
 */
public class HistoryDAO extends GenericDAO<History_, History> {

	private String persistenceUnitName;

	private static final HistoryDAO instance = new HistoryDAO();

	private HistoryDAO() {
	}

	public static HistoryDAO getInstance() {
		return instance;
	}

	public HistoryDAO(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	@Override
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public List<History> findByTypeAndId(Class<?> type, String id) {
		return findByTypeAndId(type, id, null);
	}
	
	/**
	 * Finds history entities based on the passed type and id.
	 * 
	 * Default sorting on timestamp DESC
	 * 
	 * @param type
	 * @param id
	 * @param settings
	 * @return
	 */
	public List<History> findByTypeAndId(Class<?> type, String id, QuerySettings<History> settings) {
		List<History> result = null;
		try (CriteriaQueryContext<History> ctx = initializeQuery()) {
			Expression<String> idExpr = getExpression(null, ctx.r, History_.id, HistoryPK_.entityId);
			Expression<String> typeExpr = getExpression(null, ctx.r, History_.id, HistoryPK_.entityType);
			Expression<Timestamp> timeStampExpr = getExpression(null, ctx.r, History_.id, HistoryPK_.timestamp);
			ctx.q.where(ctx.c.and(ctx.c.equal(idExpr, id), ctx.c.equal(typeExpr, type.getCanonicalName())));
			ctx.q.orderBy(ctx.c.desc(timeStampExpr));
			if(settings != null) {
				ctx.setQuerySettings(settings);
			}
			result = findByCriteria(ctx);
		}
		return result;
	}
	
	@Override
	protected Class<History> getType() {
		return History.class;
	}
	
}