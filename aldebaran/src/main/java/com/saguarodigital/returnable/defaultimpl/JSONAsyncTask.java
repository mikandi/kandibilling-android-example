package com.saguarodigital.returnable.defaultimpl;

import android.os.AsyncTask;

import com.saguarodigital.returnable.IReturnable;

import java.util.Map;

public class JSONAsyncTask<T extends IReturnable> extends AsyncTask<Void, Void, JSONResponse<T>>
{
    private NetworkTaskImpl<T> mTask;
    
    public JSONAsyncTask(Class<T> clazz, Map<String, String> queryArgs)
    {
        super();
        this.mTask = new NetworkTaskImpl<T>(clazz, queryArgs);
    }
    
    public JSONAsyncTask(Class<T> clazz, String uri)
    {
        super();
        this.mTask = new NetworkTaskImpl<T>(clazz, uri);
    }
    
    @Override
    protected JSONResponse<T> doInBackground(Void... args)
    {
        return this.mTask.backgroundTask();
    }
}
