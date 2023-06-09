package com.snmp.server.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;


public class MainRouter extends AbstractVerticle
{

    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {

        var server = vertx.createHttpServer();

        EventBus eventBus = vertx.eventBus();

        var router = Router.router(vertx);

        router.route("/credential/*")
            .subRouter(new CredentialHandler(vertx, eventBus).initializeRouter());

        router.route("/discovery/*")
            .subRouter(new DiscoveryHandler(vertx, eventBus).initializeRouter());

        router.route("/provision/*")
            .subRouter(new ProvisionHandler(vertx, eventBus).initializeRouter());


        server.requestHandler(router).listen(8080, ready -> {

            if (ready.succeeded())
            {

                System.out.println("Server Started Listening!");

                startPromise.complete();
            }
            else
            {
                startPromise.fail("Failed to start server " + ready.cause().getMessage());
            }
        });


    }

}
