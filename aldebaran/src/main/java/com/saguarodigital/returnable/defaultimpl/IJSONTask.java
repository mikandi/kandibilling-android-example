/**
 *
 */
package com.saguarodigital.returnable.defaultimpl;

import com.saguarodigital.returnable.IReturnable;

/**
 * @author rekaszeru
 * @date Aug 18, 2014
 */
public interface IJSONTask<T extends IReturnable>
{
    public JSONResponse<T> backgroundTask();
}
