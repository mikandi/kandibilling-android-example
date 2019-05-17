package com.saguarodigital.returnable.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation added to classes to indicate that they are a returnable type.
 * By adding this annotation, the class can be used directly as part of
 * a deserializer. It is assumed that all data is transfered using
 * a default object (DefaultJSONObject, being the current implementation).
 * In any case, the actual data to be desrialized shoudl live in a top level
 * <code>data</node>.
 *
 * @author Christopher O'Connell
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type
{
    /**
     * Indicates to the deserialize how to treat the raw data. In general,
     * DJO's are either arrays of objects or individual objects, however, the
     * the {@link #ARRAY} data type may be used where the root data object
     * contains a list of simple strings.
     *
     * @author Christopher O'Connell
     */
    public enum JSONDataType
    {
        OBJECT, ARRAY, OBJECT_ARRAY, EMPTY
    }
    
    ;
    
    JSONDataType[] type() default {JSONDataType.OBJECT_ARRAY};
    
    String name() default "";
    
    /**
     * Version is a monototically increasing number used by the caching engine to
     * determine if the object implementation has changed. In the event that the
     * version has increased since the caching engine last saved the cache, it may
     * either attempt an upgrade or simply clear the cache and start over.
     */
    int version();
    
    /**
     * The base parameter is only applicable to ARRAY and OBJECT_ARRAY types.
     * Consider the json:
     * <code>
     * {
     * ...,
     * data: {
     * some_key: []
     * },
     * ...
     * }
     * </code>
     * In this case, the data is either an ARRAY, or an OBJECT_ARRAY, but it lives
     * on a subkey of data instead of data itself being an array. In order to handle
     * this case, <code>base</code> would be set to 'some_key'.
     * <p>
     * In the event, however that data itself is an array, then base should be left
     * empty, unless it needs to be both the raw "data" base value as well as specified
     * values, in which case the first value of base should be "data".
     * <p>
     * Base is an array so that multiple bases may be incorporated into a single type.
     * The first matched base is the one parsed.
     */
    String[] base() default {};
    
    /**
     * Provides a hint to the networking code as to how many times this type may be
     * retried over the network before a failure is raised. Note: Networking libraries
     * are not guaranteed to retry the access this many times, but take this as a
     * retry hint. Library <b>will not</b> access the network more than this many
     * times, however.
     */
    int retries() default 3;
}
