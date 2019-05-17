EasyLogger
==========
Prints pretty log messages with link to the source where you are logging.

Tips
----
1. Extend `EasyLogger` for your convenience, and declare debug case flags you want to use;
2. Use logging inside one of your debug flags' `if` clause, to make sure no logs will leak into production unintended; 
3. Use formatting arguments to minimize the number of `String` literals;
4. Take advantage of _Android Studio_'s live template feature, and create your shortcuts for logging;

Usage
-----
*`EasyLogger` extension*
---------------------
```
public class Logger extends EasyLogger
{
    public static boolean flowDebug = BuildConfid.ALPHA_BUILD;
    public static boolean serviceDebug = false;
}
```

*Call convention*
-----------------
* log values in a predefined format: 
```
if (Logger.flowDebug) Logger.d("new size: %1$d x %2$d", width, height);
```
* log the values of the formatting arguments (leaving the format `null`): 
```
final Object[] values = new Object[] 
{
    // TODO: populate
};
if (Logger.flowDebug) Logger.d(null, values);
```

