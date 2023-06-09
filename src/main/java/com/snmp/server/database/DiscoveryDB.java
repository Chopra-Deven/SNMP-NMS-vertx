package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class DiscoveryDB implements DatabaseServices<JsonObject>
{

    private static DiscoveryDB instance;

    private static final ConcurrentHashMap<Integer, JsonObject> discoveryProfiles = new ConcurrentHashMap<>();

    public static DiscoveryDB getInstance()
    {

        if (instance == null)

            instance = new DiscoveryDB();

        return instance;
    }

    @Override
    public JsonObject get(int id)
    {
        return discoveryProfiles.get(id).copy();
    }

    @Override
    public List<JsonObject> getAll()
    {

        return new ArrayList<>(discoveryProfiles.values());
    }

    @Override
    public JsonObject update(int id, Object obj)
    {

        return discoveryProfiles.put(id, (JsonObject) obj);
    }

    @Override
    public JsonObject add(int id, Object obj)
    {
        JsonObject inputData = (JsonObject) obj;

        if (isValueExist(inputData.getString("discoveryName"))){
            return inputData;
        }

        System.out.println("Discovery Added");
        return discoveryProfiles.put(id, inputData);
    }

    @Override
    public JsonObject delete(int id)
    {
        return discoveryProfiles.remove(id);
    }

    public boolean isKeyExist(int id){

        return discoveryProfiles.containsKey(id);
    }

    public boolean isValueExist(String name){

        return discoveryProfiles.values().stream().anyMatch(value -> value.getString("discoveryName").equalsIgnoreCase(name));
    }

}
