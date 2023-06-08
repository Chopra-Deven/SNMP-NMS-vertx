package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.DiscoveryUtil;
import com.snmp.server.util.Util;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.snmp.server.util.Constants.*;


public class DiscoveryHandler
{

    private final Vertx vertx;

    EventBus eventBus;

    private static final DiscoveryUtil discoveryUtil = new DiscoveryUtil();

    public DiscoveryHandler(Vertx vertx, EventBus eventBus)
    {

        this.vertx = vertx;

        this.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        System.out.println("Discovery Init Called");

        Router router = Router.router(vertx);

        router.post("/").consumes("application/json").handler(BodyHandler.create()).handler(context -> {

            HttpServerResponse response = context.response();

            response.putHeader("content-type", "application/json");

            JsonObject inputData = context.body().asJsonObject();

            if (discoveryUtil.isValidInput(inputData))
            {
                inputData.put(REQUEST_TYPE, DISCOVERY_POST);

                eventBus.request(DISCOVER_ADDRESS, inputData).onComplete(result -> {

                    if (result.succeeded())
                    {
                        JsonObject resultData = (JsonObject) result.result().body();

                        if (resultData.getString(STATUS).equals(STATUS_SUCCESS))
                        {
                            response.end(Util.getSuccessResponse("Discovery Profile Created.").encodePrettily());
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
                });
            }
            else
                response.end(Util.getFailureResponse("Invalid Input").encodePrettily());

        });

        router.get("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader("content-type", "application/json");

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

            response.putHeader("content-type", "application/json");

            eventBus.request(DISCOVER_ADDRESS, new JsonObject().put("discoveryId", id).put(REQUEST_TYPE, DISCOVERY_RUN), replyHandler -> {

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

                            eventBus.request(DISCOVER_ADDRESS, updatedDiscoverProfile.put(REQUEST_TYPE, DISCOVERY_PUT)).onComplete(task -> {

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


        return router;

    }

    private static JsonObject discovery(JsonObject discoveryProfile)
    {

        if (discoveryUtil.ping(discoveryProfile.getString("ip")))
        {

            JsonObject response = discoveryUtil.getSystemName(discoveryProfile);

            return response.getJsonObject("result");

        }

        return Util.getFailureResponse("Device is not available or unreachable");
    }

}
