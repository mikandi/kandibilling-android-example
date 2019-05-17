package com.saguarodigital.returnable.exception;

public class MissingDataException extends RuntimeException
{
    private static final long serialVersionUID = -7505122774182658750L;
    
    public MissingDataException(final String name)
    {
        super("The required field '" + name + "' could not be found.");
    }
}
