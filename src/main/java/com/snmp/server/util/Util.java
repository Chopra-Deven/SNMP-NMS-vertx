package com.snmp.server.util;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.snmp.server.util.Constants.*;


public class Util
{

    public static JsonObject getSuccessResponse(String msg)
    {

        return new JsonObject().put(Constants.STATUS_CODE, Constants.STATUS_CODE_200).put(STATUS, Constants.STATUS_SUCCESS).put(Constants.MESSAGE, msg);

    }

    public static JsonObject getFailureResponse(String msg)
    {

        return new JsonObject().put(Constants.STATUS_CODE, Constants.STATUS_CODE_400).put(STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, msg);

    }

   /* public static JsonObject executeProcess3(ProcessBuilder builder)
    {

        JsonObject result = new JsonObject();

        BufferedReader processReader;

        InputStream inputStream;

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

                System.out.println("\nCommand from execute : " + builder.command());

                processReader = new BufferedReader(new InputStreamReader(inputStream));

                String output = processReader.readLine();

                System.out.println("\n\nOutput from process : " + output + "\n");

                //                System.out.println("\nOutput as json " + new JsonObject(output).encode());

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

        return result;
    }*/

    public static JsonObject executeProcess(List<String> command)
    {

        ProcessBuilder builder = new ProcessBuilder(command);

        JsonObject result = new JsonObject();

        BufferedReader processReader;

        InputStream inputStream;

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

        return result;
    }

    public static boolean getStatus(String output)
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
            e.printStackTrace();
        }

        return false;

    }

}
