package com.snmp.server.database;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static com.snmp.server.util.Constants.*;


public class DatabaseHandler extends AbstractVerticle
{

    private final DatabaseServices<JsonObject> credentialDB = CredentialDB.getInstance();

    private final DatabaseServices<JsonObject> discoveryDB = DiscoveryDB.getInstance();

    private final DatabaseServices<JsonObject> provisionDB = ProvisionDB.getInstance();


    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {

        EventBus eventbus = vertx.eventBus();

        eventbus.localConsumer(Constants.CREDENTIAL_ADDRESS, data -> {

            try
            {
                JsonObject inputData = (JsonObject) data.body();

                switch (inputData.getString(Constants.REQUEST_TYPE))
                {

                    case Constants.CREDENTIAL_POST:

                        vertx.executeBlocking(insertPromise -> {

                            //                            System.out.println("Message : " + inputData.toString());

                            inputData.put(CREDENTIAL_ID_KEY, CREDENTIAL_ID).remove(REQUEST_TYPE);

                            JsonObject json = credentialDB.add(CREDENTIAL_ID, inputData);

                            if (json == null)
                            {
                                CREDENTIAL_ID++;
                                insertPromise.complete();
                            }
                            else
                            {
                                insertPromise.fail("Credential Name already Exist!!");
                            }
                        }, false, handler -> {

                            if (handler.succeeded())
                            {
                                data.reply(Util.setSuccessResponse("Credential Profile Created successfully"));
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

                            promise.complete(Util.setSuccessResponse("Data Retrieved").put("data", credentials));

                        }, false, result -> {

                            if (result.succeeded())
                            {
                                data.reply(result.result());
                            }

                            else
                            {
                                data.reply(Util.setFailureResponse(result.cause().getMessage()));
                            }

                        });

                        break;

                    case Constants.CREDENTIAL_GET:

                        int credentialId = inputData.getInteger(CREDENTIAL_ID_KEY);

                        getVertx().executeBlocking(promise -> {

                            JsonObject credentialProfile = credentialDB.get(credentialId);

                            if (credentialProfile != null)
                            {

                                promise.complete(credentialProfile);

                            }
                            else
                            {
                                promise.fail("Credential Id doesn't exist");
                            }

                        }, false, result -> {

                            if (result.succeeded())
                                data.reply(Util.setSuccessResponse("Data Retrieved").put("data",result.result()));

                            else
                                data.reply(Util.setFailureResponse(result.cause().getMessage()));
                        });

                        break;

                    case Constants.CREDENTIAL_PUT:

                        credentialId = inputData.getInteger(CREDENTIAL_ID_KEY);

                        getVertx().executeBlocking(promise -> {

                            inputData.remove(REQUEST_TYPE);

                            if (credentialDB.isKeyExist(credentialId))
                            {

                                JsonObject oldCredentialProfile = credentialDB.get(credentialId);

                                oldCredentialProfile.put(COMMUNITY, inputData.getString(COMMUNITY)).put(VERSION, inputData.getString(VERSION));


                                if (oldCredentialProfile.getString(CREDENTIAL_NAME).equals(oldCredentialProfile.getString(CREDENTIAL_NAME)))
                                {
                                    if (credentialDB.update(credentialId, inputData) != null)
                                    {
                                        promise.complete(Util.setSuccessResponse("Credential Profile updated successfully"));
                                    }
                                    else
                                    {
                                        promise.fail("Failed to update Credential Profile");
                                    }
                                }
                                else
                                {
                                    promise.fail("You can't change the credential Name");
                                }

                            }
                            else
                            {
                                promise.fail("Credential Id doesn't exist");
                            }

                        }, false, result -> {

                            if (result.succeeded())
                            {
                                data.reply(result.result());
                            }
                            else
                                data.reply(Util.setFailureResponse(result.cause().getMessage()));

                        });

                        break;

                    case Constants.CREDENTIAL_DELETE:

                        credentialId = inputData.getInteger(CREDENTIAL_ID_KEY);

                        getVertx().executeBlocking(promise -> {

                            List<JsonObject> discoveryProfiles = discoveryDB.getAll();

                            List<JsonObject> provisionProfiles = provisionDB.getAll();

                            if (!findKey(discoveryProfiles, CREDENTIAL_ID_KEY, credentialId) && !findKey(provisionProfiles, CREDENTIAL_ID_KEY, credentialId))
                            {

                                if (credentialDB.delete(credentialId) != null)
                                {
                                    promise.complete(Util.setSuccessResponse("Credential Profile deleted successfully"));
                                }
                                else
                                    promise.fail("Credential Id doesn't exist.");

                            }
                            else
                                promise.fail("Failed to delete Credential Profile because It is used in discovery or provision");

                        }, false, result -> {

                            if (result.succeeded())
                            {
                                data.reply(result.result());
                            }
                            else
                                data.reply(Util.setFailureResponse(result.cause().getMessage()));

                        });

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

                        if (credentialDB.isKeyExist(inputData.getInteger(CREDENTIAL_ID_KEY)))
                        {
                            inputData.put(DISCOVERY_ID_KEY, DISCOVERY_ID).put("isDiscovered", "false").remove(REQUEST_TYPE);

                            JsonObject json = discoveryDB.add(DISCOVERY_ID, inputData);

                            if (json == null)
                            {
                                DISCOVERY_ID++;
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

                        JsonObject discoveryProfile = discoveryDB.get(inputData.getInteger(DISCOVERY_ID_KEY));

                        JsonObject response = new JsonObject();

                        if (discoveryProfile != null)
                        {

                            JsonObject credentialProfile = credentialDB.get(discoveryProfile.getInteger("credentialId"));

                            if (credentialProfile != null)
                            {

                                response.put("credentialId", credentialProfile.getInteger("credentialId")).put(DISCOVERY_ID_KEY, discoveryProfile.getInteger(DISCOVERY_ID_KEY)).put("credentialName", credentialProfile.getString("credentialName")).put("discoveryName", discoveryProfile.getString("discoveryName")).put("community", credentialProfile.getString("community")).put("version", credentialProfile.getString("version")).put("port", discoveryProfile.getInteger("port")).put("ip", discoveryProfile.getString("ip")).put(STATUS, STATUS_SUCCESS);

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

                    int discoveryId = inputData.getInteger(DISCOVERY_ID_KEY);

                    getVertx().executeBlocking(promise -> {

                        JsonObject discoveryProfile = discoveryDB.get(discoveryId);

                        if (discoveryProfile != null)
                        {
                            promise.complete(discoveryProfile);
                        }
                        else
                        {
                            promise.fail("Discovery Id doesn't exist");
                        }

                    }, false, result -> {

                        if (result.succeeded())
                            data.reply(Util.setSuccessResponse("Data Retrieved").put("data",result.result()));

                        else
                            data.reply(Util.setFailureResponse(result.cause().getMessage()));
                    });

                    break;

                case DISCOVERY_PUT:

                    discoveryId = inputData.getInteger(DISCOVERY_ID_KEY);

                    getVertx().executeBlocking(promise -> {

                        inputData.remove(REQUEST_TYPE);

                        JsonObject oldDiscoveryProfile = discoveryDB.get(discoveryId);

                        if (oldDiscoveryProfile != null)
                        {
                            if (credentialDB.isKeyExist(inputData.getInteger(CREDENTIAL_ID_KEY)))
                            {
                                if (oldDiscoveryProfile.getString(DISCOVERY_NAME).equals(inputData.getString(DISCOVERY_NAME)))
                                {
                                    oldDiscoveryProfile.put(IP, inputData.getString(IP)).put(PORT, inputData.getString(PORT)).put(CREDENTIAL_ID_KEY, inputData.getInteger(CREDENTIAL_ID_KEY)).put(IS_DISCOVERED, "false");

                                    if (discoveryDB.update(discoveryId, oldDiscoveryProfile) != null)
                                    {
                                        System.out.println("\nready for update");
                                        promise.complete(Util.setSuccessResponse("discovery Profile updated successfully"));
                                    }
                                    else
                                    {
                                        promise.fail("Failed to update discovery Profile");
                                    }
                                }
                                else
                                {
                                    promise.fail("You can't change the discovery Name");
                                }
                            }else {
                                promise.fail("Credential Id doesn't exist");
                            }
                        }
                        else
                        {
                            promise.fail("Discovery Id doesn't exist");
                        }

                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }
                        else
                            data.reply(Util.setFailureResponse(result.cause().getMessage()));

                    });

                    break;

                case DISCOVERY_UPDATE:

                    getVertx().executeBlocking(promise -> {

                        String[] keysToRemove = {REQUEST_TYPE, STATUS, "type"};

                        // Remove key-value pairs
                        for (String key : keysToRemove)
                        {
                            inputData.remove(key);
                        }

                        if (discoveryDB.update(inputData.getInteger(DISCOVERY_ID_KEY), inputData) != null)
                        {
                            promise.complete(new JsonObject().put(STATUS, STATUS_SUCCESS).put(MESSAGE, "Discovery updated successfully"));
                        }
                        else
                            promise.fail("Discovery update Failed");


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

                    discoveryId = inputData.getInteger(DISCOVERY_ID_KEY);

                    getVertx().executeBlocking(promise -> {

                        if (discoveryDB.delete(discoveryId) != null){
                            promise.complete(Util.setSuccessResponse("Discovery profile deleted successfully"));
                        }
                        else
                            promise.fail("Discovery Profile not deleted");

                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }
                        else
                            data.reply(Util.setFailureResponse(result.cause().getMessage()));

                    });

                    break;

            }

        });

        eventbus.localConsumer(PROVISION_ADDRESS, data -> {

            JsonObject inputData = (JsonObject) data.body();

            switch (inputData.getString(Constants.REQUEST_TYPE))
            {

                case PROVISION_RUN:

                    vertx.executeBlocking(promise -> {

                        System.out.println("\nId for provision : " + inputData.getInteger(DISCOVERY_ID_KEY));

                        JsonObject discoveryProfile = discoveryDB.get(inputData.getInteger(DISCOVERY_ID_KEY));

                        if (discoveryProfile != null)
                        {
                            if (discoveryProfile.getString("isDiscovered") != null && discoveryProfile.getString("isDiscovered").equals("true"))
                            {


                                String[] keysToRemove = {"credentialName", "community", "version"};

                                // Remove key-value pairs
                                for (String key : keysToRemove)
                                {
                                    discoveryProfile.remove(key);
                                }

                                if (provisionDB.add(PROVISION_ID++, discoveryProfile) == null)
                                {
                                    promise.complete("Provision Success");
                                }
                                else
                                {
                                    promise.fail("Provision Failed");
                                }

                            }
                            else
                                promise.fail("Devise is not Discovered");
                        }
                        else
                        {
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

                        for (Object profile : provisionProfile)
                        {

                            JsonObject provisionData = (JsonObject) profile;

                            JsonObject credentialData = credentialDB.get(provisionData.getInteger("credentialId"));

                            provisionData.put("credentialName", credentialData.getString("credentialName")).put("community", credentialData.getString("community")).put("version", credentialData.getString("version"));
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

                case PROVISION_DELETE:

                    int provisionId = inputData.getInteger(PROVISION_ID_KEY);

                    getVertx().executeBlocking(promise -> {

                        if (provisionDB.delete(provisionId) != null){
                            promise.complete(Util.setSuccessResponse("Provision profile deleted successfully"));
                        }
                        else
                            promise.fail("Provision Profile not deleted");

                    }, false, result -> {

                        if (result.succeeded())
                        {
                            data.reply(result.result());
                        }
                        else
                            data.reply(Util.setFailureResponse(result.cause().getMessage()));

                    });

            }

        });

        startPromise.complete();
    }

    private static boolean findKey(List<JsonObject> list, String key, int value)
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
