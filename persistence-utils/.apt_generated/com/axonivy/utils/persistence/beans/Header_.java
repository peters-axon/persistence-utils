package com.axonivy.utils.persistence.beans;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Header.class)
public abstract class Header_ {

	public static volatile SingularAttribute<Header, String> createdByUserName;
	public static volatile SingularAttribute<Header, Date> createdDate;
	public static volatile SingularAttribute<Header, String> modifiedByUserName;
	public static volatile SingularAttribute<Header, String> flaggedDeletedByUserName;
	public static volatile SingularAttribute<Header, Date> modifiedDate;
	public static volatile SingularAttribute<Header, Date> flaggedDeletedDate;
	public static volatile SingularAttribute<Header, Integer> HEADERINITIALIZER;

	public static final String CREATED_BY_USER_NAME = "createdByUserName";
	public static final String CREATED_DATE = "createdDate";
	public static final String MODIFIED_BY_USER_NAME = "modifiedByUserName";
	public static final String FLAGGED_DELETED_BY_USER_NAME = "flaggedDeletedByUserName";
	public static final String MODIFIED_DATE = "modifiedDate";
	public static final String FLAGGED_DELETED_DATE = "flaggedDeletedDate";
	public static final String H_EA_DE_RI_NI_TI_AL_IZ_ER = "HEADERINITIALIZER";

}

