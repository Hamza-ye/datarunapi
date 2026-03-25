package org.nmcpye.datarun.web.mvc.annotation;

import org.nmcpye.datarun.sharedkernal.DRunApiVersion;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    @AliasFor("include")
    DRunApiVersion[] value() default DRunApiVersion.ALL;

    @AliasFor("value")
    DRunApiVersion[] include() default DRunApiVersion.ALL;

    DRunApiVersion[] exclude() default {};
}
