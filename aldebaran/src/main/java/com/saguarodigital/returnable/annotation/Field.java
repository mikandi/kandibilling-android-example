package com.saguarodigital.returnable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Marks fields in returnables as being dynamic fields, to
 * unserialize from DefaultJsonObjects and store in a cache.
 *
 * @author Christopher O'Connell
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field
{
    /**
     * The fundamental type which this field encapsulates. In general, it is safe
     * to treat all fields as either {@link #TEXT} or {@link #LIST}, however, implementations
     * are encouraged to set fields guaranteed to be numbers as {@link #NUMBER}, as this
     * may allow underlying caching implementations (such as those which rely on an SQL
     * variant) to optimize the cache.
     *
     * @author Christopher O'Connell
     */
    public enum Type
    {
        /**
         * Represents a field which is exclusively a number. Caching implementations
         * must make sure that numbers are stored with at least the precision of the
         * {@link Double} numeric type. Caching implementations may store numbers as
         * {@link TEXT}.
         */
        NUMBER,
        /**
         * Represents a field which is exclusively a boolean (in the bi-state sense).
         * Caching implementations may store booleans as a {@link #TEXT}.
         */
        BOOLEAN,
        /**
         * Represents a field which contains general textual information. This is the
         * core type for "fundamental" data, and should be used in any case where the
         * data is best represented as a string. Do note, however, that a specialized
         * {@link #NUMBER} type exists for fields which are guaranteed to be numbers and
         * an {@link #BOOLEAN} type for boolean fields. These specialized types should
         * not be used for nullable fields. It is safe to use a {@link #TEXT} type
         * instead of a {@link #NUMBER} type or {@link #BOOLEAN} type.
         */
        TEXT,
        /**
         * Indicates that this field is a complex object. If the field is annotated
         * as an object, it's declared class <b>must</b> declare a {@link Type}.
         */
        OBJECT,
        /**
         * Indicates that the field is a list of objects. The annotated filed must be of
         * type {@link List} with a generic type of {@link Integer}, {@link Double}, {@link String}
         * or a specific object. However, if declared as a specific object, the object must declare
         * a {@link Type}.
         */
        LIST
    }
    
    ;
    
    /**
     * Creates constraints on the field to determine if deserialization succeeds and hint to the
     * cache as to data storage preferences. During the deserialization phase, {@link PRIMAY_KEY},
     * {@link UNIQUE} and {@link NOT_NULL} are all treated as "required for deserializaiton to succeed".
     * {@link PRIMARY_KEY} and {@link UNIQUE} both indicate to the caching engine that only one object
     * with that key may exist at any one time. {@link PRIMARY_KEY} is solely a hint to the caching
     * engine that it <em>may</em> treat the field as an SQL style primary key, however, caching engines
     * are not required to treat it in such a fashion and may instead treat {@link PRIMARY_KEY} as
     * {@link UNIQUE}.
     * <p>
     * It is important to note that the desrialization engine does not enforce uniqueness during
     * deserialization, and that data with no caching engine may have multiple elements which violate
     * the uniqueness constraint.
     *
     * @author Christopher O'Connell
     */
    public enum Constraint
    {
        /**
         * Indicates that the field is required and should be unique in the cache, further it hints
         * to the caching engine that this is a good field to set as the primary key. The caching
         * implementation may use this as a primary key (in SQL variant engines), but is only
         * required to treat a primary key field as {@link UNIQUE}.
         */
        PRIMARY_KEY,
        /**
         * Indicates that the field is required represents a uniqueness constraint for the {@link Type}.
         * In general, all unique and {@link #PRIMARY_KEY} key fields, taken together as a
         * tuple should be unique within the context of the caching engine.
         */
        UNIQUE,
        /**
         * Indicates that the deserializer should fail if the field is not set or cannot be correctly
         * coerced to the requisite type. Both {@link #UNIQUE} and {@link #PRIMARY_KEY} are stronger
         * versions of not null, and are treated as not null during deserialization.
         * <p>
         * <em>Note:</em> Fields which declare a restrictive type such as {@link Type#NUMBER} and
         * {@link Type#BOOLEAN} automatically imply not null, even if not explicitly tagged as such.
         */
        NOT_NULL,
        /**
         * Indicates that the deserialize should treat the field as optional, and set it to the
         * <code>null</code> raw type if it cannot be found or successfully type coerced.
         */
        NONE
    }
    
    ;
    
    /**
     * The specific type assigned to {@link Field#generic_type()} to indicate
     * that a lists capturing type is a primitive type.
     */
    public class PrimativeListType
    {
        public class IntegerListType extends PrimativeListType
        {
        }
        
        ;
        
        public class LongListType extends PrimativeListType
        {
        }
        
        ;
        
        public class StringListType extends PrimativeListType
        {
        }
        
        ;
        
        public class BooleanListType extends PrimativeListType
        {
        }
        
        ;
        
        public class DoubleListType extends PrimativeListType
        {
        }
        
        ;
    }
    
    ;
    
    Type type();
    
    /**
     * Database style constraint on this Field
     */
    Constraint constraint() default Constraint.NONE;
    
    /**
     * The name of this element in json documents (if different from the field name).
     */
    String json_name() default "";
    
    /**
     * The name of this element in the cache, if different from the field name).
     */
    String cache_name() default "";
    
    /**
     * The specific class (or interface, abstract, etc) to instantiate for generic types
     * Only examined while inflating a list.
     */
    @SuppressWarnings("rawtypes")
    Class generic_type() default PrimativeListType.class;
}
