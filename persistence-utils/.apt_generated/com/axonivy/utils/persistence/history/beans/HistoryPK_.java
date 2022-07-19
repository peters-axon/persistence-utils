package com.axonivy.utils.persistence.history.beans;

import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HistoryPK.class)
public abstract class HistoryPK_ {

	public static volatile SingularAttribute<HistoryPK, String> entityType;
	public static volatile SingularAttribute<HistoryPK, String> entityId;
	public static volatile SingularAttribute<HistoryPK, Timestamp> timestamp;

	public static final String ENTITY_TYPE = "entityType";
	public static final String ENTITY_ID = "entityId";
	public static final String TIMESTAMP = "timestamp";

}

