package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CredentialDB implements DatabaseServices
{

    private static CredentialDB instance;

    private static final HashMap<Integer, JsonObject> credentialProfiles = new HashMap<>();

    public static CredentialDB getInstance()
    {

        if (instance == null)

            instance = new CredentialDB();

        return instance;
    }

    @Override
    public Object get(int id)
    {

        return credentialProfiles.get(id).copy();
    }

    @Override
    public List<Object> getAll()
    {
        return new ArrayList<>(credentialProfiles.values());
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
    public Object delete(int id)
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
