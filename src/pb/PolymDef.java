package pb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wallace on 14-2-19.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PolymDef {
    // 序列化过的数据，顺序绝对不能改，也不能删，否则id对不上
    Class<?>[] classes() default {};

    int[] tags() default {};
}

