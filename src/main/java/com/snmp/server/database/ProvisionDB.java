package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;


public class ProvisionDB implements DatabaseServices
{

    private static ProvisionDB instance;

    private static final HashMap<Integer, JsonObject> provisionProfiles = new HashMap<>();

    public static ProvisionDB getInstance()
    {

        if (instance == null)

            instance = new ProvisionDB();

        return instance;
    }

    @Override
    public Object get(int id)
    {

        return null;
    }

    @Override
    public List getAll()
    {

        return null;
    }

    @Override
    public Object update(int id, Object obj)
    {

        return null;
    }

    @Override
    public Object add(int id, Object obj)
    {
        JsonObject inputData = (JsonObject) obj;

        System.out.println("Provision Added");
        return provisionProfiles.put(id, inputData);
    }

    @Override
    public Object delete(int id)
    {

        return null;
    }

    @Override
    public boolean isKeyExist(int id)
    {

        return false;
    }

    @Override
    public boolean isValueExist(String name)
    {

        return false;
    }

}
