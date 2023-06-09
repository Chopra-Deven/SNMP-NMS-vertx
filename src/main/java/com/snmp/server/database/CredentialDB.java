package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CredentialDB implements DatabaseServices<JsonObject>
{

    private static CredentialDB instance;

    private static final ConcurrentHashMap<Integer, JsonObject> credentialProfiles = new ConcurrentHashMap<>();

    public static CredentialDB getInstance()
    {

        if (instance == null)

            instance = new CredentialDB();

        return instance;
    }

    @Override
    public JsonObject get(int id)
    {
        return credentialProfiles.get(id).copy();
    }

    @Override
    public List<JsonObject> getAll()
    {
        return new ArrayList<>(credentialProfiles.values());
    }

    @Override
    public JsonObject update(int id, Object obj)
    {

        return credentialProfiles.put(id, (JsonObject) obj);
    }

    @Override
    public JsonObject add(int id, Object obj)
    {

        JsonObject inputData = (JsonObject) obj;

        for (Map.Entry<Integer, JsonObject> entry : credentialProfiles.entrySet())
        {

            if (entry.getValue().getString("credentialName").equalsIgnoreCase(inputData.getString("credentialName")))
            {

                return inputData;
            }
        }

        System.out.println("Credential Data Added");

        return credentialProfiles.put(id, inputData);
    }

    @Override
    public JsonObject delete(int id)
    {

        return credentialProfiles.remove(id);
    }

    @Override
    public boolean isKeyExist(int id)
    {

        return credentialProfiles.containsKey(id);
    }


    public boolean isValueExist(String name)
    {

        return credentialProfiles.values().stream().anyMatch(value -> value.getString("discoveryName").equalsIgnoreCase(name));
    }

}
