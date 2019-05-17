package com.saguarodigital.returnable;

import com.saguarodigital.returnable.annotation.Field;
import com.saguarodigital.returnable.annotation.Type;

import org.json.JSONObject;

/**
 * The contract which parsers must implement.
 *
 * @param <E> An object with {@link Type} and {@link Field} annotations.
 *
 * @author Christopher O'Connell
 */
public interface IParser<E extends IReturnable>
{
    /**
     * Parses the supplied json into the specified object
     * type. The parse method <em>should</em> attempt to fill in
     * the supplied object. It may, however, re-instantiate as required.
     * In any case, the return value should be considered the fully parsed
     * version of the object and not an external reference to the passed object.
     *
     * @param jo  The json data to parse
     * @param val An instance of the object to parse into.
     *
     * @return
     *
     * @throws RuntimeException In the event that an unrecoverable parsing error occurs.
     * @retun Whether or not the object was successfully parsed
     */
    public <T> boolean parse(JSONObject jo, T val);
}
