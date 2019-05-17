package com.saguarodigital.returnable.defaultimpl;

import android.content.Context;

import com.saguarodigital.returnable.IReturnable;
import com.saguarodigital.returnable.annotation.Type;

/**
 * @author rekaszeru
 * @date Aug 18, 2014
 */
public class SimpleJSONAsyncTaskLoader extends JSONAsyncTaskLoader<IReturnable>
{
    private IJSONRegistry mRegistry;
    private Type mType;
    
    public SimpleJSONAsyncTaskLoader(Context context, String uri, Type type, IJSONRegistry registry)
    {
        super(context, IReturnable.class, uri);
        this.mRegistry = registry;
        this.mType = type;
        this.mTask = new JSONTaskImpl(uri, mType, mRegistry);
    }
    
    @Override
    protected void initTask(Object... params)
    {
    }
}
