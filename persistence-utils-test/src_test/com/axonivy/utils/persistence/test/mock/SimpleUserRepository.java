package com.axonivy.utils.persistence.test.mock;

import ch.ivyteam.ivy.persistence.query.IPagedResult;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.query.IUserQueryExecutor;
import ch.ivyteam.ivy.security.query.UserQuery;
import ch.ivyteam.ivy.security.user.IUserRepository;
import ch.ivyteam.ivy.security.user.NewUser;


public class SimpleUserRepository implements IUserRepository {

	@Override
	public IUser create(String userName) {
		throw new NotMockedException();
	}

	@Override
	public IUser create(String userName, String password) {
		throw new NotMockedException();
	}

	@Override
	public IUser create(NewUser newUser) {
		return Mocked.securityContext.createUser(newUser.getName(), 
				newUser.getFullName(), 
				newUser.getPassword(), 
				newUser.getLanguage(), 
				newUser.getMailAddress(), 
				newUser.getExternalName());
	}

	@Override
	public void delete(String userName) {
		throw new NotMockedException();
	}

	@Override
	public IUser findWithExternalLookup(String userName) {
		return Mocked.securityContext.findUser(userName);
	}

	@Override
	public IUser find(String userName) {
		throw new NotMockedException();
	}

	@Override
	public IUser find(long userId) {
		throw new NotMockedException();
	}

	@Override
	public IUser system() {
		return Mocked.securityContext.getSystemUser();
	}

	@Override
	public IPagedResult<IUser> paged() {
		throw new NotMockedException();
	}

	@Override
	public IPagedResult<IUser> paged(int pageSize) {
		throw new NotMockedException();
	}

	@Override
	public UserQuery query() {
		throw new NotMockedException();
	}

	@Override
	public IUserQueryExecutor queryExecutor() {
		throw new NotMockedException();
	}

	@Override
	public long count() {
		throw new NotMockedException();
	}

	@Override
	public IUser findById(String securityMemberId) { throw new NotMockedException(); }
}
