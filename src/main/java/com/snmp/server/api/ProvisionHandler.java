package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import static com.snmp.server.util.Constants.*;


public class ProvisionHandler
{

    public static Vertx vertx;

    public static EventBus eventBus;

    public ProvisionHandler(Vertx vertx, EventBus eventBus)
    {

        ProvisionHandler.vertx = vertx;

        ProvisionHandler.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        Router router = Router.router(vertx);

        run(router);

        delete(router);

        getAll(router);

        get(router);

        return router;
    }

    private static void run(Router router)
    {

        router.get("/run/:id").handler(context -> {

            HttpServerResponse response = context.response();

            try
            {
                if (Util.validNumeric(context.pathParam("id")))
                {
                    int id = Integer.parseInt(context.pathParam("id"));

                    response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                    eventBus.request(PROVISION_ADDRESS, new JsonObject().put(DISCOVERY_ID_KEY, id).put(REQUEST_TYPE, PROVISION_RUN), replyHandler -> {

                        JsonObject reply = (JsonObject) replyHandler.result().body();

                        response.end(reply.encodePrettily());
                    });
                }
                else
                {
                    response.setStatusCode(400);
                    response.end(Util.setFailureResponse("Invalid provision Id").encodePrettily());
                }
            }
            catch (Exception e)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Provision Failed : " + e.getMessage()).encodePrettily());
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

                int id;

                if (Util.validNumeric(context.pathParam("id")))
                {
                    id = Integer.parseInt(context.pathParam("id"));

                    eventBus.<JsonObject>request(PROVISION_ADDRESS, new JsonObject().put(PROVISION_ID_KEY, id).put(REQUEST_TYPE, PROVISION_DELETE), result -> {

                        try
                        {
                            if (result.succeeded())
                            {
                                JsonObject resultData = result.result().body();

                                if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
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
                                response.end(Util.setFailureResponse("Internal Server Error : " + result.cause()).encodePrettily());
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
                    response.end(Util.setFailureResponse("Invalid Credential Id").encodePrettily());
                }


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

                eventBus.<JsonObject>request(PROVISION_ADDRESS, new JsonObject().put(REQUEST_TYPE, PROVISION_GET_ALL)).onComplete(result -> {

                    try
                    {
                        if (result.succeeded())
                        {
                            JsonObject resultData = result.result().body();

                            if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                            {
                                response.end(resultData.encodePrettily());
                            }
                            else
                            {
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

                eventBus.<JsonObject>request(PROVISION_ADDRESS, new JsonObject().put(PROVISION_ID_KEY, id).put(REQUEST_TYPE, PROVISION_GET), result -> {

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
