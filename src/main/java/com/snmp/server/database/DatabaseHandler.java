package com.snmp.server.database;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.snmp.server.util.Constants.*;


public class DatabaseHandler extends AbstractVerticle
{

    private final DatabaseServices credentialDB = CredentialDB.getInstance();

    private final DatabaseServices discoveryDB = DiscoveryDB.getInstance();

    private final DatabaseServices provisionDB = ProvisionDB.getInstance();


    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {

        EventBus eventbus = vertx.eventBus();

        eventbus.localConsumer(Constants.CREDENTIAL_ADDRESS, data -> {

            try
            {
                JsonObject inputData = (JsonObject) data.body();

                //            JsonObject response = new JsonObject();

                switch (inputData.getString(Constants.REQUEST_TYPE))
                {

                    case Constants.CREDENTIAL_POST:

                        vertx.executeBlocking(insertPromise -> {

                            //                            System.out.println("Meessage : " + inputData.toString());

                            inputData.put("credentialId", CREDENTIAL_ID).remove(REQUEST_TYPE);

                            var json = (JsonObject) credentialDB.add(Constants.CREDENTIAL_ID++, inputData);

                            if (json == null)
                            {
                                insertPromise.complete();
                            }
                            else
                            {
                                insertPromise.fail("Credential Name already Exist!!");
                            }
                        }, false, handler -> {

                            if (handler.succeeded())
                            {
                                data.reply(Util.getSuccessResponse("Credential Profile Created successfully"));
                            }

                            else
                            {
                                data.reply(Util.getFailureResponse(handler.cause().getMessage()));
                            }
                        });

                        break;

                    case Constants.CREDENTIAL_GET_ALL:

                        vertx.executeBlocking(promise -> {

                            JsonArray credentials = new JsonArray(credentialDB.getAll());

                            promise.complete(Util.getSuccessResponse("Data Retrieved").put("data", credentials));

                        }, false, result -> {

                            if (result.succeeded())
                            {
                                data.reply(result.result());
                            }

                            else
                            {
                                data.reply(Util.getFailureResponse(result.cause().getMessage()));
                            }

                        });

                        break;

                    case Constants.CREDENTIAL_GET:

                        getVertx().executeBlocking(promise -> {


                        }, false);

                        break;

                    case Constants.CREDENTIAL_PUT:

                        break;

                    case Constants.CREDENTIAL_DELETE:

                        break;

                }
            }
            catch (Exception exception)
            {
                System.out.println("Deven::: " + exception);
            }

        });

        eventbus.localConsumer(Constants.DISCOVER_ADDRESS, data -> {

            JsonObject inputData = (JsonObject) data.body();

            switch (inputData.getString(Constants.REQUEST_TYPE))
            {

                case DISCOVERY_POST:

                    vertx.executeBlocking(insertPromise -> {

                        if (credentialDB.isKeyExist(inputData.getInteger("credentialId")))
                        {

                            inputData.put("discoveryId", DISCOVERY_ID).put("isDiscovered","false").remove(REQUEST_TYPE);

                            JsonObject json = (JsonObject) discoveryDB.add(DISCOVERY_ID++, inputData);

                            if (json == null)
                            {
                                insertPromise.complete("Discovery Profile created successfully");
                            }
                            else
                            {
                                insertPromise.fail("Discovery Name already Exist!!");
                            }
                        }
                        else
                        {
                            insertPromise.fail("Credential Key doesn't exist");
                        }

                    }, false, handler -> {

                        if (handler.succeeded())
                        {
                            data.reply(Util.getSuccessResponse(handler.result().toString()));
                        }

                        else
                        {
                            data.reply(Util.getFailureResponse(handler.cause().getMessage()));
                        }
                    });

                    break;

                case DISCOVERY_GET_ALL:

                    vertx.executeBlocking(promise -> {

                        JsonArray credentials = new JsonArray(discoveryDB.getAll());

                        promise.complete(Util.getSuccessResponse("Data Retrieved").put("data", credentials));

                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }

                        else
                        {
                            data.reply(Util.getFailureResponse(result.cause().getMessage()));
                        }

                    });

                    break;

                case DISCOVERY_RUN:

                    vertx.executeBlocking(promise -> {

                        JsonObject discoveryProfile = (JsonObject) discoveryDB.get(inputData.getInteger("discoveryId"));

                        JsonObject response = new JsonObject();

                        if (discoveryProfile != null)
                        {

                            JsonObject credentialProfile = (JsonObject) credentialDB.get(discoveryProfile.getInteger("credentialId"));

                            if (credentialProfile != null)
                            {

                                response.put("credentialId", credentialProfile.getInteger("credentialId")).put("discoveryId", discoveryProfile.getInteger("discoveryId")).put("credentialName", credentialProfile.getString("credentialName")).put("discoveryName", discoveryProfile.getString("discoveryName")).put("community", credentialProfile.getString("community")).put("version", credentialProfile.getString("version")).put("port", discoveryProfile.getInteger("port")).put("ip", discoveryProfile.getString("ip")).put(STATUS, STATUS_SUCCESS);

                                promise.complete(response);

                            }
                            else
                                promise.fail("Credential Profile not found");

                        }
                        else
                            promise.fail("Discovery Id doesn't exist");

                    }, false, result -> {

                        if (result.succeeded())
                        {

                            data.reply(result.result());

                        }
                        else
                        {
                            data.reply(Util.getFailureResponse(result.cause().getMessage()));
                        }


                    });

                    break;

                case DISCOVERY_GET:

                    getVertx().executeBlocking(promise -> {


                    }, false);

                    break;

                case DISCOVERY_PUT:

                    getVertx().executeBlocking(promise -> {

                        String[] keysToRemove = {REQUEST_TYPE, STATUS, "type"};

                        // Remove key-value pairs
                        for (String key : keysToRemove) {
                            inputData.remove(key);
                        }

                        if (discoveryDB.update(inputData.getInteger("discoveryId"), inputData) != null)
                        {
                            promise.complete(new JsonObject().put(STATUS, STATUS_SUCCESS).put(MESSAGE, "Discovery updated successfully"));
                        }
                        else
                            promise.fail("Discovery Updation Failed");


                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }
                        else
                            data.reply(result.cause().getMessage());

                    });

                    break;

                case DISCOVERY_DELETE:

                    break;

            }

        });

        eventbus.localConsumer(PROVISION_ADDRESS, data -> {

            JsonObject inputData = (JsonObject) data.body();

            switch (inputData.getString(Constants.REQUEST_TYPE))
            {

                case PROVISION_RUN:

                    vertx.executeBlocking(promise -> {

                        System.out.println("\nId for provision : " + inputData.getInteger("discoveryId"));

                        JsonObject discoveryProfile = (JsonObject) discoveryDB.get(inputData.getInteger("discoveryId"));

                        if (discoveryProfile != null)
                        {
                            if (discoveryProfile.getString("isDiscovered") != null && discoveryProfile.getString("isDiscovered").equals("true")){


                                String[] keysToRemove = {"credentialName", "community", "version"};

                                // Remove key-value pairs
                                for (String key : keysToRemove) {
                                    discoveryProfile.remove(key);
                                }

                                if (provisionDB.add(PROVISION_ID++, discoveryProfile) == null){
                                    promise.complete("Provision Success");
                                }
                                else {
                                    promise.fail("Provision Failed");
                                }

                            }
                            else promise.fail("Devise is not Discovered");
                        }
                        else {
                            promise.fail("Discovery Id doesn't exist");
                        }
                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(Util.getSuccessResponse(result.result().toString()));
                        }
                        else
                        {
                            data.reply(Util.getFailureResponse(result.cause().getMessage()));
                        }


                    });

                    break;

                case PROVISION_GET_ALL:

                    vertx.executeBlocking(promise -> {

                        JsonArray provisionProfile = new JsonArray(provisionDB.getAll());

                        for (Object profile : provisionProfile){

                            JsonObject provisionData = (JsonObject) profile;

                            JsonObject credentialData = (JsonObject) credentialDB.get(provisionData.getInteger("credentialId"));

                            provisionData.put("credentialName",credentialData.getString("credentialName")).put("community",credentialData.getString("community")).put("version",credentialData.getString("version"));
                        }

//                        System.out.println("\nProvision Data : " + provisionProfile);

                        promise.complete(Util.getSuccessResponse("Data Retrieved").put("data", provisionProfile));

                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }

                        else
                        {
                            data.reply(Util.getFailureResponse(result.cause().getMessage()));
                        }

                    });
            }

        });

        startPromise.complete();

    }

}
