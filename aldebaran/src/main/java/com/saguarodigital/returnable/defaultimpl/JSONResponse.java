package com.saguarodigital.returnable.defaultimpl;

import java.util.List;

public class JSONResponse<E>
{
    public static final int CODE_CACHED_DATA = -0x01;
    public static final int CODE_UNKNOWN_HTTP_CODE = -0x02;
    private List<E> mElements;
    private int mResponseCode;
    private String mResponseMessage;
    
    public JSONResponse(List<E> elements, int code, String message)
    {
        this.mElements = elements;
        this.mResponseCode = code;
        this.mResponseMessage = message;
    }
    
    public String getMessage()
    {
        return this.mResponseMessage;
    }
    
    public int getCode()
    {
        return this.mResponseCode;
    }
    
    public List<E> getAll()
    {
        return this.mElements;
    }
    
    public E getOne()
    {
        return this.mElements != null && this.mElements.size() > 0 ? this.mElements.get(0) : null;
    }
}
