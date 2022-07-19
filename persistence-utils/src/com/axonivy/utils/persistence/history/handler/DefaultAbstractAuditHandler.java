package com.axonivy.utils.persistence.history.handler;

import java.io.Serializable;
import java.sql.Timestamp;

import ch.ivyteam.ivy.environment.Ivy;

import com.axonivy.utils.persistence.StringUtilities;
import com.axonivy.utils.persistence.beans.GenericEntity;
import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.enums.UpdateType;
import com.axonivy.utils.persistence.history.beans.History;
import com.axonivy.utils.persistence.history.beans.HistoryPK;
import com.axonivy.utils.persistence.history.dao.HistoryDAO;
import com.axonivy.utils.persistence.history.util.ClobUtil;
import com.axonivy.utils.persistence.logging.Logger;

/**
 * Abstract class for default audit handler. It creates default behaviors
 * ,creates history record in the table History, for handleCreate, handleUpdate,
 * handleDelete and handleRead.
 * 
 * @author maonguyen
 *
 */
public abstract class DefaultAbstractAuditHandler implements AuditHandler {

	private static final Logger LOG = Logger.getLogger(DefaultAbstractAuditHandler.class);

	@Override
	public <T extends GenericEntity<? extends Serializable>> void handleCreate(T bean) {
		// DO nothing
	}

	@Override
	public <T extends GenericEntity<? extends Serializable>> void handleUpdate(T current, T bean) {
		createHistory(current, UpdateType.UPDATE);
	}

	@Override
	public <T extends GenericEntity<? extends Serializable>> void handleDelete(T bean) {
		createHistory(bean, UpdateType.DELETE);
	}

	@Override
	public void handleRead(CriteriaQueryGenericContext<?, ?> query) {
		// Do nothing 
	}

	/**
	 * Create history by update type in this case exist entity ID 
	 * 
	 * @param bean
	 * @param updateType
	 */
	private <T extends GenericEntity<? extends Serializable>> void createHistory(T bean, UpdateType updateType) {
		if (bean.getId() != null) {
			History historyEntity = new History();
			HistoryPK historyPK = new HistoryPK();

			historyPK.setTimestamp(new Timestamp(System.currentTimeMillis()));
			historyPK.setEntityType(bean.getClass().getName());
			historyPK.setEntityId(bean.getId().toString());
			historyEntity.setId(historyPK);
			historyEntity.setUserName(Ivy.session().getSessionUserName());
			historyEntity.setUpdateType(updateType.name());
			historyEntity.setJsonData(StringUtilities.fromObjectToJSON(bean));
			HistoryDAO historyDAO = new HistoryDAO(getHandlerPersistenceUnitName());
			historyDAO.save(historyEntity);
		} else {
			LOG.info("Do not create history because entity di not set!");
		}
	}

}
