package com.axonivy.utils.persistence.dao;

public interface CascadeDelete<T> {
	public void deleteChildren(T bean);
}
