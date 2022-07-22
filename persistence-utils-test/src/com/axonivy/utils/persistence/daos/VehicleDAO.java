package com.axonivy.utils.persistence.daos;

import com.axonivy.utils.persistence.dao.AuditableDAO;
import com.axonivy.utils.persistence.entities.Vehicle;
import com.axonivy.utils.persistence.entities.Vehicle_;


public class VehicleDAO extends AuditableDAO<Vehicle_, Vehicle> implements BaseDAO {
	private static final VehicleDAO instance = new VehicleDAO();

	private VehicleDAO() {
	}

	public static VehicleDAO getInstance() {
		return instance;
	}

	@Override
	protected Class<Vehicle> getType() {
		return Vehicle.class;
	}
}
