package com.saguarodigital.returnable.defaultimpl;

import com.saguarodigital.returnable.IParser;
import com.saguarodigital.returnable.IReturnable;
import com.saguarodigital.returnable.Logger;
import com.saguarodigital.returnable.annotation.Field;
import com.saguarodigital.returnable.annotation.Type;
import com.saguarodigital.returnable.exception.MissingDataException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AutoParser<T extends IReturnable> implements IParser<T>
{
    
    @SuppressWarnings("unchecked")
    @Override
    public <E> boolean parse(JSONObject jo, E empty)
    {
        if (Logger.parserDebug) Logger.i("Running auto parser for " + empty.getClass().getSimpleName());
        if (jo == null)
        {
            if (Logger.parserDebug) Logger.w("Input object is null, bailing");
            return false;
        }
        if (Logger.parserDebug) Logger.i("JSON Data is: " + jo.toString());
        Type cT = empty.getClass().getAnnotation(Type.class);
        if (cT.base().length > 0)
        {
            for (final String b : cT.base())
            {
                if (Logger.parserDebug) Logger.i("Base '" + b + "' requested");
                if (jo.has(b))
                {
                    if (Logger.parserDebug) Logger.i("Recursing down to parse base '" + b + "'");
                    return parse(jo.optJSONObject(b), empty);
                }
            }
            if (Logger.parserDebug) Logger.i("Base not detected, continuing");
        }
        java.lang.reflect.Field[] flds = empty.getClass().getDeclaredFields();
        if (Logger.parserDebug) Logger.i(empty.getClass().getSimpleName() + " has " + flds.length + " fields");
        List<java.lang.reflect.Field> fields = new ArrayList<java.lang.reflect.Field>(flds.length);
        for (final java.lang.reflect.Field f : flds)
        {
            Field fl = f.getAnnotation(Field.class);
            if (Logger.parserDebug) Logger.i("Checking if " + f.getName() + " has a Field annotation: " + (fl == null ? "false" : "true"));
            if (fl != null)
            {
                fields.add(f);
            }
        }
        for (java.lang.reflect.Field f : fields)
        {
            if (Logger.parserDebug) Logger.i("Parsing field " + f.getName());
            f.setAccessible(true);
            Field rF = f.getAnnotation(Field.class);
            boolean canBeEmpty = rF.constraint() == Field.Constraint.NONE;
            String name = rF.json_name();
            if (name.equals(""))
            {
                name = f.getName();
                if (name.startsWith("m"))
                {
                    name = name.substring(1);
                }
                name = name.toLowerCase();
            }
            if (Logger.parserDebug) Logger.i("Looking for JSON field named " + name);
            if (rF.type() == Field.Type.TEXT)
            {
                String val = jo.optString(name, null);
                if (val == null && !canBeEmpty)
                {
                    throw new MissingDataException(name);
                }
                try
                {
                    f.set(empty, val);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be set");
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be accessed");
                }
            }
            else if (rF.type() == Field.Type.NUMBER)
            {
                Integer val = null;
                try
                {
                    val = jo.getInt(name);
                }
                catch (JSONException ex) {}
                if (val == null && !canBeEmpty)
                {
                    throw new MissingDataException(name);
                }
                try
                {
                    f.set(empty, val == null ? 0 : val);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be set");
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be accessed");
                }
            }
            else if (rF.type() == Field.Type.BOOLEAN)
            {
                Boolean val = null;
                try
                {
                    val = jo.getBoolean(name);
                }
                catch (JSONException ex) {}
                if (val == null && !canBeEmpty)
                {
                    throw new MissingDataException(name);
                }
                try
                {
                    f.set(empty, val == null ? false : val);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be set");
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be accessed");
                }
            }
            else if (rF.type() == Field.Type.OBJECT)
            {
                if (Logger.parserDebug) Logger.i("Parsing immediate object of type " + f.getType().getSimpleName());
                JSONObject childObject = jo.optJSONObject(name);
                if (childObject == null)
                {
                    if (!canBeEmpty)
                    {
                        throw new RuntimeException("Required field '" + name + "' was not found");
                    }
                    else
                    {
                        try
                        {
                            f.set(empty, null);
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new RuntimeException("Field '" + name + "' could not be set");
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException("Field '" + name + "' could not be accessed");
                        }
                        continue;
                    }
                }
                try
                {
                    f.getType().getAnnotation(Type.class);
                }
                catch (NullPointerException ex)
                {
                    throw new RuntimeException("Immediate object " + rF.getClass().getSimpleName() + " is not a marked type");
                }
                Class<?> cz = f.getType();
                boolean isReturnable = false;
                do
                {
                    Class<?>[] ifaces = cz.getInterfaces();
                    for (Class<?> c : ifaces)
                    {
                        if (Logger.parserDebug) Logger.i("Immediate object implements " + c.getCanonicalName());
                        if (c.equals(IReturnable.class))
                        {
                            isReturnable = true;
                            break;
                        }
                    }
                    if (isReturnable)
                    {
                        break;
                    }
                }
                while ((cz = cz.getSuperclass()) != null);
                if (!isReturnable)
                {
                    throw new RuntimeException("Immediate object " + f.getType().getSimpleName() + " is not returnable");
                }
                IReturnable prototype = null;
                try
                {
                    prototype = f.getType().asSubclass(IReturnable.class).newInstance();
                }
                catch (IllegalAccessException e)
                {
                    if (Logger.parserDebug) Logger.e("IllegalAccessException instantiating immediate object");
                }
                catch (InstantiationException e)
                {
                    if (Logger.parserDebug) Logger.e("InstantiationException instantiating immediate object");
                }
                IParser<? extends IReturnable> pr = prototype.getParser();
                IReturnable outputObject = null;
                try
                {
                    outputObject = (IReturnable) f.getType().newInstance();
                }
                catch (IllegalAccessException e)
                {
                    if (Logger.parserDebug) Logger.e("IllegalAccessException instantiating immediate object");
                }
                catch (InstantiationException e)
                {
                    if (Logger.parserDebug) Logger.e("InstantiationException instantiating immediate object");
                    if (Logger.parserDebug) Logger.e("Does the returnable contain a 0 argument constructor?");
                }
                if (Logger.parserDebug) Logger.i("Parsing with outputObject of type " + outputObject.getClass().getSimpleName());
                pr.parse(childObject, outputObject);
                try
                {
                    f.set(empty, outputObject);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be set");
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Field '" + name + "' could not be accessed");
                }
            }
            else if (rF.type() == Field.Type.LIST)
            {
                if (Logger.parserDebug) Logger.i("Parsing as a list");
                boolean primative = true;
                try
                {
                    primative = rF.generic_type().newInstance() instanceof Field.PrimativeListType;
                }
                catch (InstantiationException e) {}
                catch (IllegalAccessException e) {}
                if (!primative)
                {
                    if (Logger.parserDebug) Logger.i("List type declares " + rF.generic_type().getSimpleName());
                    try
                    {
                        Object obj = rF.generic_type().newInstance();
                        if (!(obj instanceof IReturnable))
                        {
                            throw new RuntimeException("Unable to parse a list of type " + rF.generic_type().getSimpleName() + " as it does not descend from IReturnable");
                        }
                        if (Logger.parserDebug) Logger.i("Parsing list of type " + obj.getClass().getSimpleName());
                        JSONArray ja = jo.optJSONArray(name);
                        if (ja == null)
                        {
                            if (Logger.parserDebug) Logger.w("List " + name + " was not found");
                            continue;
                        }
                        @SuppressWarnings("rawtypes")
                        List l = new ArrayList(ja.length());
                        @SuppressWarnings("rawtypes")
                        final IParser par = ((IReturnable) obj).getParser();
                        for (int x = 0; x < ja.length(); x += 1)
                        {
                            final Object parsed = rF.generic_type().asSubclass(IReturnable.class).newInstance();
                            if (par.parse(ja.optJSONObject(x), parsed))
                            {
                                if (Logger.parserDebug) Logger.i("Adding parsed object of type " + parsed.getClass().getSimpleName() + " to list at position " + x);
                                l.add(parsed);
                            }
                            else
                            {
                                if (Logger.parserDebug) Logger.w("Unable to parse object at list position " + x);
                            }
                        }
                        if (Logger.parserDebug) Logger.i("Setting list with " + l.size() + " items to " + f.getName());
                        f.set(empty, l);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException("Class " + rF.generic_type().getSimpleName() + " could not be accessed");
                    }
                    catch (InstantiationException e)
                    {
                        throw new RuntimeException("Class " + rF.generic_type()
                                                                .getSimpleName() + " could not be instantiated. Is your inner type publicly instantiable and has a no-args constructor?");
                    }
                }
                else
                {
                    // TODO: Examine which specific type of Field.PrimativeListType is in existence
                    // The list has no declared type, so it is interpreted as a list of primitives
                    if (Logger.parserDebug) Logger.i("Parsing a list of primatives");
                    JSONArray ja = jo.optJSONArray(name);
                    if (ja == null)
                    {
                        // TODO: Figure our how to properly turn debugging on and off in a static context
                        if (Logger.parserDebug) Logger.w("List " + name + " was not found");
                        continue;
                    }
                    @SuppressWarnings("rawtypes")
                    List l = new ArrayList();
                    for (int i = 0; i < ja.length(); ++i)
                    {
                        l.add(ja.opt(i));
                    }
                    try
                    {
                        f.set(empty, l);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new RuntimeException("Field '" + name + "' could not be set");
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException("Field '" + name + "' could not be accessed");
                    }
                }
            }
        }
        return true;
    }
}
