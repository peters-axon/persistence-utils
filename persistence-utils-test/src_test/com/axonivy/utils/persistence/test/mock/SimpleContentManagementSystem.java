package com.axonivy.utils.persistence.test.mock;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ch.ivyteam.ivy.cm.ContentManagementSystem;
import ch.ivyteam.ivy.cm.ContentObject;
import ch.ivyteam.ivy.cm.IContentManagementSystem;
import ch.ivyteam.ivy.cm.IContentObject;
import ch.ivyteam.ivy.cm.IContentObjectValue;
import ch.ivyteam.ivy.cm.event.ContentManagementEventListener;
import ch.ivyteam.ivy.persistence.PersistencyException;


public class SimpleContentManagementSystem implements IContentManagementSystem {

	private final Map<String, Object> uriMap = new HashMap<>();

	public Object put(String key, Object value) {
		return uriMap.put(key, value);
	}

	@Override
	public String co(String uri) {
		Object result = uriMap.get(uri);
		return result != null ? result.toString() : "";
	}

	@Override
	public String co(String uri, List<Object> formatObjects) {
		return MessageFormat.format(co(uri), formatObjects.toArray());
	}

	@Override
	public String coLocale(String uri, Locale locale) {
		return co(uri) + "<" + locale.getISO3Language() + ">";
	}

	@Override
	public IContentManagementSystem getContentManagementSystem() { throw new NotMockedException(); }

	@Override
	public String getName() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public String getDescription() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public Object getIdentifier() { throw new NotMockedException(); }

	@Override
	public List<Locale> getSupportedLanguages() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public void addSupportedLanguage(Locale language) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public void removeSupportedLanguage(Locale language) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public boolean isSupportedLanguage(Locale language) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public Locale getDefaultLanguage() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public void setDefaultLanguage(Locale defaultLanguage) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public void setDefaultPageLayout(IContentObject defaultLayout) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public void setDefaultPageStyleSheet(IContentObject defaultStyleSheet) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject getDefaultPageLayout() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject getDefaultPageStyleSheet() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject getRootContentObject() throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject getContentObject(String uri, boolean searchInRequiredProjects) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject findContentObject(String uri) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObject getContentObject(String uri) throws PersistencyException { throw new NotMockedException(); }

	@Override
	public IContentObjectValue getContentObjectValue(String uri, Locale language, boolean searchInRequiredProjects) throws PersistencyException, IllegalArgumentException {
		throw new NotMockedException();
	}

	@Override
	public IContentObjectValue findContentObjectValue(String uri, Locale language) throws PersistencyException {
		throw new NotMockedException();
	}

	@Override
	public IContentObjectValue getContentObjectValue(String uri, Locale language) throws PersistencyException {
		throw new NotMockedException();
	}

	@Override
	public IContentObject getContentObjectForKey(Object key) throws PersistencyException {
		throw new NotMockedException();
	}

	@Override
	public String coLocale(String uri, String locale) { throw new NotMockedException(); }

	@Override
	public String cr(String uri) { throw new NotMockedException(); }

	@Override
	public String getKey() { throw new NotMockedException(); }

	@Override
	public ContentObject root() { throw new NotMockedException(); }

	@Override
	public Optional<ContentObject> get(String uri) { throw new NotMockedException(); }

	@Override
	public Set<Locale> locales() { throw new NotMockedException(); }

	@Override
	public ContentManagementSystem cms() { throw new NotMockedException(); }

	@Override
	public void addListener(ContentManagementEventListener listener) { throw new NotMockedException(); }

	@Override
	public void removeListener(ContentManagementEventListener listener) { throw new NotMockedException(); }
}
