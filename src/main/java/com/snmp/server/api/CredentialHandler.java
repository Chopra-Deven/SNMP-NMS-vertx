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


public class CredentialHandler
{

    public static Vertx vertx;

    public static EventBus eventBus;

    public CredentialHandler(Vertx vertx, EventBus eventBus)
    {

        CredentialHandler.vertx = vertx;

        CredentialHandler.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        Router router = Router.router(vertx);

        post(router);

        getAll(router);

        get(router);

        put(router);

        delete(router);

        return router;
    }

    private static void post(Router router)
    {

        router.post("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            try
            {

                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                JsonObject inputData = context.body().asJsonObject();

                String error = Util.validateBody(inputData, CREDENTIAL_ADDRESS);

                if (error.equals(""))
                {
                    inputData.put(Constants.REQUEST_TYPE, Constants.CREDENTIAL_POST);

                    eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, inputData, result -> {

                        try
                        {
                            if (result.succeeded())
                            {
                                JsonObject resultData = result.result().body();

                                if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                                {
                                    response.setStatusCode(200);
                                    response.end(resultData.encodePrettily()    );
                                }
                                else
                                {
                                    response.end(Util.setFailureResponse(resultData.getString(Constants.MESSAGE)).encodePrettily());
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
            catch (DecodeException exception){
                response.setStatusCode(400);
                response.end(Util.setFailureResponse("Invalid Json Body!").encodePrettily());
            }
            catch (Exception e)
            {

                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal server error : " + e.getMessage()).encodePrettily());
            }

        });
    }

    private static void getAll(Router router)
    {

        router.get("/").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            try
            {
                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, new JsonObject().put(REQUEST_TYPE, CREDENTIAL_GET_ALL)).onComplete(result -> {

                    if (result.succeeded())
                    {
                        JsonObject resultData = result.result().body();

                        if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                        {
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
                        response.setStatusCode(500);
                        response.end(Util.setFailureResponse("Internal Server Error : " + result.cause().getMessage()).encodePrettily());
                    }

                }); // end of eventbus
            }
            catch (Exception exception)
            {
                response.setStatusCode(500);
                response.end(Util.setFailureResponse("Internal Server Error : " + exception.getMessage()).encodePrettily());
            }
        });

    }

    private static void get(Router router)
    {

        router.get("/:id").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            int id = 0;

            if (Util.validNumeric(context.pathParam("id")))
            {
                id = Integer.parseInt(context.pathParam("id"));

                eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, new JsonObject().put(CREDENTIAL_ID_KEY, id).put(REQUEST_TYPE, CREDENTIAL_GET), result -> {

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
                response.end(Util.setFailureResponse("Invalid Credential Id").encodePrettily());
            }


        });


    }

    private static void put(Router router)
    {

        router.put("/:id").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            try
            {

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

                JsonObject inputData = context.body().asJsonObject();

                String error = Util.validateBody(inputData, CREDENTIAL_ADDRESS);

                if (error.equals(""))
                {

                    inputData.put(Constants.REQUEST_TYPE, CREDENTIAL_PUT).put(CREDENTIAL_ID_KEY, id);

                    eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, inputData, result -> {

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
                    response.end(Util.setFailureResponse(Util.validateBody(inputData, CREDENTIAL_ADDRESS)).encodePrettily());
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

                eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, new JsonObject().put(CREDENTIAL_ID_KEY, id).put(REQUEST_TYPE, CREDENTIAL_DELETE), result -> {

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

}
