package com.axonivy.utils.persistence.beans;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ToggleableEntity.class)
public abstract class ToggleableEntity_ extends com.axonivy.utils.persistence.beans.AuditableEntity_ {

	public static volatile SingularAttribute<ToggleableEntity, Date> expiryDate;
	public static volatile SingularAttribute<ToggleableEntity, Boolean> isEnabled;

	public static final String EXPIRY_DATE = "expiryDate";
	public static final String IS_ENABLED = "isEnabled";

}

