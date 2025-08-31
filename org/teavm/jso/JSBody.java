package org.teavm.jso;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JSBody {
    String script();
    String[] params() default {};
}
