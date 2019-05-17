package com.saguarodigital.returnable.defaultimpl;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.AsyncTaskLoader;

import com.saguarodigital.returnable.IReturnable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class JSONAsyncTaskLoader<T extends IReturnable> extends AsyncTaskLoader<JSONResponse<T>>
{
    
    protected static final String sUserAgent = new StringBuilder()
            .append("Aldebaran/Apache-HttpClient/4.0").append(" (")
            .append("Android; ").append("SDK-Version ").append(Build.VERSION.SDK_INT).append("; ")
            .append("java 1.6)").append(" Mobile/").append(Build.ID).toString();
    protected static final OkHttpClient sClient = new Builder()
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addNetworkInterceptor(new UserAgentInterceptor(sUserAgent))
            .build();
    
    protected IJSONTask<T> mTask;
    
    public JSONAsyncTaskLoader(Context context, Class<T> clazz, Map<String, String> queryArgs)
    {
        super(context);
        initTask(clazz, queryArgs);
    }
    
    public JSONAsyncTaskLoader(Context context, Class<T> clazz, String uri)
    {
        super(context);
        initTask(clazz, uri);
    }
    
    @SuppressWarnings("unchecked")
    protected void initTask(final Object... params)
    {
        // skip ensuring parameter count
        if (params[1] instanceof String)
        {
            this.mTask = new NetworkTaskImpl<T>((Class<T>) params[0], (String) params[1]);
        }
        else
        {
            this.mTask = new NetworkTaskImpl<T>((Class<T>) params[0], (Map<String, String>) params[1]);
        }
    }
    
    @Override
    protected void onStartLoading()
    {
        this.forceLoad();
        super.onStartLoading();
    }
    
    @Override
    public JSONResponse<T> loadInBackground()
    {
        return this.mTask.backgroundTask();
    }
}
