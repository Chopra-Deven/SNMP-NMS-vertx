package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Objects;

import static com.snmp.server.util.Constants.*;


public class CredentialHandler
{

    private final Vertx vertx;

    EventBus eventBus;

    //    private final CredentialUtil credentialUtil = new CredentialUtil();

    public CredentialHandler(Vertx vertx, EventBus eventBus)
    {

        this.vertx = vertx;

        this.eventBus = eventBus;
    }

    public Router initializeRouter()
    {

        Router router = Router.router(vertx);

        router.post("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            JsonObject inputData = context.body().asJsonObject();

            if (Objects.equals(Util.validateBody(inputData, CREDENTIAL_ADDRESS), ""))
            {
                inputData.put(Constants.REQUEST_TYPE, Constants.CREDENTIAL_POST);

                eventBus.request(CREDENTIAL_ADDRESS, inputData, result -> {

                    try
                    {
                        if (result.succeeded())
                        {
                            JsonObject resultData = (JsonObject) result.result().body();

                            if (resultData.getString(Constants.STATUS).equals(Constants.STATUS_SUCCESS))
                            {
                                response.setStatusCode(200);
                                response.end(Util.setSuccessResponse("Credential Profile Created.").encode());
                            }
                            else
                            {
                                response.end(Util.getFailureResponse(resultData.getString(Constants.MESSAGE)).encodePrettily());
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

        });

        // handler(BodyHandler.create())
        router.get("/").handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

            eventBus.request(CREDENTIAL_ADDRESS, new JsonObject().put(REQUEST_TYPE, CREDENTIAL_GET_ALL)).onComplete(result -> {
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
                            response.setStatusCode(502);
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
                response.end(Util.setFailureResponse("Invalid Credential Id").encodePrettily());
            }

            JsonObject inputData = context.body().asJsonObject();

            if (Objects.equals(Util.validateBody(inputData, CREDENTIAL_ADDRESS), ""))
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

            }
            else
            {
                response.setStatusCode(400);
                response.end(Util.getFailureResponse(Util.validateBody(inputData, CREDENTIAL_ADDRESS)).encodePrettily());
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

        router.delete("/:id").handler(context -> {

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

}
