package com.axonivy.utils.persistence.dao;

import java.io.Closeable;

import javax.transaction.TransactionRolledbackException;

/**
 * Interface marking a object which support auto commit of transactions
 *
 */
@FunctionalInterface
public interface AutoCloseTransaction extends Closeable{
	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws TransactionRolledbackException;
}
