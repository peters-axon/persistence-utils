package com.axonivy.utils.persistence.test.mock;

import ch.ivyteam.ivy.security.role.IRoleRepository;
import ch.ivyteam.ivy.security.user.IUserRepository;


public class Mocked {
	public static SimplePersistenceContext persistenceContext;
	public static SimpleSecurityContext securityContext;
	public static SimpleWorkflowSession workflowSession;
	public static SimpleGlobalVariableContext globalVariableContext;
	public static SimpleContentManagementSystem contentManagementSystem;
	public static SimpleFacesContext facesContext;
	public static IUserRepository userRepository;
	public static IRoleRepository roleRepository;

	
	public static void initialize() {
		persistenceContext = new SimplePersistenceContext();
		securityContext = new SimpleSecurityContext();
		workflowSession = new SimpleWorkflowSession();
		globalVariableContext = new SimpleGlobalVariableContext();
		contentManagementSystem = new SimpleContentManagementSystem();
		facesContext = new SimpleFacesContext();
		userRepository = new SimpleUserRepository();
		roleRepository = new SimpleRoleRepository();
	}
}
