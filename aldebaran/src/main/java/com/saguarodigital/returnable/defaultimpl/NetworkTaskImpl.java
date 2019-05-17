package com.saguarodigital.returnable.defaultimpl;

import com.saguarodigital.returnable.IReturnable;
import com.saguarodigital.returnable.Logger;
import com.saguarodigital.returnable.annotation.Type;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkTaskImpl<T extends IReturnable> implements IJSONTask<T>
{
	private static final String TAG = "NetworkTask";
    private static int sCounter = 0;
    private String mUri;
    private Class<T> mClazz;
    private String mTag;
    
    public NetworkTaskImpl(Class<T> clazz, Map<String, String> queryArgs)
    {
        this.mClazz = clazz;
        this.mUri = this.getNewObj().getUri(queryArgs);
        
        init();
        if (Logger.networkDebug) Logger.d("%1$s - Started up with  %2$s", this.mTag, clazz.getSimpleName() + " and " + this.mUri);
    }
    
    public NetworkTaskImpl(Class<T> clazz, String uri)
    {
        this.mClazz = clazz;
        this.mUri = uri;
        
        init();
        
        if (Logger.networkDebug) Logger.d("%1$s - Started up with  %2$s", this.mTag, clazz.getSimpleName() + " and " + this.mUri);
    }
    
    private void init()
    {
        this.createLogTag();
    }
    
    @Override
    public JSONResponse<T> backgroundTask()
    {
        Call call = null;
        List<T> data = new ArrayList<T>();
        T obj = this.getNewObj();
        if (obj == null)
        {
            throw new RuntimeException("Unable to create object");
        }
        String retData = null;
        JSONObject jo = null;
        try
        {
            final Request request = new Request.Builder().url(this.mUri).build();
            
            if (Logger.networkDebug) Logger.i("%1$s - OkHttp Fetching  %2$s", this.mTag, this.mUri);
            call = JSONAsyncTaskLoader.sClient.newCall(request);
            Response response = call.execute();
            
            if (!response.isSuccessful())
            {
                throw new IOException("OkHttp Unexpected code " + response);
            }
            
            retData = response.body().string();
            
            if (Logger.networkDebug) Logger.i("%1$s - Fetch Success: %2$s", this.mTag, retData);
        }
        catch (IOException ex)
        {
            if (Logger.networkDebug) Logger.e("%1$s - Fetch Failure: %2$s", this.mTag, retData, ex);
            return null;
        }
        catch (Exception ex)
        {
            if (Logger.networkDebug) Logger.e("%1$s - Unexpected Exception", this.mTag, ex);
            return null;
        }
        finally
        {
            if (Logger.networkDebug) Logger.d(this.mTag + " Posting results");
            if (call != null)
            {
                call.cancel();
            }
            if (retData == null)
            {
                if (Logger.networkDebug) Logger.d(this.mTag + " Null Payload Detected");
                return null;
            }
            try
            {
                jo = new JSONObject((String) retData);
            }
            catch (Exception ex)
            {
                if (Logger.networkDebug) Logger.e("%1$s - Unable to parse RAW JSON from  %2$s", this.mTag, retData, ex);
            }
            if (jo == null)
            {
                return null;
            }
        }
        final JSONObject original = jo;
        if (Logger.networkDebug) Logger.i("%1$s - Parsing data as  %2$s", this.mTag, this.mClazz.getSimpleName());
        Type t = this.getType().getAnnotation(Type.class);
//		} else if(t.type() == Type.JSONDataType.OBJECT_ARRAY) {
        if (hasType(t.type(), Type.JSONDataType.EMPTY))
        {
            return new JSONResponse<T>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unknown Response Status"));
        }
        if (data.isEmpty() && hasType(t.type(), Type.JSONDataType.OBJECT_ARRAY))
        {
            if (Logger.networkDebug) Logger.i(this.mTag + " Data is an OBJECT_ARRAY");
            JSONArray ja = null;
            if (t.base().length > 0)
            {
                jo = jo.optJSONObject("data");
                for (final String b : t.base())
                {
                    if ((jo != null && jo.has(b)) || b.equals("data"))
                    {
                        if (Logger.networkDebug) Logger.i("%1$s - Found base ' %2$s", this.mTag, b + "'");
                        if (b.equals("data"))
                        {
                            ja = original.optJSONArray("data");
                        }
                        else
                        {
                            if (jo == null)
                            {
                                if (Logger.networkDebug) Logger.w(this.mTag + " 'data' element is null, sending error codes");
                                return new JSONResponse<T>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original
                                        .optString("message", "Unknown Response Status"));
                            }
                            ja = jo.optJSONArray(b);
                        }
                        if (ja == null)
                        {
                            if (jo == null)
                            {
                                if (Logger.networkDebug) Logger.w(this.mTag + " 'data' element is null, sending error codes");
                                return new JSONResponse<T>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original
                                        .optString("message", "Unknown Response Status"));
                            }
                            final JSONObject tryObj = jo.optJSONObject(b);
                            if (tryObj != null && !hasType(t.type(), Type.JSONDataType.ARRAY) && hasType(t.type(), Type.JSONDataType.OBJECT))
                            {
                                try
                                {
                                    if (Logger.networkDebug) Logger.i("%1$s - Opting single object at base ' %2$s", this.mTag, b + "'");
                                    T val = this.getNewObj();
                                    if (val.getParser().parse(tryObj, val))
                                    {
                                        data.add(val);
                                    }
                                }
                                catch (Exception e)
                                {
                                    if (Logger.networkDebug) Logger.e(this.mTag + " Exception caught in NetworkTaskImpl.backgroundTask", e);
                                }
                                return new JSONResponse<T>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original
                                        .optString("message", "Unknown Response Status"));
                            }
                        }
                    }
                    else
                    {
                        if (Logger.networkDebug) Logger.i("%1$s - Base ' %2$s", this.mTag, b + "' not found");
                    }
                }
            }
            else
            {
                ja = jo.optJSONArray("data");
            }
            if (ja == null)
            {
                if (Logger.networkDebug) Logger.e(this.mTag + " Unable to opt main data array");
				if (!hasType(t.type(), Type.JSONDataType.ARRAY) && !hasType(t.type(), Type.JSONDataType.OBJECT))
				{ return new JSONResponse<T>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unknown Response Status")); }
            }
            else
            {
                if (Logger.networkDebug) Logger.i(this.mTag + " Parsing %d objects", ja.length());
                for (int i = 0; i < ja.length(); ++i)
                {
                    T val = this.getNewObj();
                    if (val.getParser().parse(ja.optJSONObject(i), val))
                    {
                        data.add(val);
                    }
                }
            }
//		} else if(t.type() == Type.JSONDataType.ARRAY) {
        }
        if (data.isEmpty() && hasType(t.type(), Type.JSONDataType.ARRAY))
        {
            // TODO: This is a super shitty quick fix. Make it right.
            if (Logger.networkDebug) Logger.i(this.mTag + " Data is an ARRAY");
            jo = jo.optJSONObject("data");
            JSONArray ja = jo.optJSONArray(t.base()[0]);
            if (Logger.networkDebug) Logger.i(this.mTag + " Parsing %d objects", ja.length());
            T val = null;
            for (int i = 0; i < ja.length(); ++i)
            {
                try
                {
                    val = this.getNewObj();
                    final JSONObject json = new JSONObject();
                    json.put("name", ja.optString(i));
                    if (val.getParser().parse(json, val))
                    {
                        data.add(val);
                    }
                }
                catch (Exception e)
                {
                    if (Logger.networkDebug) Logger.e("%1$s - Unable to opt data ' %2$s", this.mTag, ja.optString(i) + "' for array", e);
                }
            }
        }
        if (data.isEmpty() && hasType(t.type(), Type.JSONDataType.OBJECT))
        {
            try
            {
                if (Logger.networkDebug) Logger.i(this.mTag + " Data is an OBJECT");
                T val = this.getNewObj();
                final JSONObject tmp = original.optJSONObject("data");
                if (tmp != null && val.getParser().parse(tmp, val))
                {
                    data.add(val);
                }
            }
            catch (Exception e)
            {
                if (Logger.networkDebug) Logger.e(this.mTag + " Exception caught in NetworkTaskImpl.backgroundTask", e);
            }
        }
        return new JSONResponse<T>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unknown Response Status"));
    }
    
    public static boolean hasType(final Type.JSONDataType[] typeArray, final Type.JSONDataType targetType)
    {
        for (final Type.JSONDataType type : typeArray)
        {
			if (type == targetType)
			{ return true; }
        }
        return false;
    }
    
    private T getNewObj()
    {
        try
        {
            return this.getType().newInstance();
        }
        catch (IllegalAccessException e)
        {
            if (Logger.networkDebug) Logger.e("IllegalAccessException instantiating object");
        }
        catch (InstantiationException e)
        {
            if (Logger.networkDebug) Logger.e("InstantiationException instantiating object");
        }
        return null;
    }
    
    private Class<T> getType()
    {
        return this.mClazz;
    }
    
    private void createLogTag()
    {
        this.mTag = TAG + "-" + (++sCounter);
    }
}
