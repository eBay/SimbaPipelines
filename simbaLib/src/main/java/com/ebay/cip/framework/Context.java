package com.ebay.cip.framework;


/**
 * Created by hachong on 8/24/2015.
 */
public interface Context  {

    /**
     * Adds Key value pair to DB.
     * Warning: There is no duplicate key check here. So if business logic overwrites value, it will allow it.
     * @param key
     * @param value
     */
    void put(Object key, Object value);

    /**
     * Get data back from the context given a key. Implementater (child class) are free to lookup parents or limit to this jobContext only.
     * @param key
     * @return
     */
    Object get(Object key);

}
