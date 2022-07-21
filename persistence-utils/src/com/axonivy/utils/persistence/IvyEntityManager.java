package com.axonivy.utils.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;

import com.axonivy.utils.persistence.dao.AutoCloseTransaction;
import com.axonivy.utils.persistence.logging.Logger;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.data.persistence.IIvyEntityManager;

/**
 * Singleton class to hold IvyEntityManager.
 */
public class IvyEntityManager {

	private static final Logger LOG = Logger.getLogger(IvyEntityManager.class);

	/**
	 * Singleton instance.
	 */
	private static IvyEntityManager singleton = new IvyEntityManager();

	private final ThreadLocal<Integer> sessions = new ThreadLocal<>();

	private final ThreadLocal<Map<String, PersistenceContext>> threadLocalPersistenceContexts = new ThreadLocal<>();


	/**
	 * Container for objects which are cached for each thread, persistence unit and
	 * calling context.
	 */
	private static class PersistenceContext {

		private Session hibernateSession;
		private IIvyEntityManager iIvyEntityManager;

		/**
		 * Constructor.
		 *
		 * @param callerContext
		 * @param persistenceIdentifier
		 */
		private PersistenceContext(String callerContext, String persistenceIdentifier) {
		}

		/**
		 * @return the hibernateSession
		 */
		public Session getHibernateSession() {
			return hibernateSession;
		}

		/**
		 * @param hibernateSession the hibernate Session to set
		 */
		public void setHibernateSession(Session hibernateSession) {
			this.hibernateSession = hibernateSession;
		}

		/**
		 * @return the iIvyEntityManager
		 */
		public IIvyEntityManager getiIvyEntityManager() {
			return iIvyEntityManager;
		}

		/**
		 * @param iIvyEntityManager the iIvyEntityManager to set
		 */
		public void setiIvyEntityManager(IIvyEntityManager iIvyEntityManager) {
			this.iIvyEntityManager = iIvyEntityManager;
		}
	}

	/**
	 * Gets the single instance of IvyEntityManager.
	 *
	 * @return the single instance of the IvyEntityManager
	 */
	public static IvyEntityManager getInstance() {
		if (singleton == null) {
			synchronized (IvyEntityManager.class) {
				if (singleton == null) {
					singleton = new IvyEntityManager();
				}
			}
		}
		return singleton;
	}

	/**
	 * Locked constructor.
	 */
	private IvyEntityManager() {
	}

	/**
	 * Get the {@link PersistenceContext} for the given caller and persistence
	 * identifier.
	 *
	 * @param callerContext
	 * @param persistenceIdentifier
	 * @return
	 */
	private PersistenceContext getPersistenceContext(String callerContext, String persistenceIdentifier) {
		// get the thread local map of contexts
		Map<String, PersistenceContext> ctx = threadLocalPersistenceContexts.get();
		if (ctx == null) {
			ctx = new HashMap<>();
			threadLocalPersistenceContexts.set(ctx);
		}

		String persistenceContextKey = getPersistenceContextKey(callerContext, persistenceIdentifier);
		PersistenceContext context = ctx.get(persistenceContextKey);
		if (context == null) {
			context = new PersistenceContext(callerContext, persistenceIdentifier);
			ctx.put(persistenceContextKey, context);
		}

		return context;
	}

	/**
	 * Construct a {@link Map} key for a caller context and a persistence
	 * identifier.
	 *
	 * @param callerContext
	 * @param persistenceIdentifier
	 * @return
	 */
	private String getPersistenceContextKey(String callerContext, String persistenceIdentifier) {
		return callerContext + "." + persistenceIdentifier;
	}

	/**
	 * Get the {@link Session} for a specific persistence identifier.
	 *
	 * @param persistenceIdentifier the persistence unit to use
	 * @param properties a map use to create a new EntityManager
	 * @return the registered entity manager
	 */
	public Session getHibernateSession(String persistenceIdentifier, Map<?, ?> properties) {
		String callerContext = IvyUtilities.getProcessModelName();

		PersistenceContext persistentContext = getPersistenceContext(callerContext, persistenceIdentifier);

		Session session = persistentContext.getHibernateSession();

		if (session == null || !session.isOpen()) {
			Map<?, ?> emProperties = properties == null ? new HashMap<>() : properties;
			Session oldSession = session;

			IIvyEntityManager ivyEntityManager = persistentContext.getiIvyEntityManager();

			if (ivyEntityManager == null) {
				ivyEntityManager = Ivy.persistence().get(persistenceIdentifier);
				persistentContext.setiIvyEntityManager(ivyEntityManager);
			}

			// get a real entity manager from the ivy entity manager
			session = (Session) ivyEntityManager.createEntityManager(emProperties);
			if (oldSession == null) {
				LOG.debug("thread {0} created entity manager: {1}", Thread.currentThread().getId(), session);
			} else {
				LOG.debug("thread {0} recreated entity manager: {1} because the old entity manager: {2} was not open.",
						Thread.currentThread().getId(), session, oldSession);
			}
			persistentContext.setHibernateSession(session);
		}

		return session;
	}

	/**
	 * Add a new session to the active session count.
	 *
	 * Note: This function never opens a session. A session is created by a call to
	 * AbstractDAO#getEm() which is used by DAO functions. {@link #beginSession()}
	 * and {link {@link #closeSession()} can be used to cleanup Sessions. When the
	 * session count goes to zero, the {@link Session} (or Session)
	 * will be closed automatically.
	 * 
	 * @return Autocloseable instance
	 */
	public AutoCloseTransaction beginSession() {
		Integer count = sessions.get();
		if (count == null) {
			count = 0;
		}
		sessions.set(++count);
		LOG.debug("thread {0} began a new session, nesting count is now {1}", Thread.currentThread().getId(), count);
		return () -> closeSession(); // call closeSession automatically when invoked via try with resources call ,
										// e.g. try( AutoCloseTransaction autoclose = beginSession) {...
	}

	/**
	 * Remove one session from the session count.
	 *
	 * If there are no more sessions, then close the {@link Session}.
	 */
	public void closeSession() {
		Integer count = sessions.get();
		if (count == null) {
			count = 1;
		}
		count--;

		boolean closedEm = false;
		if (count <= 0) {
			if (count < 0) {
				LOG.warn("thread {0} closed a session which was not opened, count is {1}",
						Thread.currentThread().getId(), count);
			}

			Map<String, PersistenceContext> persistenceContexts = threadLocalPersistenceContexts.get();

			if (persistenceContexts != null) {
				for (Entry<String, PersistenceContext> entry : persistenceContexts.entrySet()) {
					PersistenceContext persistenceContext = entry.getValue();
					Session session = persistenceContext.getHibernateSession();
					if (session != null && session.isOpen()) {
						session.clear();
						session.close();
					}
					persistenceContext.setHibernateSession(null);
					LOG.debug("thread {0} context {1} closed entity manager: {2} because session nesting count was 0",
							Thread.currentThread().getId(), entry.getKey(), session);
					closedEm = true;
				}
			}

			sessions.remove();
		} else {
			sessions.set(count);
		}

		LOG.debug("thread {0} closed a session, nesting count is now {1} {2}", Thread.currentThread().getId(), count,
				closedEm ? "(the entity manager was closed)" : "");
	}
	
}