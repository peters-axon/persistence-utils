package com.axonivy.utils.persistence.daos;

import com.axonivy.utils.persistence.dao.ToggleableDAO;
import com.axonivy.utils.persistence.entities.Producer;
import com.axonivy.utils.persistence.entities.Producer_;

public class ProducerDAO extends ToggleableDAO<Producer_, Producer> implements BaseDAO {

	@Override
	protected Class<Producer> getType() {
		return Producer.class;
	}
}
