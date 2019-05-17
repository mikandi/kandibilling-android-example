package com.tenta.easylogger;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Locale;

public class EasyLogger
{
    public static boolean isDebug = false;
    
    /**
     * log entry format where the caller class' name is the tag
     * (1: thread name, 2: method name, 3: log message)
     */
    private static final String LOG_FORMAT = "[%1$6s] %2$-30s %3$s";
    /**
     * log entry format for custom tags where the caller class' name is part of the log message
     * (1: caller, 2: thread name, 3: method name, 4: log message)
     */
    private static final String LOG_FORMAT_TAGGED = "%1$s [%2$6s] %3$-30s %4$s";
    /**
     * caller formatter (classname + linenumber)
     * (1: classname, 2: linenumber)
     */
    private static final String CALLER_FILE_FORMAT = "(%s";
    private static final String CALLER_LINENUMBER_FORMAT = "%d)";
    private static final String CALLER_FORMAT = "%1$38s:%2$-6s:"; // necessary colon at the end to prevent trimming :|
    
    private static final String THREAD_NAME_GENERIC = "Thread-";
    private static final String THREAD_NAME_ASYNCTASK = "AsyncTask #";
    private static final int THREAD_NAME_MAX = 6;
    
    /// different approach for logging:
    
    /**
     * enum for the various log levels to apply
     */
    private enum Level
    {
        /**
         * Verbose
         */
        V,
        /**
         * Debug
         */
        D,
        /**
         * Information
         */
        I,
        /**
         * Warning
         */
        W,
        /**
         * Error
         */
        E
    }
    
