package com.snmp.server.database;

import com.snmp.server.util.DiscoveryUtil;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DiscoveryDB implements DatabaseServices
{

    private static DiscoveryDB instance;

    private static final HashMap<Integer, JsonObject> discoveryProfiles = new HashMap<>();

    public static DiscoveryDB getInstance()
    {

        if (instance == null)

            instance = new DiscoveryDB();

        return instance;
    }

    @Override
    public Object get(int id)
    {
        return discoveryProfiles.get(id).copy();
    }

    @Override
    public List<Object> getAll()
    {

        return new ArrayList<>(discoveryProfiles.values());
    }

    @Override
    public Object update(int id, Object obj)
    {

        return discoveryProfiles.put(id, (JsonObject) obj);
    }

    @Override
    public Object add(int id, Object obj)
    {
        JsonObject inputData = (JsonObject) obj;

        if (isValueExist(inputData.getString("discoveryName"))){
            return inputData;
        }

        System.out.println("Discovery Added");
        return discoveryProfiles.put(id, inputData);
    }

    @Override
    public Object delete(int id)
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
