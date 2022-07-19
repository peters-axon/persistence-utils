package com.axonivy.utils.persistence.history.beans;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(History.class)
public abstract class History_ extends com.axonivy.utils.persistence.beans.GenericEntity_ {

	public static volatile SingularAttribute<History, String> jsonData;
	public static volatile SingularAttribute<History, HistoryPK> id;
	public static volatile SingularAttribute<History, String> userName;
	public static volatile SingularAttribute<History, String> updateType;

	public static final String JSON_DATA = "jsonData";
	public static final String ID = "id";
	public static final String USER_NAME = "userName";
	public static final String UPDATE_TYPE = "updateType";

}

