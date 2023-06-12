package com.snmp.server.database;

import java.util.List;


public interface DatabaseServices<T>
{

    T get(int id);

    List<T> getAll();

    T update(int id, T obj);

    T add(int id, T obj);

    T delete (int id);

    boolean containsKey(int id);

//    boolean isValueExist(String name);

    boolean containsKeyValue(String key, String value);

}
