package com.axonivy.utils.persistence.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

import com.axonivy.utils.persistence.beans.AuditableEntity;
import com.axonivy.utils.persistence.beans.AuditableEntity_;
import com.axonivy.utils.persistence.beans.Header_;
import com.axonivy.utils.persistence.dao.markers.AuditableMarker;
import com.axonivy.utils.persistence.enums.AuditableStatus;

import ch.ivyteam.ivy.environment.Ivy;

/**
 * Dao for auditable entities
 *
 * @param <MetaDataGeneric> meta model
 * @param <A> entity
 * 
 * @author peter
 */
public abstract class AuditableDAO<MetaDataGeneric extends AuditableEntity_, A extends AuditableEntity>
		extends GenericIdEntityDAO<MetaDataGeneric, A> {

	/**
	 * Status to use if none is specified.
	 */
	protected static AuditableStatus DEFAULT_AUDITABLE_STATUS = AuditableStatus.ACTIVE;

	@Override
	protected <U> void manipulateCriteriaFactory(CriteriaQueryGenericContext<A, U> context) {
		context.where(getAuditableRestriction(context));

		super.manipulateCriteriaFactory(context);
	}

	/**
	 * Manipulate the update query context. It set the modification date and user
	 * name.
	 * 
	 */
	@Override
	protected void manipulateUpdateQuery(UpdateQueryGenericContext<A> context) {
		context.u.set(context.r.get(AuditableEntity_.header).get(Header_.modifiedDate), new Date());
		context.u.set(context.r.get(AuditableEntity_.header).get(Header_.modifiedByUserName), getModifiedUserName()); 
		Predicate wherePredicate = context.u.getRestriction();
		Attribute<?, ?>[] attributes = new Attribute<?, ?>[0];

		Predicate notDeletePredicate = getExpressionGeneral(null, context.r,
				concat(attributes, AuditableEntity_.header, Header_.flaggedDeletedDate)).isNull();
		wherePredicate = context.c.and(wherePredicate, notDeletePredicate);
		context.u.where(wherePredicate);

		super.manipulateUpdateQuery(context);
	}

	
	/**
	 * Get modified username, could be override later.
	 * 
	 * @return Ivy session username
	 */
	protected String getModifiedUserName() {
		return Ivy.session().getSessionUserName();
	}

	private static AuditableStatus getAuditableStatus(CriteriaQueryGenericContext<?, ?> context) {
		AuditableStatus which = DEFAULT_AUDITABLE_STATUS;
		AuditableMarker marker = context.getQuerySettings().getMarker(AuditableMarker.class);
		if (null != marker) {
			which = marker.getWhich();
		}

		return which;
	}

	/**
	 * Get the predicate to handle {@link AuditableStatus} for bean.
	 *
	 * @param context   if the context root is already an {@link AuditableEntity}
	 *                  then there should not be any attributes.
	 * @param auditable if the context root is not an {@link AuditableEntity} then
	 *                  this is the path from the root to the auditable.
	 * @return predicate
	 */
	public static Predicate getAuditableRestriction(CriteriaQueryGenericContext<?, ?> context, Attribute<?, ?>... auditable) {
		Predicate pred = null;

		AuditableStatus which = getAuditableStatus(context);

		switch (which) {
		case ACTIVE:
			pred = getExpressionGeneral(context.getCurrentExpressionMap(), context.getCurrentRoot(),
					concat(auditable, AuditableEntity_.header, Header_.flaggedDeletedDate)).isNull();
			break;
		case DELETED:
			pred = getExpressionGeneral(context.getCurrentExpressionMap(), context.getCurrentRoot(),
					concat(auditable, AuditableEntity_.header, Header_.flaggedDeletedDate)).isNotNull();
			break;
		default:
			pred = context.alwaysTrue();
			break;
		}

		return pred;
	}

	/**
	 * Perform the same auditable filtering as the DAO would do in the database but
	 * for a list of entities.
	 *
	 * @param all represents a collection of a Java
	 * @param auditableStatus represents an auditable status
	 * @param <A> the type of elements in this collection
	 * @return a list
	 */
	public static <A extends AuditableEntity> List<A> filter(Collection<A> all, AuditableStatus auditableStatus) {
		List<A> result = null;
		if (all != null) {
			switch (auditableStatus) {
			case ACTIVE:
				result = all.stream().filter(a -> !a.isDeleted()).collect(Collectors.toList());
				break;
			case DELETED:
				result = all.stream().filter(a -> a.isDeleted()).collect(Collectors.toList());
				break;
			default:
				result = all instanceof List ? (List<A>) all : all.stream().collect(Collectors.toList());
				break;
			}
		}

		return result;
	}

	/**
	 * Perfom auditable filtering.
	 *
	 * @param all represents a collection of a Java
	 * @param <A> the type of elements in this collection
	 * @return a list
	 */
	public static <A extends AuditableEntity> List<A> filter(Collection<A> all) {
		return filter(all, DEFAULT_AUDITABLE_STATUS);
	}

	@Override
	protected A removeBean(A oldBean) {
		A bean = oldBean;
		if (oldBean.isAuditingDisabled()) {
			bean = super.removeBean(oldBean);
		} else {
			bean.preRemove();
			bean = (A) getEM().merge(bean);
		}
		return bean;
	}

	/**
	 * Undelete a bean.
	 *
	 * @param bean represents a bean
	 * @return bean
	 */
	public A undelete(A bean) {
		bean.preUndelete();
		return super.save(bean);
	}

	/**
	 * Delete with disabled auditing - really remove from DB
	 *
	 * @param bean represents a bean
	 * @return deleted entity with disabled auditing
	 */
	public A deleteWithoutAuditing(A bean) {
		bean.setAuditingDisabled(true);
		return super.delete(bean);
	}

	/**
	 * Save with disabled auditing
	 *
	 * @param bean represents a bean
	 * @return saved entity with disabled auditing
	 */
	public A saveWithoutAuditing(A bean) {
		bean.setAuditingDisabled(true);
		return super.save(bean);
	}
}
