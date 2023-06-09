package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.snmp.server.util.Constants.*;


public class DiscoveryHandler
{

    private final Vertx vertx;

    EventBus eventBus;

//    private static final DiscoveryUtil discoveryUtil = new DiscoveryUtil();

    public DiscoveryHandler(Vertx vertx, EventBus eventBus)
    {

        this.vertx = vertx;

        this.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        Router router = Router.router(vertx);

        router.post("/").consumes(APPLICATION_JSON).handler(BodyHandler.create()).handler(context -> {

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            JsonObject inputData = context.body().asJsonObject();

            if (Util.validateBody(inputData, DISCOVER_ADDRESS).equals(""))
            {
                inputData.put(REQUEST_TYPE, DISCOVERY_POST);

                eventBus.request(DISCOVER_ADDRESS, inputData).onComplete(result -> {

                    if (result.succeeded())
                    {
                        JsonObject resultData = (JsonObject) result.result().body();

                        if (resultData.getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            response.setStatusCode(200);
                            response.end(Util.setSuccessResponse("Discovery Profile Created.").encodePrettily());
                        }
                        else
                        {
                            response.setStatusCode(502);
                            response.end(Util.setFailureResponse(resultData.getString(Constants.MESSAGE)).encodePrettily());
                        }
                    }
                    else
                    {
                        response.setStatusCode(502);
                        response.end(Util.getFailureResponse("Internal Server Error").encodePrettily());
                    }
                });
            }
            else
            {
                response.setStatusCode(400);
                response.end(Util.getFailureResponse(Util.validateBody(inputData, DISCOVER_ADDRESS)).encodePrettily());
            }
        });

        router.get("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            eventBus.request(DISCOVER_ADDRESS, new JsonObject().put(REQUEST_TYPE, DISCOVERY_GET_ALL)).onComplete(result -> {

                try
                {
                    if (result.succeeded())
                    {
                        JsonObject resultData = (JsonObject) result.result().body();

                        if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                        {
                            response.end(resultData.encodePrettily());
                        }
                        else
                        {
                            response.end(Util.getFailureResponse(resultData.getString(Constants.MESSAGE)).encodePrettily());
                        }
                    }
                    else
                    {
                        response.end(Util.getFailureResponse("Internal Server Error").encodePrettily());
                    }
                }
                catch (Exception exception)
                {
                    System.out.println(exception.getMessage());
                }

            });


        });

        router.get("/run/:id").handler(context -> {

            int id = Integer.parseInt(context.pathParam("id"));

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            eventBus.request(DISCOVER_ADDRESS, new JsonObject().put(DISCOVERY_ID_KEY, id).put(REQUEST_TYPE, DISCOVERY_RUN), replyHandler -> {

                if (replyHandler.succeeded())
                {
                    JsonObject outputData = (JsonObject) replyHandler.result().body();

                    vertx.executeBlocking(promise -> {

                        JsonObject result = discovery(outputData);

                        if (result.getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            promise.complete(outputData.put("system.name", result.getString("system.name")));
                        }
                        else
                            promise.fail(result.getString(MESSAGE));


                    }, false, asyncResult -> {

                        if (asyncResult.succeeded())
                        {

                            JsonObject updatedDiscoverProfile = (JsonObject) asyncResult.result();

                            updatedDiscoverProfile.put("isDiscovered", "true");

                            eventBus.request(DISCOVER_ADDRESS, updatedDiscoverProfile.put(REQUEST_TYPE, DISCOVERY_UPDATE)).onComplete(task -> {

                                if (asyncResult.succeeded())
                                {
                                    response.end(Util.getSuccessResponse("Discovery Successful").put("system.name", updatedDiscoverProfile.getString("system.name")).encodePrettily());
                                }
                                else
                                    response.end(Util.getFailureResponse("Discovery Failed... Discovery Profile not updated").put("system.name", updatedDiscoverProfile.getString("system.name")).encodePrettily());
                            });

                        }
                        else
                        {
                            response.end(Util.getSuccessResponse(asyncResult.cause().getMessage()).encodePrettily());

                        }
                    });
                }
            });
        });

        router.put("/:id").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            int id = 0;

            if (Util.validNumeric(context.pathParam("id")))
            {
                id = Integer.parseInt(context.pathParam("id"));
            }
            else
            {
                response.setStatusCode(400);
                response.end(Util.setFailureResponse("Invalid Discovery Id").encodePrettily());
            }

            JsonObject inputData = context.body().asJsonObject();

            String error = Util.validateBody(inputData, DISCOVER_ADDRESS);

            if (error.equals(""))
            {
                inputData.put(REQUEST_TYPE, DISCOVERY_PUT).put(DISCOVERY_ID_KEY, id);

                System.out.println("Befeore send update req id : " + id);

                eventBus.<JsonObject>request(DISCOVER_ADDRESS, inputData, result -> {

                    try
                    {
                        if (result.succeeded())
                        {
                            JsonObject resultData = result.result().body();

                            if (resultData.getString(Constants.STATUS).equals(STATUS_SUCCESS))
                            {
                                response.setStatusCode(200);
                                response.end(Util.setSuccessResponse(resultData.getString(MESSAGE)).encodePrettily());
                            }
                            else
                            {
                                response.setStatusCode(400);
                                response.end(Util.setFailureResponse(resultData.getString(MESSAGE)).encodePrettily());
                            }
                        }
                        else
                        {
                            response.setStatusCode(500);
                            response.end(Util.setFailureResponse("Internal Server Error").encodePrettily());
                        }
                    }
                    catch (Exception exception)
                    {
                        response.setStatusCode(500);
                        response.end(Util.setFailureResponse("Internal Server Error : " + exception.getMessage()).encodePrettily());
                    }
                });

            }
            else
            {
                response.setStatusCode(400);
                response.end(Util.setFailureResponse(error).encodePrettily());
            }

        });

        router.get("/:id").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            int id = 0;

            if (Util.validNumeric(context.pathParam("id")))
            {
                id = Integer.parseInt(context.pathParam("id"));
            }
            else
            {
                response.setStatusCode(400);
                response.end(Util.setFailureResponse("Invalid Credential Id").encodePrettily());
            }

            eventBus.<JsonObject>request(DISCOVER_ADDRESS, new JsonObject().put(DISCOVERY_ID_KEY, id).put(REQUEST_TYPE, DISCOVERY_GET), result -> {

                try
                {
                    if (result.succeeded())
                    {
                        JsonObject resultData = result.result().body();

                        if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                        {
                            response.setStatusCode(200);
                            response.end(resultData.encodePrettily());
                        }
                        else
                        {
                            response.setStatusCode(400);
                            response.end(Util.getFailureResponse(resultData.getString(MESSAGE)).encodePrettily());
                        }
                    }
                    else
                    {
                        response.setStatusCode(500);
                        response.end(Util.setFailureResponse("Internal Server Error").encodePrettily());
                    }
                }
                catch (Exception exception)
                {
                    response.setStatusCode(500);
                    response.end(Util.setFailureResponse("Internal Server Error : " + exception.getMessage()).encodePrettily());
                }
            });

        });


        return router;

    }

    private static JsonObject discovery(JsonObject discoveryProfile)
    {

        if (ping(discoveryProfile.getString("ip")))
        {

            JsonObject response = getSystemName(discoveryProfile);

            return response.getJsonObject("result");

        }

        return Util.getFailureResponse("Device is not available or unreachable");
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
