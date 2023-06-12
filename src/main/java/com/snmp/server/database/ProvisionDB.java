package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProvisionDB implements DatabaseServices<JsonObject>
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
    public JsonObject get(int id)
    {

        if (provisionProfiles.get(id) != null){
            return provisionProfiles.get(id).copy();
        }

        return null;
    }

    @Override
    public List<JsonObject> getAll()
    {

        return new ArrayList<>(provisionProfiles.values());
    }

    @Override
    public JsonObject update(int id, JsonObject obj)
    {

        return provisionProfiles.put(id, obj);
    }

    @Override
    public JsonObject add(int id, JsonObject obj)
    {

        System.out.println("Provision Added");

        return provisionProfiles.put(id, obj);
    }

    @Override
    public JsonObject delete(int id)
    {

        return provisionProfiles.remove(id);
    }

    @Override
    public boolean containsKey(int id)
    {

        return provisionProfiles.containsKey(id);
    }

    @Override
    public boolean containsKeyValue(String key, String value)
    {

        return provisionProfiles.values().stream().anyMatch(profile -> profile.getString(key).equalsIgnoreCase(value));
    }

    /*@Override
    public boolean isValueExist(String name)
    {

        return provisionProfiles.values().stream().anyMatch(value -> value.getString("discoveryName").equalsIgnoreCase(name));
    }*/



}
