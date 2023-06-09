package com.snmp.server.util;

import io.vertx.core.json.JsonObject;

public class DiscoveryUtil
{

    public boolean isValidInput(JsonObject data)
    {

        try
        {
            if (data.getString("discoveryName") != null && data.getString("ip") != null && data.getInteger("port") != null && data.getInteger("credentialId") != null && !data.getString("discoveryName").trim().equals("") && !data.getString("ip").trim().equals("") && data.getInteger("port") > 0 && data.getInteger("credentialId") > 0)
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
