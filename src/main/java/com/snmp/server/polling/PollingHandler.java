package com.snmp.server.polling;

import com.snmp.server.util.Util;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.snmp.server.util.Constants.*;


public class PollingHandler extends AbstractVerticle
{

    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {

        EventBus eventBus = vertx.eventBus();

        vertx.setPeriodic(POLLING_INTERVAL, handler -> {

            eventBus.<JsonObject>request(PROVISION_ADDRESS, new JsonObject().put(REQUEST_TYPE, PROVISION_GET_ALL)).onComplete(result -> {

                if (result.succeeded())
                {
                    JsonArray provisionProfiles = result.result().body().getJsonArray(DATA);

                    for (Object profile : provisionProfiles)
                    {
                        JsonObject profileData = (JsonObject) profile;

                        profileData.put(METRICS, SYSTEM_METRICS);

                        poll(profileData, vertx);

                        profileData.put(METRICS, INTERFACE_METRICS);

                        poll(profileData, vertx);


                    }   // end of ProvisionProfile JsonArray

                }   //  end of if succeed

            }); // end of eventbus

        }); //  end of setPeriodic

        startPromise.complete();

    }

    public static void poll(JsonObject profileData, Vertx vertx)
    {

        List<String> command = new ArrayList<>();

        command.add(PLUGIN_PATH);

        command.add(profileData.toString());

        vertx.executeBlocking(promise -> {

            JsonObject processResult = Util.executeProcess(command);

            if (processResult.getString(STATUS).equals(STATUS_FAIL))
            {
                promise.fail(processResult.getString(MESSAGE));
            }
            else
            {
                promise.complete(new JsonObject(processResult.getString(RESULT)));
            }

        }, false, task -> {

            if (task.succeeded())
            {

                JsonObject allData = new JsonObject(task.result().toString());

                String metrics = allData.getString(METRICS);

                String ip = allData.getString(IP);

                JsonObject result = allData.getJsonObject(RESULT);

                if (result.getString(STATUS).equals(STATUS_SUCCESS))
                {

                    writeDataToFile(new JsonObject(result.getJsonObject(DATA).encodePrettily()), vertx, metrics, ip);
                }
                else
                {
                    System.out.println("\nError in polling : " + result.getString(MESSAGE));
                }

            }

            else
            {
                System.out.println("\nError in process result : " + task.cause().getMessage());
            }


        });
    }

    public static void writeDataToFile(JsonObject inputData, Vertx vertx, String metrics, String directoryName)
    {

        String fileName;

        if (metrics.equals(SYSTEM_METRICS))
            fileName = "systemInfo.json";
        else
            fileName = "interfaceInfo.json";

        String directoryPath = DATA_STORE + directoryName;

        String filePath = directoryPath + "/" + fileName;

        vertx.fileSystem().mkdirs(directoryPath, result -> {
            if (result.succeeded())
            {

                checkOrCreateJsonFile(filePath, vertx, inputData);
            }
            else
            {
                // Failed to create directory
                System.out.println("Failed to create directory: " + result.cause().getMessage());
            }
        });

    }

    private static void checkOrCreateJsonFile(String filePath, Vertx vertx, JsonObject inputData)
    {

        vertx.fileSystem().exists(filePath, existsResult -> {
            if (existsResult.succeeded())
            {
                if (existsResult.result())
                {

                    insertJsonObject(filePath, vertx, inputData);
                }
                else
                {
                    // JSON file doesn't exist, create and initialize it
                    createAndInitializeJsonFile(filePath, vertx, inputData);
                }
            }
            else
            {
                // Failed to check file existence
                System.out.println("Failed to check file existence: " + existsResult.cause().getMessage());
            }
        });
    }

    private static void createAndInitializeJsonFile(String filePath, Vertx vertx, JsonObject inputData)
    {

        JsonArray jsonArray = new JsonArray();

        OpenOptions options = new OpenOptions().setCreate(true).setTruncateExisting(true);

        vertx.fileSystem().open(filePath, options, result -> {
            if (result.succeeded())
            {
                AsyncFile asyncFile = result.result();

                asyncFile.write(jsonArray.toBuffer(), writeResult -> {
                    if (writeResult.succeeded())
                    {
                        // JSON file created and initialized with an empty JsonArray
                        // Now you can insert JsonObjects at runtime
                        insertJsonObject(filePath, vertx, inputData);
                    }
                    else
                    {
                        // Failed to write data to the JSON file
                        System.out.println("Failed to write data to the JSON file: " + writeResult.cause().getMessage());
                    }
                });
            }
            else
            {
                // Failed to open/create the JSON file
                System.out.println("Failed to open/create the JSON file: " + result.cause().getMessage());
            }
        });
    }

    private static void insertJsonObject(String filePath, Vertx vertx, JsonObject inputData)
    {

        vertx.fileSystem().readFile(filePath, result -> {
            if (result.succeeded())
            {

                //                System.out.println("Result : " + result.result());
                // Read the contents of the JSON file
                JsonArray fileData = result.result().toJsonArray();

                // Get the existing JsonArray or create a new one if it doesn't exist
                fileData.add(new JsonObject().put(String.valueOf(System.currentTimeMillis()), inputData));

                // Add the new JsonObject to the JsonArray
                // Update the file with the modified JsonArray
                vertx.fileSystem().writeFile(filePath, Buffer.buffer(fileData.encodePrettily()), writeResult -> {
                    if (writeResult.succeeded())
                    {
                        System.out.println("Data inserted successfully");
                    }
                    else
                    {
                        System.out.println("Failed to insert JsonObject: " + writeResult.cause().getMessage());
                    }
                });
            }
            else
            {
                System.out.println("Failed to read JSON file: " + result.cause().getMessage());
            }
        });
    }


}


