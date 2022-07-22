package com.axonivy.utils.persistence.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.dbunit.dataset.DataSetException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.StringUtilities;
import com.axonivy.utils.persistence.daos.HistorizedPersonDAO;
import com.axonivy.utils.persistence.entities.HistorizedPerson;
import com.axonivy.utils.persistence.history.beans.History;
import com.axonivy.utils.persistence.history.dao.HistoryDAO;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class HistorizedPersonDAOTest extends DemoTestBase {
	private static final HistorizedPersonDAO DAO = HistorizedPersonDAO.getInstance();

	@Before
	public void prepare() throws DataSetException, FileNotFoundException, IOException  {
		switchToSystemUser();
		//prepareTestDataAndMocking(true);
	}

	@Test
	public void testHistory() {
		HistoryDAO historyDAO = new HistoryDAO(DAO.getPersistenceUnitName());
		
		HistorizedPerson historizedPerson = new HistorizedPerson();
		historizedPerson.setFirstName("Sepp");
		historizedPerson.setLastName("Mayer");
		historizedPerson = DAO.save(historizedPerson);
		
		historizedPerson.setLastName("Maier");
		historizedPerson = DAO.save(historizedPerson);
		
		HistorizedPerson historizedPerson2 = new HistorizedPerson();
		historizedPerson2.setFirstName("Franz");
		historizedPerson2.setLastName("Huber");
		historizedPerson2 = DAO.save(historizedPerson2);
		
		List<History> result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson2.getId());
		assertTrue("found no history entry", result.size() == 0);
		
		historizedPerson2.setLastName("Binder");
		historizedPerson2 = DAO.save(historizedPerson2);
		
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson2.getId());
		assertTrue("found exactly one history entry", result.size() == 1);
		
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson.getId());
		assertTrue("found exactly one history entry", result.size() == 1);
		
		DAO.delete(historizedPerson);
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson.getId());
		
		assertTrue("found exactly two history entries", result.size() == 2);		
		assertEquals("name of history entries match", "Mayer", StringUtilities.fromJSONToObject(result.get(1).getJsonData(), HistorizedPerson.class).getLastName());
		assertEquals("name of history entries match", "Maier", StringUtilities.fromJSONToObject(result.get(0).getJsonData(), HistorizedPerson.class).getLastName());
		
		//switchOnLogging(Level.INFO, packageLevelHibernateFull());
		/*result.forEach(history -> {
            LOG.info("History by id => {0}", history.getId().getEntityId());
            LOG.info("Entity type => {0}", history.getId().getEntityType());
            LOG.info("Time created => {0}", history.getId().getTimestamp());
            LOG.info("jsonData => {0}", history.getJsonData());
            LOG.info("Update type => {0}", history.getUpdateType());
            LOG.info("User name => {0}", history.getUserName());
            LOG.info("----------------------------------------------------");
		});*/
	}

}
