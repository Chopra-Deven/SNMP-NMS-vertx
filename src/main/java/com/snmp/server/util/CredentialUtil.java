package com.snmp.server.util;

import io.vertx.core.json.JsonObject;


public class CredentialUtil
{

    public boolean isValidInput(JsonObject data)
    {

        try
        {
            if (data.getString("credentialName") != null && data.getString("community") != null && data.getString("version") != null && !data.getString("credentialName").trim().equals("") && !data.getString("community").trim().equals("") && !data.getString("version").trim().equals(""))
            {

                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

}
