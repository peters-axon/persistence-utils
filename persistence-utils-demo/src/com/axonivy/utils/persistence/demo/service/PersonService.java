package com.axonivy.utils.persistence.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.axonivy.utils.persistence.demo.Logger;
import com.axonivy.utils.persistence.demo.daos.PersonDAO;
import com.axonivy.utils.persistence.demo.entities.Department;
import com.axonivy.utils.persistence.demo.entities.Person;
import com.axonivy.utils.persistence.demo.enums.Role;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IRole;
import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.IUser;
import ch.ivyteam.ivy.security.user.NewUser;


/**
 * Services to work with persons.
 */
public class PersonService {
	private static final Logger LOG = Logger.getLogger(PersonService.class);
	private static Random random = new Random();
	private static final BigDecimal ONEHUNDRED = new BigDecimal("100");

	private PersonService() {
	}

	/**
	 * Sync all {@link Person}s to ivy users.
	 */
	public static void syncUsers() {
		ISecurityContext securityContext = Ivy.wf().getApplication().getSecurityContext();
		List<Person> all = PersonDAO.getInstance().findAll();

		IRole userRole = IvyService.findRole(Role.USER);

		for (Person person : all) {
			syncUser(securityContext, userRole, person);
		}
	}

	/**
	 * Sync a single {@link Person} to an ivy user.
	 *
	 * @param person
	 */
	public static void syncUser(Person person) {
		ISecurityContext securityContext = Ivy.wf().getApplication().getSecurityContext();
		IRole userRole = IvyService.findRole(Role.USER);
		syncUser(securityContext, userRole, person);
	}

	private static void syncUser(ISecurityContext securityContext, IRole userRole, Person person) {
		String ivyUserName = person.getIvyUserName();
		IUser user = IvyService.findUser(ivyUserName);
		if(person.isSyncToIvy() && StringUtils.isNotBlank(ivyUserName)) {
			if(user == null) {
				Department dpmnt = person.getDepartment();
				String dpmntName = dpmnt != null ? dpmnt.getName() : "no department";
				LOG.info("Creating user {0}", ivyUserName);
				user = securityContext.users().create(
						NewUser.create(ivyUserName)
							.fullName(String.format("%s %s (%s)", person.getFirstName(), person.getLastName(), dpmntName))
							.password("password")
							.mailAddress(String.format("%s.%s@demo.axonivy.com", person.getFirstName(), person.getLastName())).toNewUser());

				user.addRole(userRole);
			}
		}
		else {
			if(user != null) {
				LOG.info("Deleting user {0}", ivyUserName);
				securityContext.users().delete(ivyUserName);
			}
		}
	}

	/**
	 * Raise people's salaries.
	 */
	public static void raiseSalaries() {
		List<Person> all = PersonDAO.getInstance().findAll();
		BigDecimal probability = ConfigurationService.getRaiseSalaryProbability().divide(ONEHUNDRED, 3, RoundingMode.HALF_UP);
		BigDecimal factor = BigDecimal.ONE.add(ConfigurationService.getRaiseSalaryPercentage().divide(ONEHUNDRED, 3, RoundingMode.HALF_UP));
		for (Person person : all) {
			if(random.nextDouble() < probability.doubleValue()) {
				BigDecimal oldSalary = person.getSalary();
				BigDecimal newSalary = factor.multiply(oldSalary);
				newSalary = newSalary.setScale(2, RoundingMode.HALF_UP);
				person.setSalary(newSalary);
				PersonDAO.getInstance().save(person);
				LOG.info("Raised salary of {0} {1} from {2} to {3}", person.getFirstName(), person.getLastName(), oldSalary, newSalary);
			}
		}
	}
}
