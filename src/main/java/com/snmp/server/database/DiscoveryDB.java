package com.snmp.server.database;

import com.snmp.server.util.Constants;
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

        if (discoveryProfiles.get(id) != null)
            return discoveryProfiles.get(id).copy();

        return null;
    }

    @Override
    public List<JsonObject> getAll()
    {

        return new ArrayList<>(discoveryProfiles.values());
    }

    @Override
    public JsonObject update(int id, JsonObject obj)
    {

        return discoveryProfiles.put(id, obj);
    }

    @Override
    public JsonObject add(int id, JsonObject obj)
    {

        if (containsKeyValue(Constants.DISCOVERY_NAME,obj.getString("discoveryName")))
        {
            return obj;
        }

        System.out.println("Discovery Added");
        return discoveryProfiles.put(id, obj);
    }

    @Override
    public JsonObject delete(int id)
    {

        return discoveryProfiles.remove(id);
    }

    public boolean containsKey(int id)
    {

        return discoveryProfiles.containsKey(id);
    }

    @Override
    public boolean containsKeyValue(String key, String value)
    {

        return discoveryProfiles.values().stream().anyMatch(profile -> profile.getString(key).equalsIgnoreCase(value));
    }

}
