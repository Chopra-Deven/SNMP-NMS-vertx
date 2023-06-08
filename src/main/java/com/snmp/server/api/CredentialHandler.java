package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.CredentialUtil;
import com.snmp.server.util.Util;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import static com.snmp.server.util.Constants.*;


public class CredentialHandler
{

    private final Vertx vertx;

    EventBus eventBus;

    private final CredentialUtil credentialUtil = new CredentialUtil();

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

            response.putHeader("content-type", "application/json");

            JsonObject inputData = context.body().asJsonObject();

            if (credentialUtil.isValidInput(inputData))
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
                                response.end(Util.getSuccessResponse("Credential Profile Created.").encode());
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
            }
            else
                response.end( Util.getFailureResponse("Invalid Inputs").encodePrettily());

        });

        router.get("/").handler(BodyHandler.create()).handler(context -> {

            context.response().setChunked(true);

            HttpServerResponse response = context.response();

            response.putHeader("content-type", "application/json");

            eventBus.request(CREDENTIAL_ADDRESS, new JsonObject().put(REQUEST_TYPE, CREDENTIAL_GET_ALL)).onComplete(result -> {
                System.out.println("Get request sent to eventbus");

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



        return router;
    }

}
