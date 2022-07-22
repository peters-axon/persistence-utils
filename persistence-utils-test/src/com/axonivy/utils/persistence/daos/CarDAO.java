package com.axonivy.utils.persistence.daos;

import com.axonivy.utils.persistence.Logger;
import com.axonivy.utils.persistence.dao.AuditableDAO;
import com.axonivy.utils.persistence.entities.Car;
import com.axonivy.utils.persistence.entities.Car_;


public class CarDAO extends AuditableDAO<Car_, Car> implements BaseDAO {
	private static final Logger LOG = Logger.getLogger(CarDAO.class);
	private static final CarDAO instance = new CarDAO();

	private CarDAO() {
	}

	public static CarDAO getInstance() {
		return instance;
	}

	@Override
	protected Class<Car> getType() {
		return Car.class;
	}
}
