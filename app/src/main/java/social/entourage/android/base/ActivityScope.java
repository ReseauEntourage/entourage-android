package social.entourage.android;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Commodity alias for Scope running only in an Activity
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScope {
}