package com.snmp.server.util;

import io.netty.util.collection.CharObjectMap;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.snmp.server.util.Constants.*;


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

    public boolean ping(String ip)
    {

        List<String> command = new ArrayList<>();

        command.add("fping");
        command.add("-c");
        command.add(NUMBER_OF_PACKETS);
        command.add("-q");
        command.add(ip);

        //        ProcessBuilder builder = new ProcessBuilder(command);

        JsonObject data = Util.executeProcess(command);

        if (data.getString(STATUS).equals(STATUS_FAIL))

            return false;

        else

            return Util.getStatus(data.getString("result"));

    }

    public JsonObject getSystemName(JsonObject inputData)
    {

        inputData.put("type", "discovery");

        List<String> command = new ArrayList<>();

        command.add(PLUGIN_PATH);

        command.add(inputData.toString());

        JsonObject result = Util.executeProcess(command);

        if (result.getString(STATUS).equals(STATUS_FAIL))
        {
            return result.put(STATUS, STATUS_FAIL);
        }
        else
        {
            return new JsonObject(result.getString("result"));
        }

    }

}
