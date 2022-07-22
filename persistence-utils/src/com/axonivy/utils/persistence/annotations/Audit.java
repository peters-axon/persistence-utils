package com.axonivy.utils.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.axonivy.utils.persistence.history.handler.AuditHandler;

/**
 * Audit annotation for handling entity history.
 * 
 * @author maonguyen
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
	Class<? extends AuditHandler> handler();
}
