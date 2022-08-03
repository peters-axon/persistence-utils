package com.axonivy.utils.persistence.test.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.persistence.StringUtilities;
import com.axonivy.utils.persistence.daos.HistorizedPersonDAO;
import com.axonivy.utils.persistence.entities.HistorizedPerson;
import com.axonivy.utils.persistence.history.beans.History;
import com.axonivy.utils.persistence.history.dao.HistoryDAO;
import com.axonivy.utils.persistence.test.DemoTestBase;

import ch.ivyteam.ivy.environment.IvyTest;


@IvyTest
public class HistorizedPersonDAOTest extends DemoTestBase {
	private static final HistorizedPersonDAO DAO = HistorizedPersonDAO.getInstance();

	
	@BeforeEach
	public void prepare() throws Exception {
		switchToSystemUser();
		prepareTestDataAndMocking(true);
	}


	//@Test
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
		assertThat(result).as("Found no history entry").isEmpty();
		
		historizedPerson2.setLastName("Binder");
		historizedPerson2 = DAO.save(historizedPerson2);
		
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson2.getId());
		assertThat(result).as("Found exactly one history entry").hasSize(1);
		
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson.getId());
		assertThat(result).as("Found exactly one history entry").hasSize(1);
		
		DAO.delete(historizedPerson);
		result = historyDAO.findByTypeAndId(HistorizedPerson.class, historizedPerson.getId());

		assertThat(result).as("Found exactly two history entries").hasSize(2);
		assertThat(StringUtilities.fromJSONToObject(result.get(1).getJsonData(), HistorizedPerson.class).getLastName())
			.as("Name of history entries match").isEqualTo("Mayer");
		assertThat(StringUtilities.fromJSONToObject(result.get(0).getJsonData(), HistorizedPerson.class).getLastName())
			.as("Name of history entries match").isEqualTo("Maier");
		
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
