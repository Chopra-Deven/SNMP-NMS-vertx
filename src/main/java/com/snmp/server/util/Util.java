package com.snmp.server.util;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.snmp.server.util.Constants.*;


public class Util
{

    public static JsonObject setSuccessResponse(String msg)
    {

        return new JsonObject().put(STATUS, Constants.STATUS_SUCCESS).put(Constants.MESSAGE, msg);

    }

    public static JsonObject setFailureResponse(String msg)
    {

        return new JsonObject().put(STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, msg);

    }

    public static JsonObject executeProcess(List<String> command)
    {

        ProcessBuilder builder = new ProcessBuilder(command);

        JsonObject result = new JsonObject();

        BufferedReader processReader = null;

        InputStream inputStream = null;

        builder.redirectErrorStream(true);

        try
        {
            Process process = builder.start();

            if (!process.waitFor(PROCESS_TIMEOUT, TimeUnit.SECONDS))
            {
                process.destroyForcibly();

                System.out.println("Process killed");
            }

            if (process.waitFor() == 0)
            {
                inputStream = process.getInputStream();

                processReader = new BufferedReader(new InputStreamReader(inputStream));

                String output = processReader.readLine();

                return result.put("result", output).put(STATUS, STATUS_SUCCESS);
            }
            else
            {
                return result.put(STATUS, STATUS_FAIL).put(MESSAGE, process.getInputStream());
            }

        }
        catch (Exception e)
        {
            result.put(STATUS, STATUS_FAIL).put(MESSAGE, e.getMessage());
        }
        finally
        {
            try
            {
                if (processReader != null)
                    processReader.close();

                if (inputStream != null)
                    inputStream.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static boolean getPingStatus(String output)
    {

        try
        {

            String[] splitedOutput = output.split("\\s+");

            String[] packetInfo = splitedOutput[4].split("/");

            int sentPackets = Integer.parseInt(packetInfo[0]);

            int receivedPackets = Integer.parseInt(packetInfo[1]);

            if (sentPackets == receivedPackets && packetInfo[2].equals("0%,"))
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

    public static boolean isValidIp(String ip)
    {

        String zeroTo255 = "(\\d{1,2}|([01])\\" + "d{2}|2[0-4]\\d|25[0-5])";

        String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;

        Pattern p = Pattern.compile(regex);

        if (ip == null)
        {
            return false;
        }

        Matcher m = p.matcher(ip);

        return m.matches();
    }

    public static boolean validNumeric (String number)
    {
        String regex = "\\d+";

        Pattern pattern = Pattern.compile(regex);

        if (number == null)
        {
            return false;
        }

        Matcher m = pattern.matcher(number);

        return m.matches();
    }

    public static String validateBody(JsonObject inputBody, String type)
    {

        String error = "";

        if (type.equals(CREDENTIAL_ADDRESS))
        {

            if (inputBody.size() != 3)
            {
                return "Invalid input.";
            }
            if (inputBody.getString(COMMUNITY) != null)
            {
                if (!inputBody.getString(COMMUNITY).equals("public") && !inputBody.getString(COMMUNITY).equals("private"))
                {
                    error += " Please enter valid community type. ";
                }
            }
            else
                return "Please enter community type.";

            if (inputBody.getString(VERSION) != null)
            {
                if (!inputBody.getString(VERSION).equals("v1") && !inputBody.getString(VERSION).equals("v2c"))
                {
                    error += " Invalid version.";
                }
            }
            else
                error += " Please enter version.";

            if (inputBody.getString(CREDENTIAL_NAME) != null)
            {
                if (inputBody.getString(CREDENTIAL_NAME).trim().equals(""))
                {
                    error += " Please enter valid credential Name.";
                }
            }
            else
                error += " Please enter credential Name.";

            return error;
        }

        else
        {
            if (inputBody.size() != 4)
            {
                System.out.println("here");
                error += " Invalid input.";
            }
            if (!isValidIp(inputBody.getString(IP)))
            {
                error += " Please enter valid IP.";
            }
            if (inputBody.getString(PORT) != null)
            {
                try
                {
                    Integer.parseInt(String.valueOf(inputBody.getInteger(PORT)));
                    inputBody.getInteger(PORT);
                }
                catch (Exception e)
                {
                    error += " Please enter valid port.";
                }
            }
            else
                error += " Please enter port";

            if (inputBody.getString(DISCOVERY_NAME) != null)
            {
                if (inputBody.getString(DISCOVERY_NAME).trim().equals(""))
                {
                    error += " Please enter valid discovery Name.";
                }

            }
            else
                error += " Please enter discovery Name.";

            if (inputBody.getString(CREDENTIAL_ID_KEY) != null)
            {
                try
                {
                    Integer.parseInt(String.valueOf(inputBody.getInteger(CREDENTIAL_ID_KEY)));
                    inputBody.getInteger(CREDENTIAL_ID_KEY);
                }
                catch (Exception e)
                {
                    error += " Please enter credential ID.";
                }
            }
            else
                error += " Please enter port";
        }

        return error;
    }

    public static boolean ping(String ip)
    {

        List<String> command = new ArrayList<>();

        command.add("fping");
        command.add("-c");
        command.add(NUMBER_OF_PACKETS);
        command.add("-q");
        command.add(ip);

        JsonObject data = Util.executeProcess(command);

        if (data.getString(STATUS).equals(STATUS_FAIL))

            return false;

        else

            return Util.getPingStatus(data.getString("result"));

    }

    public static JsonObject getSystemName(JsonObject inputData)
    {

        inputData.put(TYPE, TYPE_DISCOVERY);

        List<String> command = new ArrayList<>();

        command.add(PLUGIN_PATH);

        command.add(inputData.toString());

        JsonObject processData = Util.executeProcess(command);

        JsonObject result = new JsonObject(processData.getString("result"));

        if (result.getString(STATUS).equals(STATUS_FAIL))
        {
            return result.put(STATUS, STATUS_FAIL);
        }
        else
        {
            return result;
        }
    }

    public static boolean findKey(List<JsonObject> list, String key, int value)
    {

        for (JsonObject profile : list)
        {
            if (profile.getInteger(key) == value)
            {
                return true;
            }
        }
        return false;
    }

}
