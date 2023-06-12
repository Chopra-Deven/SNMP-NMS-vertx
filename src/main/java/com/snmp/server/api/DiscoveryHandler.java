package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import static com.snmp.server.util.Constants.*;


public class DiscoveryHandler
{

    public static Vertx vertx;

    public static EventBus eventBus;

    public DiscoveryHandler(Vertx vertx, EventBus eventBus)
    {

        DiscoveryHandler.vertx = vertx;

        DiscoveryHandler.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        Router router = Router.router(vertx);

        post(router);

        getAll(router);

        get(router);

        put(router);

        run(router);

        delete(router);

        return router;

    }

    private static JsonObject discovery(JsonObject discoveryProfile)
    {

        if (Util.ping(discoveryProfile.getString(IP)))
        {

            JsonObject response = Util.getSystemName(discoveryProfile);

            return response.getJsonObject(RESULT);

        }

        return Util.setFailureResponse("Device is not available or unreachable");
    }

    private static void post(Router router)
    {

        router.post("/").consumes(APPLICATION_JSON).handler(BodyHandler.create()).handler(context -> {

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            try
            {

                JsonObject inputData = context.body().asJsonObject();

                if (Util.validateBody(inputData, DISCOVER_ADDRESS).equals(""))
                {
                    inputData.put(REQUEST_TYPE, DISCOVERY_POST);

                    eventBus.<JsonObject>request(DISCOVER_ADDRESS, inputData).onComplete(result -> {

                        if (result.succeeded())
                        {
                            JsonObject resultData = result.result().body();

                            if (resultData.getString(STATUS).equals(STATUS_SUCCESS))
                            {
                                response.setStatusCode(200);
                                response.end(resultData.encodePrettily());
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
                            response.end(Util.setFailureResponse("Internal Server Error").encodePrettily());
                        }
                    });
                }
                else
                {
                    response.setStatusCode(400);
                    response.end(Util.setFailureResponse(Util.validateBody(inputData, DISCOVER_ADDRESS)).encodePrettily());
                }
            }
            catch (DecodeException exception){
                response.setStatusCode(400);
                response.end(Util.setFailureResponse("Invalid Json Body!").encodePrettily());
            }
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + e.getMessage()).encodePrettily());
            }
        });

    }

    private static void getAll(Router router)
    {

        router.get("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            try
            {

                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                eventBus.<JsonObject>request(DISCOVER_ADDRESS, new JsonObject().put(REQUEST_TYPE, DISCOVERY_GET_ALL)).onComplete(result -> {

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
                                response.end(Util.setFailureResponse(resultData.getString(Constants.MESSAGE)).encodePrettily());
                            }
                        }
                        else
                        {
                            response.end(Util.setFailureResponse("Internal Server Error").encodePrettily());
                        }
                    }
                    catch (Exception exception)
                    {
                        System.out.println(exception.getMessage());
                    }

                });

            }
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + e.getMessage()).encodePrettily());
            }

        });

    }

    private static void run(Router router)
    {

        router.get("/run/:id").handler(context -> {

            int id = Integer.parseInt(context.pathParam("id"));

            HttpServerResponse response = context.response();

            try
            {

                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                eventBus.<JsonObject>request(DISCOVER_ADDRESS, new JsonObject().put(DISCOVERY_ID_KEY, id).put(REQUEST_TYPE, DISCOVERY_RUN), replyHandler -> {

                    JsonObject reply = replyHandler.result().body();

                    if (reply.getString(STATUS).equals(STATUS_SUCCESS))
                    {
                        JsonObject outputData = replyHandler.result().body();

                        vertx.executeBlocking(promise -> {

                            JsonObject result = discovery(outputData);

                            if (result.getString(STATUS).equals(STATUS_SUCCESS))
                            {
                                promise.complete(outputData.put("system.name", result.getString("system.name")));
                            }
                            else
                                promise.fail("Device is not available or unreachable : " + result.getString(MESSAGE));


                        }, false, asyncResult -> {

                            if (asyncResult.succeeded())
                            {

                                JsonObject updatedDiscoverProfile = (JsonObject) asyncResult.result();

                                updatedDiscoverProfile.put(IS_DISCOVERED, TRUE);

                                eventBus.request(DISCOVER_ADDRESS, updatedDiscoverProfile.put(REQUEST_TYPE, DISCOVERY_UPDATE)).onComplete(task -> {

                                    if (asyncResult.succeeded())
                                    {
                                        response.setStatusCode(200);
                                        response.end(Util.setSuccessResponse("Discovery Successful").put("system.name", updatedDiscoverProfile.getString("system.name")).encodePrettily());
                                    }
                                    else
                                    {
                                        response.setStatusCode(400);
                                        response.end(Util.setFailureResponse("Discovery Failed... Discovery Profile not updated").put("system.name", updatedDiscoverProfile.getString("system.name")).encodePrettily());
                                    }
                                });

                            }
                            else
                            {
                                response.setStatusCode(400);
                                response.end(Util.setFailureResponse(asyncResult.cause().getMessage()).encodePrettily());

                            }
                        });
                    }
                    else{
                        response.setStatusCode(400);
                        response.end(Util.setFailureResponse(reply.getString(MESSAGE)).encodePrettily());
                    }

                });

            }
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + e.getMessage()).encodePrettily());
            }
        });

    }

    private static void put(Router router)
    {

        router.put("/:id").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            try
            {

                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                int id;

                if (Util.validNumeric(context.pathParam("id")))
                {
                    id = Integer.parseInt(context.pathParam("id"));

                    JsonObject inputData = context.body().asJsonObject();

                    String error = Util.validateBody(inputData, DISCOVER_ADDRESS);

                    if (error.equals(""))
                    {
                        inputData.put(REQUEST_TYPE, DISCOVERY_PUT).put(DISCOVERY_ID_KEY, id);

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

                }
                else
                {
                    response.setStatusCode(400);
                    response.end(Util.setFailureResponse("Invalid Discovery Id").encodePrettily());
                }



            }
            catch (DecodeException exception){
                response.setStatusCode(400);
                response.end(Util.setFailureResponse("Invalid Json Body!").encodePrettily());
            }
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + e.getMessage()).encodePrettily());
            }

        });

    }

    private static void delete(Router router)
    {

        router.delete("/:id").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            try
            {

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
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + e.getMessage()).encodePrettily());
            }

        });

    }

    private static void get(Router router)
    {
        router.get("/:id").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            int id;

            if (Util.validNumeric(context.pathParam("id")))
            {
                id = Integer.parseInt(context.pathParam("id"));

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
                                response.end(Util.setFailureResponse(resultData.getString(MESSAGE)).encodePrettily());
                            }
                        }
                        else
                        {
                            response.setStatusCode(500);
                            response.end(Util.setFailureResponse("Internal Server Error : " + result.cause().getMessage()).encodePrettily());
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
                response.end(Util.setFailureResponse("Invalid discovery Id").encodePrettily());
            }


        });
    }

}