    /**
     * Logs the passed message with the arguments and provided level and eventual stack trace for the throwable,
     * if there is any at the end of the format args.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG = callerClassName   methodName    message</code></pre>
     *
     * @param level      the log level in which to print the log
     * @param message    the message to print
     * @param formatArgs the optional list of format args and an optional throwable at the end of the list.
     */
    public static void log(@NonNull final Level level, @Nullable String tag, @Nullable final Object message,
                           @Nullable Object... formatArgs)
    {
        String wrappedMessage;
        if (message == null)
        {
            // support null message with format args, building the message by concatenating the provided arguments:
            final StringBuilder sb = new StringBuilder();
            if (formatArgs != null && formatArgs.length > 0)
            {
                // print all the format args (including the eventual last throwable) if no message was provided
                for (int i = 0; i < formatArgs.length; i++)
                {
                    if (i > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append("%").append((i + 1)).append("$s");
                }
            }
            wrappedMessage = sb.toString();
        }
        else
        {
            wrappedMessage = message.toString().trim();
        }
        final Throwable exception;
        if (formatArgs == null || formatArgs.length <= 0)
        {
            exception = null;
        }
        else
        {
            if (formatArgs[formatArgs.length - 1] instanceof Throwable)
            {
                exception = (Throwable) formatArgs[formatArgs.length - 1];
            }
            else
            {
                exception = null;
            }
        }
        try
        {
            final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            boolean foundLogger = false;
            int start = 0;
            if (stackTraceElements.length > start)
            {
                for (StackTraceElement element : stackTraceElements)
                {
                    if (!foundLogger)
                    {
                        foundLogger = element.getClassName().equals(EasyLogger.class.getName());
                        continue;
                    }
                    if (!element.getClassName().equals(EasyLogger.class.getName()))
                    {
                        final String caller = String.format(CALLER_FORMAT,
                                String.format(CALLER_FILE_FORMAT, element.getFileName()),
                                String.format(Locale.US, CALLER_LINENUMBER_FORMAT, element.getLineNumber()));
                        final String threadName = getShortThreadName();
                        if (tag == null)
                        {
                            tag = caller;
                            wrappedMessage = String.format(LOG_FORMAT, threadName, element.getMethodName(),
                                    String.format(wrappedMessage, formatArgs));
                        }
                        else
                        {
                            wrappedMessage = String.format(LOG_FORMAT_TAGGED, caller, threadName, element.getMethodName(),
                                    String.format(wrappedMessage, formatArgs));
                        }
                        break;
                    }
                }
            }
            switch (level)
            {
                case V:
                    Log.v(tag, wrappedMessage, exception);
                    break;
                case I:
                    Log.i(tag, wrappedMessage, exception);
                    break;
                case W:
                    Log.w(tag, wrappedMessage, exception);
                    break;
                case E:
                    Log.e(tag, wrappedMessage, exception);
                    break;
                case D:
                default:
                    Log.d(tag, wrappedMessage, exception);
                    break;
            }
        }
        catch (Exception e)
        {
            if (isDebug) EasyLogger.e("error in EasyLogger.log", e);
            if (isDebug) EasyLogger.e("original message was: %1$s", wrappedMessage, exception);
        }
    }
    
    /**
     * Returns the shortened name of the current thread, which is no longer than {@link #THREAD_NAME_MAX}.
     * <p>
     * Thread-99999 gets shortened to 999999.
     * AsyncTask #999 gets shortened to a#999.
     * <p>
     * Anything longer than {@link #THREAD_NAME_MAX} gets ellipsized with a '.' in the middle.
     *
     * @return shortened thread name
     */
    private static String getShortThreadName()
    {
        final String fullName = Thread.currentThread().getName();
        final String shortName;
        if (fullName.startsWith(THREAD_NAME_GENERIC))
        {
            shortName = fullName.substring(THREAD_NAME_GENERIC.length());
        }
        else if (fullName.startsWith(THREAD_NAME_ASYNCTASK))
        {
            shortName = "a#" + fullName.substring(THREAD_NAME_ASYNCTASK.length());
        }
        else
        {
            shortName = fullName;
        }
        final int len = shortName.length();
        if (len <= THREAD_NAME_MAX)
        {
            return shortName;
        }
        else
        {
            final int startLen = (THREAD_NAME_MAX - 1) / 2;
            final int endLen = (THREAD_NAME_MAX - 1) - startLen;
            return shortName.substring(0, startLen) + "." + shortName.substring(len - endLen, len);
        }
    }
    
    /**
     * Shortcut for <b>verbose</b> logging the current execution point.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    </code></pre>
     *
     * @param throwable eventual exception
     */
    public static void v(final Throwable... throwable)
    {
        log(Level.V, null, null, (Object[]) throwable);
    }
    
    /**
     * Shortcut for <b>debug</b> logging the current execution point.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    </code></pre>
     *
     * @param throwable eventual exception
     */
    public static void d(final Throwable... throwable)
    {
        log(Level.D, null, null, (Object[]) throwable);
    }
    
    /**
     * Shortcut for <b>info</b> logging the current execution point.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    </code></pre>
     *
     * @param throwable eventual exception
     */
    public static void i(final Throwable... throwable)
    {
        log(Level.I, null, null, (Object[]) throwable);
    }
    
    /**
     * Shortcut for <b>warn</b> logging the current execution point.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName   </code></pre>
     *
     * @param throwable eventual exception
     */
    public static void w(final Throwable... throwable)
    {
        log(Level.W, null, null, (Object[]) throwable);
    }
    
    /**
     * Shortcut for <b>error</b> logging the current execution point.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName   </code></pre>
     *
     * @param throwable eventual exception
     */
    public static void e(final Throwable... throwable)
    {
        log(Level.E, null, null, (Object[]) throwable);
    }
    
    /**
     * Shortcut for <b>verbose</b> logging.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    message</code></pre>
     *
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     */
    public static void v(@Nullable final Object message, final Object... formatArgs)
    {
        log(Level.V, null, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>debug</b> logging.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    message</code></pre>
     *
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     */
    public static void d(@Nullable final Object message, final Object... formatArgs)
    {
        log(Level.D, null, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>info</b> logging.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    message</code></pre>
     *
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     */
    public static void i(@Nullable final Object message, final Object... formatArgs)
    {
        log(Level.I, null, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>warn</b> logging.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    message</code></pre>
     *
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     */
    public static void w(@Nullable final Object message, final Object... formatArgs)
    {
        log(Level.W, null, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>error</b> logging.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>TAG =  (callerClassFile:lineNumber):   methodName    message</code></pre>
     *
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     */
    public static void e(@Nullable final Object message, final Object... formatArgs)
    {
        log(Level.E, null, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>verbose</b> logging with custom tag.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>tag    (callerClassFile:lineNumber)  methodName     message</code></pre>
     *
     * @param tag the tag of the log message (<code>null</code> for className tag)
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     *
     * @see #v(Object, Object...)
     */
    public static void vt(@Nullable String tag, @Nullable final Object message, final Object... formatArgs)
    {
        log(Level.V, tag, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>debug</b> logging with custom tag.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>tag    (callerClassFile:lineNumber)  methodName     message</code></pre>
     *
     * @param tag the tag of the log message (<code>null</code> for className tag)
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     *
     * @see #d(Object, Object...)
     */
    public static void dt(@Nullable String tag, @Nullable final Object message, final Object... formatArgs)
    {
        log(Level.D, tag, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>info</b> logging with custom tag.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>tag    (callerClassFile:lineNumber)  methodName     message</code></pre>
     *
     * @param tag the tag of the log message (<code>null</code> for className tag)
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     *
     * @see #i(Object, Object...)
     */
    public static void it(@Nullable String tag, @Nullable final Object message, final Object... formatArgs)
    {
        log(Level.I, tag, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>warn</b> logging with custom tag.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>tag    (callerClassFile:lineNumber)  methodName     message</code></pre>
     *
     * @param tag the tag of the log message (<code>null</code> for className tag)
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     *
     * @see #w(Object, Object...)
     */
    public static void wt(@Nullable String tag, @Nullable final Object message, final Object... formatArgs)
    {
        log(Level.W, tag, message, formatArgs);
    }
    
    /**
     * Shortcut for <b>error</b> logging with custom tag.
     * <br />
     * The log will be formatted as follows:
     * <pre><code>tag    (callerClassFile:lineNumber)  methodName     message</code></pre>
     *
     * @param tag the tag of the log message (<code>null</code> for className tag)
     * @param message the message to log (may contain format placeholders)
     * @param formatArgs optional format arguments for the message, with eventual trailing exception
     *
     * @see #e(Object, Object...)
     */
    public static void et(@Nullable String tag, @Nullable final Object message, final Object... formatArgs)
    {
        log(Level.E, tag, message, formatArgs);
    }
}
