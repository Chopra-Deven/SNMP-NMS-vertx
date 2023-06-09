package com.snmp.server.api;

import com.snmp.server.util.Constants;
import com.snmp.server.util.DiscoveryUtil;
import com.snmp.server.util.Util;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static com.snmp.server.util.Constants.*;


public class ProvisionHandler
{
    private final Vertx vertx;

    EventBus eventBus;

    private static final DiscoveryUtil discoveryUtil = new DiscoveryUtil();

    public ProvisionHandler(Vertx vertx, EventBus eventBus)
    {

        this.vertx = vertx;

        this.eventBus = eventBus;
    }

    public Router initializeRouter()
    {
        System.out.println("Provision Init Called");

        Router router = Router.router(vertx);

        router.get("/run/:id").handler(context -> {

            HttpServerResponse response = context.response();

            try
            {

                int id = Integer.parseInt(context.pathParam("id"));


                response.putHeader(CONTENT_TYPE, APPLICATION_JSON);

                eventBus.request(PROVISION_ADDRESS, new JsonObject().put(DISCOVERY_ID_KEY, id).put(REQUEST_TYPE, PROVISION_RUN), replyHandler -> {

                    JsonObject reply = (JsonObject) replyHandler.result().body();

                    response.end(reply.encodePrettily());
                });

            }
            catch (Exception e){
                response.end(Util.getFailureResponse("Provision Failed : " + e.getMessage()).encodePrettily());
            }
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

            eventBus.<JsonObject>request(CREDENTIAL_ADDRESS, new JsonObject().put(PROVISION_ID_KEY, id).put(REQUEST_TYPE, PROVISION_DELETE), result -> {

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
                        response.end(Util.setFailureResponse("Internal Server Error : " + result.cause()).encodePrettily());
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
