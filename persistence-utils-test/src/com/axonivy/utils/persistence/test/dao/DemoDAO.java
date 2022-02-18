package com.axonivy.utils.persistence.test.dao;

import java.io.IOException;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;

import com.axonivy.utils.persistence.demo.daos.BaseDAO;

public class DemoDAO extends TestDAO implements BaseDAO {
	private static final String TESTDATA_RESOURCE_PATH = "/com/axonivy/utils/persistence/test/testdata/testdata.xlsx";
	private static final DemoDAO instance = new DemoDAO();

	
	protected DemoDAO() {
	}

	
	public static DemoDAO getInstance() {
		return instance;
	}

	public void loadStandardTestdata(boolean clean, String...tableNames) throws SQLException, DatabaseUnitException, IOException {
		importTablesFromExcelResource(clean, TESTDATA_RESOURCE_PATH, tableNames);
	}
}
