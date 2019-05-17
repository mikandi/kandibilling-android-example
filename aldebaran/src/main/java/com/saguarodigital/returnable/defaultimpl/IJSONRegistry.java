/**
 *
 */
package com.saguarodigital.returnable.defaultimpl;

import com.saguarodigital.returnable.IReturnable;

/**
 * @author rekaszeru
 * @date Aug 18, 2014
 */
public interface IJSONRegistry
{
    /**
     * Returns the parser for the passed type
     *
     * @param type
     *
     * @return
     */
    Class<IReturnable> getTargetClass(final String type);
}
