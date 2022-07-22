package com.axonivy.utils.persistence.demo.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.dao.markers.AuditableMarker;
import com.axonivy.utils.persistence.demo.daos.PersonDAO;
import com.axonivy.utils.persistence.demo.entities.Person;
import com.axonivy.utils.persistence.validation.ConstraintValidatorAdapter;


@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidUniqueIvyUserName.Validator.class)
public @interface ValidUniqueIvyUserName {
	String MESSAGE = "/Validations/com/axonivy/utils/persistence/demo/validation/ValidUniqueIvyUserName/duplicate";
	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * Client Id which can be null
	 */
	String clientId() default "";

	/**
	 * Validate, that Ivy user name is unique.
	 */
	public class Validator extends ConstraintValidatorAdapter<ValidUniqueIvyUserName, Person> {
		@Override
		public boolean isValid(Person person, ConstraintValidatorContext context) {
			boolean isValid = true;

			if(person != null && person.getIvyUserName() != null) {
				Person existing = PersonDAO.getInstance().findByIvyUserName(person.getIvyUserName(), new QuerySettings<Person>().withMarkers(AuditableMarker.ALL));

				if(existing != null && !existing.getId().equals(person.getId())) {
					addConstraintViolation(context, "ivyUserName", person.getIvyUserName());
					isValid = false;
				}
			}
			return isValid;
		}
	}
}
