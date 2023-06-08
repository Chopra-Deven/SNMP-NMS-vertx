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


                response.putHeader("content-type", "application/json");

                eventBus.request(PROVISION_ADDRESS, new JsonObject().put("discoveryId", id).put(REQUEST_TYPE, PROVISION_RUN), replyHandler -> {

                    JsonObject reply = (JsonObject) replyHandler.result().body();

                    response.end(reply.encodePrettily());
                });

            }
            catch (Exception e){
                response.end(Util.getFailureResponse("Provision Failed : " + e.getMessage()).encodePrettily());
            }
        });

        return router;
    }
}
