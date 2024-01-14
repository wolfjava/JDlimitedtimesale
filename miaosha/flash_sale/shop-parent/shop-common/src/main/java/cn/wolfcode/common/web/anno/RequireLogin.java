package cn.wolfcode.common.web.anno;

import java.lang.annotation.*;

/**
 * Created by lanxw
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
