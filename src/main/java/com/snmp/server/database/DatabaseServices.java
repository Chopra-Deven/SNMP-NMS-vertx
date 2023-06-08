package com.snmp.server.database;

import java.util.List;


public interface DatabaseServices<T>
{

    T get(int id);

    List<T> getAll();

    T update(int id, Object obj);

    T add(int id, Object obj);

    T delete (int id);

    boolean isKeyExist(int id);

    boolean isValueExist(String name);

}
