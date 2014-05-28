package pb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wallace on 14-1-15.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
// tag 表示协议中的 tag
public @interface FieldDef {
    //-1 表示使用自动生成tag
    int tag() default -1;

    PBWireFormat.FieldType type() default PBWireFormat.FieldType.UNKNOWN;
}

