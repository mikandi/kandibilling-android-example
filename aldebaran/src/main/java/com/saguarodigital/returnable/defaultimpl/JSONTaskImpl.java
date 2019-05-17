package com.saguarodigital.returnable.defaultimpl;

import com.saguarodigital.returnable.IParser;
import com.saguarodigital.returnable.IReturnable;
import com.saguarodigital.returnable.Logger;
import com.saguarodigital.returnable.annotation.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author rekaszeru
 * @date Aug 18, 2014
 */
public class JSONTaskImpl implements IJSONTask<IReturnable>
{
    private static final String TAG = "JSONTask";
    private static int sCounter = 0;
    private String mUri;
    private String mTag;
    private Type mType;
    private IJSONRegistry mRegistry;
    
    public JSONTaskImpl(String uri, Type type, IJSONRegistry registry)
    {
        this.mUri = uri;
        this.mType = type;
        this.mRegistry = registry;
        
        init();
        
        if (Logger.networkDebug) Logger.w("%1$s - OkHttp Started up with  %2$s", this.mTag, this.mUri);
    }
    
    private void init()
    {
        this.createLogTag();
    }
    
    @SuppressWarnings("unchecked")
    public JSONResponse<IReturnable> backgroundTask()
    {
        Call call = null;
        final ArrayList<IReturnable> data = new ArrayList<IReturnable>();
        String retData = null;
        JSONObject jo = null;
        JSONArray ja = null;
        try
        {
            final Request request = new Request.Builder().url(this.mUri).build();
            
            if (Logger.networkDebug) Logger.i("%1$s - Fetching  %2$s", this.mTag, this.mUri);
            call = JSONAsyncTaskLoader.sClient.newCall(request);
            
            Response response = call.execute();
            
            if (!response.isSuccessful())
            {
                throw new IOException("OkHttp Unexpected code " + response);
            }
            
            retData = response.body().string();
            
            if (Logger.networkDebug) Logger.i("%1$s - Fetch Success:  %2$s", this.mTag, retData);
        }
        catch (IOException ex)
        {
            if (Logger.networkDebug) Logger.e("%1$s - Fetch Failure:  %2$s", this.mTag, retData, ex);
            return null;
        }
        catch (Exception ex)
        {
            if (Logger.networkDebug) Logger.e(this.mTag + " Unexpected Exception", ex);
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
                if (Logger.networkDebug) Logger.e("%1$s - Unable to parse RAW JSON:  %2$s", this.mTag, retData, ex);
            }
            if (jo == null)
            {
                return null;
            }
        }
        final JSONObject original = jo;
        if (Logger.networkDebug) Logger.i("%1$s - Parsing data as  %2$s", this.mTag, this.mType.name());
        if (mType == null)
        {
            if (Logger.networkDebug) Logger.e("%1$s - ERROR parsing data as  %2$s", this.mTag, this.mType.name());
            return new JSONResponse<IReturnable>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unparseable data"));
        }
        if (hasType(mType.type(), Type.JSONDataType.EMPTY))
        {
            return new JSONResponse<IReturnable>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unknown Response Status"));
        }
        if (hasType(mType.type(), Type.JSONDataType.OBJECT_ARRAY))
        {
            if (Logger.networkDebug) Logger.i(this.mTag + " Data is an OBJECT_ARRAY");
            if (mType.base().length > 0)
            {
                jo = jo.optJSONObject("data");
                for (final String b : mType.base())
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
                                return new JSONResponse<IReturnable>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original
                                        .optString("message", "Unknown Response Status"));
                            }
                            ja = jo.optJSONArray(b);
                        }
                        if (ja == null)
                        {
                            if (jo == null)
                            {
                                if (Logger.networkDebug) Logger.w(this.mTag + " 'data' element is null, sending error codes");
                                return new JSONResponse<IReturnable>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original
                                        .optString("message", "Unknown Response Status"));
                            }
                            final JSONObject tryObj = jo.optJSONObject(b);
                            if (tryObj != null && !hasType(mType.type(), Type.JSONDataType.ARRAY)
                                    && hasType(mType.type(), Type.JSONDataType.OBJECT))
                            {
                                if (Logger.networkDebug) Logger.i("%1$s - Opting single object at base ' %2$s", this.mTag, b + "'");
                                try
                                {
                                    ja = new JSONArray("[" + jo.toString() + "]");
                                }
                                catch (JSONException e)
                                {
                                    if (Logger.networkDebug) Logger.e("Exception caught in JSONTaskImpl.backgroundTask", e);
                                }
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
                if (!hasType(mType.type(), Type.JSONDataType.ARRAY) && !hasType(mType.type(), Type.JSONDataType.OBJECT))
                {
                    return new JSONResponse<IReturnable>(null, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE),
                            original.optString("message", "Unknown Response Status"));
                }
            }
            else
            {
                if (Logger.networkDebug) Logger.i("%1$s - Sending  %2$s", this.mTag, ja.length() + " objects");
            }
        }
        
        if (ja != null)
        {
            JSONObject o;
            String type = null;
            if (original.has("type"))
            {
                try
                {
                    type = original.getString("type");
                }
                catch (JSONException e)
                {
                }
            }
            Class<IReturnable> targetClass;
            IReturnable target;
            IParser<IReturnable> parser;
            for (int i = 0; i < ja.length(); i++)
            {
                try
                {
                    o = ja.getJSONObject(i);
                    if (type == null && !o.has("type"))
                    {
                        if (Logger.networkDebug) Logger.e("%1$s - Skipping JSON object. Could not determine type for  %2$s", this.mTag, o);
                        continue;
                    }
                    else if (o.has("type"))
                    {
                        type = o.getString("type");
                    }
                    targetClass = mRegistry.getTargetClass(type);
                    if (targetClass == null)
                    {
                        if (Logger.networkDebug) Logger.e("%1$s - Skipping JSON object. Could not determine class for  %2$s", this.mTag, type);
                        continue;
                    }
                    target = targetClass.newInstance();
                    parser = (IParser<IReturnable>) target.getParser();
                    if (parser.parse(o, target))
                    {
                        data.add(target);
                        if (Logger.networkDebug) Logger.w("parsed data as %s", targetClass.getSimpleName());
                    }
                }
                catch (Exception e)
                {
                    if (Logger.networkDebug) Logger.e("Exception caught in JSONTaskImpl.backgroundTask", e);
                }
            }
            return new JSONResponse<IReturnable>(data, 200, "Data parsed");
        }
        return new JSONResponse<IReturnable>(data, original.optInt("http_code", JSONResponse.CODE_UNKNOWN_HTTP_CODE), original.optString("message", "Unknown Response Status"));
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
    
    private void createLogTag()
    {
        this.mTag = TAG + "-" + (++sCounter) + " - " + mType.name();
    }
}
