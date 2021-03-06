package com.blazemeter.jmeter.citrix.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinRegistry.class);
    private static final int REG_SUCCESS = 0;
    public static final int REG_NOTFOUND = 2;
    private static final int KEY_READ = 0x20019;
    public static final int REG_ACCESSDENIED = 5;
    private static final int KEY_ALL_ACCESS = 0xf003f;
    public static final int HKEY_CLASSES_ROOT = 0x80000000;
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    private static final String CLASSES_ROOT = "HKEY_CLASSES_ROOT";
    private static final String CURRENT_USER = "HKEY_CURRENT_USER";
    private static final String LOCAL_MACHINE = "HKEY_LOCAL_MACHINE";
    private static Preferences userRoot = Preferences.userRoot();
    private static Preferences systemRoot = Preferences.systemRoot();
    private static Class<? extends Preferences> userClass = userRoot.getClass();
    private static Method regOpenKey = null;
    private static Method regCloseKey = null;
    private static Method regQueryValueEx = null;
    private static Method regEnumValue = null;
    private static Method regQueryInfoKey = null;
    private static Method regEnumKeyEx = null;
    private static Method regCreateKeyEx = null;
    private static Method regSetValueEx = null;
    private static Method regDeleteKey = null;
    private static Method regDeleteValue = null;

    static {
        try {
            regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey",
                    new Class[] { int.class, byte[].class, int.class });
            regOpenKey.setAccessible(true);
            regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class });
            regCloseKey.setAccessible(true);
            regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx",
                    new Class[] { int.class, byte[].class });
            regQueryValueEx.setAccessible(true);
            regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue",
                    new Class[] { int.class, int.class, int.class });
            regEnumValue.setAccessible(true);
            regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", new Class[] { int.class });
            regQueryInfoKey.setAccessible(true);
            regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx",
                    new Class[] { int.class, int.class, int.class });
            regEnumKeyEx.setAccessible(true);
            regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx",
                    new Class[] { int.class, byte[].class });
            regCreateKeyEx.setAccessible(true);
            regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx",
                    new Class[] { int.class, byte[].class, byte[].class });
            regSetValueEx.setAccessible(true);
            regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue",
                    new Class[] { int.class, byte[].class });
            regDeleteValue.setAccessible(true);
            regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[] { int.class, byte[].class });
            regDeleteKey.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Error binding methods", e);
        }
    }

    private WinRegistry() {
        super();
    }

    /**
     * Reads value for the key from given path
     * 
     * @param hkey 
     *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Path to key
     * @param key Key name
     * @return String value
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     * @throws IOException IO Exception
     */
    public static String valueForKey(int hkey, String path, String key)
            throws IllegalAccessException, InvocationTargetException, IOException {
        if (hkey == HKEY_LOCAL_MACHINE)
            return valueForKey(systemRoot, hkey, path, key);
        else if (hkey == HKEY_CURRENT_USER)
            return valueForKey(userRoot, hkey, path, key);
        else
            return valueForKey(null, hkey, path, key);
    }

    /**
     * Reads all key(s) and value(s) from given path
     * 
     * @param hkey
     *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Path to key
     * @return the map of key(s) and corresponding value(s)
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     * @throws IOException IO Exception
     */
    public static Map<String, String> valuesForPath(int hkey, String path)
            throws IllegalAccessException, InvocationTargetException, IOException {
        if (hkey == HKEY_LOCAL_MACHINE)
            return valuesForPath(systemRoot, hkey, path);
        else if (hkey == HKEY_CURRENT_USER)
            return valuesForPath(userRoot, hkey, path);
        else
            return valuesForPath(null, hkey, path);
    }

    /**
     * Read all the subkey(s) from a given path
     * 
     * @param hkey
     *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Path to key
     * @return the subkey(s) list
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    public static List<String> subKeysForPath(int hkey, String path)
            throws IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE)
            return subKeysForPath(systemRoot, hkey, path);
        else if (hkey == HKEY_CURRENT_USER)
            return subKeysForPath(userRoot, hkey, path);
        else
            return subKeysForPath(null, hkey, path);
    }

    /**
     * Create a key
     * 
     * @param hkey
     *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key Key name
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    public static void createKey(int hkey, String key)
            throws IllegalAccessException, InvocationTargetException {
        int[] ret;
        if (hkey == HKEY_LOCAL_MACHINE) {
            ret = createKey(systemRoot, hkey, key);
            regCloseKey.invoke(systemRoot, Integer.valueOf(ret[0]));
        } else if (hkey == HKEY_CURRENT_USER) {
            ret = createKey(userRoot, hkey, key);
            regCloseKey.invoke(userRoot, Integer.valueOf(ret[0]));
        } else
            throw new IllegalArgumentException("hkey=" + hkey);
        if (ret[1] != REG_SUCCESS)
            throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
    }

    /**
     * Write a value in a given key/value name
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key path
     * @param valueName value
     * @param value Value to set
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    public static void writeStringValue(int hkey, String key, String valueName, String value)
            throws IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE)
            writeStringValue(systemRoot, hkey, key, valueName, value);
        else if (hkey == HKEY_CURRENT_USER)
            writeStringValue(userRoot, hkey, key, valueName, value);
        else
            throw new IllegalArgumentException("hkey=" + hkey);
    }

    /**
     * Delete a given key
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key key name
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    public static void deleteKey(int hkey, String key)
            throws IllegalAccessException, InvocationTargetException {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE)
            rc = deleteKey(systemRoot, hkey, key);
        else if (hkey == HKEY_CURRENT_USER)
            rc = deleteKey(userRoot, hkey, key);
        if (rc != REG_SUCCESS)
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
    }

    /**
     * delete a value from a given key/value name
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key Key name
     * @param value Value of key
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    public static void deleteValue(int hkey, String key, String value)
            throws IllegalAccessException, InvocationTargetException {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE)
            rc = deleteValue(systemRoot, hkey, key, value);
        else if (hkey == HKEY_CURRENT_USER)
            rc = deleteValue(userRoot, hkey, key, value);
        if (rc != REG_SUCCESS)
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
    }

    // =====================

    private static int deleteValue(Preferences root, int hkey, String key, String value)
            throws IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(root,
                Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_ALL_ACCESS));
        if (handles[1] != REG_SUCCESS)
            return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
        int rc = ((Integer) regDeleteValue.invoke(root, Integer.valueOf(handles[0]), toCstr(value)))
                .intValue();
        regCloseKey.invoke(root, Integer.valueOf(handles[0]));
        return rc;
    }

    private static int deleteKey(Preferences root, int hkey, String key)
            throws IllegalAccessException, InvocationTargetException {
        return ((Integer) regDeleteKey.invoke(root, Integer.valueOf(hkey), toCstr(key))).intValue(); // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
    }

    private static String valueForKey(Preferences root, int hkey, String path, String key)
            throws IllegalAccessException, InvocationTargetException, IOException {
        int[] handles = (int[]) regOpenKey.invoke(root,
                Integer.valueOf(hkey), toCstr(path), Integer.valueOf(KEY_READ));
        if (handles[1] != REG_SUCCESS)
            throw new IllegalArgumentException(
                    "The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
        byte[] valb = (byte[]) regQueryValueEx.invoke(root, Integer.valueOf(handles[0]), toCstr(key));
        regCloseKey.invoke(root, Integer.valueOf(handles[0]));
        return (valb != null ? parseValue(valb) : queryValueForKey(hkey, path, key));
    }

    private static String queryValueForKey(int hkey, String path, String key) throws IOException {
        return queryValuesForPath(hkey, path).get(key);
    }

    private static Map<String, String> valuesForPath(Preferences root, int hkey, String path)
            throws IllegalAccessException, InvocationTargetException, IOException {
        HashMap<String, String> results = new HashMap<>();
        int[] handles = (int[]) regOpenKey.invoke(root,
                Integer.valueOf(hkey), toCstr(path), Integer.valueOf(KEY_READ));
        if (handles[1] != REG_SUCCESS)
            throw new IllegalArgumentException(
                    "The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
        int[] info = (int[]) regQueryInfoKey.invoke(root, Integer.valueOf(handles[0]));
        int count = info[2]; // Fixed: info[0] was being used here
        int maxlen = info[4]; // while info[3] was being used here, causing
                              // wrong results
        for (int index = 0; index < count; index++) {
            byte[] valb = (byte[]) regEnumValue.invoke(root,
                    Integer.valueOf(handles[0]), Integer.valueOf(index), Integer.valueOf(maxlen + 1));
            String vald = parseValue(valb);
            if (valb == null || vald.isEmpty())
                return queryValuesForPath(hkey, path);
            results.put(vald, valueForKey(root, hkey, path, vald));
        }
        regCloseKey.invoke(root, Integer.valueOf(handles[0]));
        return results;
    }

    /**
     * Searches recursively into the path to find the value for key. This method
     * gives only first occurrence value of the key. If required to get all
     * values in the path recursively for this key, then
     * {@link #valuesForKeyPath(int hkey, String path, String key)} should be
     * used.
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Key Path 
     * @param key key name
     * @return the value of given key obtained recursively
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     * @throws IOException IO Exception
     */
    public static String valueForKeyPath(int hkey, String path, String key)
            throws IllegalAccessException, InvocationTargetException, IOException {
        String val;
        try {
            val = valuesForKeyPath(hkey, path, key).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The system can not find the key: '" + key + "' after "
                    + "searching the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
        }
        return val;
    }

    /**
     * Searches recursively into given path for particular key and stores
     * obtained value in list
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Key Path 
     * @param key key name
     * @return List of values
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     * @throws IOException IO Exception
     */
    public static List<String> valuesForKeyPath(int hkey, String path, String key)
            throws IllegalAccessException, InvocationTargetException, IOException {
        List<String> list = new ArrayList<>();
        if (hkey == HKEY_LOCAL_MACHINE)
            return valuesForKeyPath(systemRoot, hkey, path, key, list);
        else if (hkey == HKEY_CURRENT_USER)
            return valuesForKeyPath(userRoot, hkey, path, key, list);
        else
            return valuesForKeyPath(null, hkey, path, key, list);
    }

    private static List<String> valuesForKeyPath(Preferences root, int hkey, String path, String key, List<String> list)
            throws IllegalAccessException, InvocationTargetException, IOException {
        if (!isDirectory(root, hkey, path)) {
            takeValueInListForKey(hkey, path, key, list);
        } else {
            List<String> subKeys = subKeysForPath(root, hkey, path);
            for (String subkey : subKeys) {
                String newPath = path + "\\" + subkey; // NOSONAR
                if (isDirectory(root, hkey, newPath))
                    valuesForKeyPath(root, hkey, newPath, key, list);
                takeValueInListForKey(hkey, newPath, key, list);
            }
        }
        return list;
    }

    /**
     * Takes value for key in list
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path path to keu
     * @param key key name
     * @param list list to which value is added
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     * @throws IOException IO Exception
     */
    private static void takeValueInListForKey(int hkey, String path, String key, List<String> list)
            throws IllegalAccessException, InvocationTargetException, IOException {
        String value = valueForKey(hkey, path, key);
        if (value != null) {
            list.add(value);
        }
    }

    /**
     * Checks if the path has more subkeys or not
     * 
     * @param root
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path path to key
     * @return true if path has subkeys otherwise false
     * @throws IllegalAccessException If access is not allowed
     * @throws InvocationTargetException if method invocation threw exception
     */
    private static boolean isDirectory(Preferences root, int hkey, String path)
            throws IllegalAccessException, InvocationTargetException {
        return !subKeysForPath(root, hkey, path).isEmpty();
    }

    private static List<String> subKeysForPath(Preferences root, int hkey, String path)
            throws IllegalAccessException, InvocationTargetException {
        List<String> results = new ArrayList<>();
        int[] handles = (int[]) regOpenKey.invoke(root,
                Integer.valueOf(hkey), toCstr(path), Integer.valueOf(KEY_READ));
        if (handles[1] != REG_SUCCESS)
            throw new IllegalArgumentException(
                    "The system can not find the specified path: '" + getParentKey(hkey) + "\\" + path + "'");
        int[] info = (int[]) regQueryInfoKey.invoke(root, Integer.valueOf(handles[0]));
        int count = info[0]; // Fix: info[2] was being used here with wrong
                             // results. Suggested by davenpcj, confirmed by
                             // Petrucio
        int maxlen = info[3]; // value length max
        for (int index = 0; index < count; index++) {
            byte[] valb = (byte[]) regEnumKeyEx.invoke(root,
                    Integer.valueOf(handles[0]), Integer.valueOf(index), Integer.valueOf(maxlen + 1));
            results.add(parseValue(valb));
        }
        regCloseKey.invoke(root, Integer.valueOf(handles[0]));
        return results;
    }

    private static int[] createKey(Preferences root, int hkey, String key)
            throws IllegalAccessException, InvocationTargetException {
        return (int[]) regCreateKeyEx.invoke(root, Integer.valueOf(hkey), toCstr(key));
    }

    private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
            throws IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(root,
                Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_ALL_ACCESS));
        Object o = regSetValueEx.invoke(root, Integer.valueOf(handles[0]), toCstr(valueName), toCstr(value));
        LOGGER.info("writeStringValue returned {}", o);
        if(!"0".equals(o.toString())) {
            throw new IllegalStateException("Error setting regex, return code:"+o);
        } else {
            o = regCloseKey.invoke(root, Integer.valueOf(handles[0]));
            LOGGER.info("writeStringValue closeKey returned {}", o);
            if(!"0".equals(o.toString())) {
                throw new IllegalStateException("Error closing regex, return code:"+o);
            }
        }
    }

    /**
     * Makes cmd query for the given hkey and path then executes the query
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param path Path to key
     * @return the map containing all results in form of key(s) and value(s)
     *         obtained by executing query
     * @throws IOException
     */
    private static Map<String, String> queryValuesForPath(int hkey, String path) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        Map<String, String> map = new HashMap<>();
        Process process = Runtime.getRuntime().exec("reg query \"" + getParentKey(hkey) + "\\" + path + "\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
            if (!line.contains("REG_")) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line, " \t");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.startsWith("REG_")) {
                    builder.append("\t ");
                }
                else {
                    builder.append(token).append(" ");
                }
            }
            String[] arr = builder.toString().split("\t");
            map.put(arr[0].trim(), arr[1].trim());
            builder.setLength(0);
        }
        return map;
    }

    /**
     * Determines the string equivalent of hkey
     * 
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE 
     * @return string equivalent of hkey
     */
    private static String getParentKey(int hkey) {
        if (hkey == HKEY_CLASSES_ROOT)
            return CLASSES_ROOT;
        else if (hkey == HKEY_CURRENT_USER)
            return CURRENT_USER;
        else if (hkey == HKEY_LOCAL_MACHINE)
            return LOCAL_MACHINE;
        return null;
    }

    /**
     * Intern method which adds the trailing \0 for the handle with java.dll
     * 
     * @param str
     *            String
     * @return byte[]
     */
    private static byte[] toCstr(String str) {
        if (str == null) {
            str = "";
        }
        return (str + "\0").getBytes();
    }

    /**
     * Method removes the trailing \0 which is returned from the java.dll (just
     * if the last sign is a \0)
     * 
     * @param buf
     *            the byte[] buffer which every read method returns
     * @return String a parsed string without the trailing \0
     */
    private static String parseValue(byte[] buf) {
        if (buf == null) {
            return null;
        }
        String ret = new String(buf);
        if (ret.charAt(ret.length() - 1) == '\0') {
            return ret.substring(0, ret.length() - 1);
        }
        return ret;
    }
}