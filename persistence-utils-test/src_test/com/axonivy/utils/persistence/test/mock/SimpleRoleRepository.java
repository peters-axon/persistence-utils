package com.axonivy.utils.persistence.test.mock;

import java.util.List;

import ch.ivyteam.ivy.security.IRole;
import ch.ivyteam.ivy.security.role.IRoleRepository;

public class SimpleRoleRepository implements IRoleRepository {

	@Override
	public void delete(String roleName) {
		throw new NotMockedException();
	}

	@Override
	public IRole find(String roleName) {
		return Mocked.securityContext.findRole(roleName);
	}

	@Override
	public IRole topLevel() {
		throw new NotMockedException();
	}

	@Override
	public List<IRole> all() {
		throw new NotMockedException();
	}

	@Override
	public List<IRole> active() {
		throw new NotMockedException();
	}

	@Override
	public int count() {
		throw new NotMockedException();
	}

}
