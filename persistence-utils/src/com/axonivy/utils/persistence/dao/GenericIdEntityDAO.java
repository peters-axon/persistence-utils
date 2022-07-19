package com.axonivy.utils.persistence.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.Path;

import org.apache.commons.collections.CollectionUtils;

import com.axonivy.utils.persistence.beans.GenericIdEntity;
import com.axonivy.utils.persistence.beans.GenericIdEntity_;
import com.axonivy.utils.persistence.logging.Logger;

/**
 * Dao for genricId entity
 *
 * @param <MetaDataGeneric> meta model
 * @param <G> entity
 */
public abstract class GenericIdEntityDAO <MetaDataGeneric extends GenericIdEntity_,G extends GenericIdEntity> extends GenericDAO<MetaDataGeneric,G> {

	static final Logger log = Logger.getLogger(GenericIdEntityDAO.class);

	/**
	 * Generate a new id.
	 *
	 * The id is a random {@link UUID}.
	 *
	 * @return String with random UUID, without the "-"
	 */
	public String generateId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replaceAll("-", "");
	}


	/**
	 * Find all not deleted entities which have specified ids and where
	 * the user has permissions and client specific permissions for them.
	 *
	 * @param entities which will be filtered
	 * @return Collection of allowed entities
	 */
	public Collection<G> findByEntityIds(List<G> entities) {
		if (entities != null) {
			List<String> ids = entities.stream().map(e->e.getId()).collect(Collectors.toList());
			return findByIds(ids);
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Find all not deleted entities which have specified ids and where
	 * the user has permissions and client specific permissions for them.
	 *
	 * @param ids of entities which need to be selected
	 * @return Collection of allowed entities
	 */
	public Collection<G> findByIds(List<String> ids) {

		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<G>();
		}

		try(CriteriaQueryContext<G> query = initializeQuery();){
			Path<String> routeId = query.r.get(MetaDataGeneric.id);

			query.where(routeId.in(ids));

			return findByCriteria(query);
		}
	}
}
