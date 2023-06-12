package com.snmp.server.database;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.snmp.server.util.Constants.*;


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

        if (credentialProfiles.get(id) != null)
        {

            return credentialProfiles.get(id).copy();
        }
        else
        {
            return null;
        }


    }

    @Override
    public List<JsonObject> getAll()
    {

        return new ArrayList<>(credentialProfiles.values());
    }

    @Override
    public JsonObject update(int id, JsonObject obj)
    {

        return credentialProfiles.put(id, obj);
    }

    @Override
    public JsonObject add(int id, JsonObject obj)
    {

        if (containsKeyValue(CREDENTIAL_NAME,obj.getString(CREDENTIAL_NAME)))
        {
            return obj;
        }

        System.out.println("Credential Data Added");

        return credentialProfiles.put(id, obj);
    }

    @Override
    public JsonObject delete(int id)
    {

        return credentialProfiles.remove(id);
    }

    @Override
    public boolean containsKey(int id)
    {

        return credentialProfiles.containsKey(id);
    }

    @Override
    public boolean containsKeyValue(String key, String value)
    {

        return credentialProfiles.values().stream().anyMatch(profile -> profile.getString(key).equalsIgnoreCase(value));
    }


}
