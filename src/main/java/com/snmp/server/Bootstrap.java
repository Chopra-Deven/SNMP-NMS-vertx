package com.snmp.server;


import com.snmp.server.api.MainRouter;
import com.snmp.server.polling.PollingHandler;
import com.snmp.server.database.DatabaseHandler;
import io.vertx.core.Vertx;


public class Bootstrap
{

    public static void main(String[] args)
    {

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(MainRouter.class.getName())
            .compose(deployDatabase -> vertx.deployVerticle(DatabaseHandler.class.getName()))
            .compose(deployPoller -> vertx.deployVerticle(PollingHandler.class.getName()))
            .onComplete(result -> {

            if (result.succeeded())

                System.out.println("All verticals are deployed");

            else
                System.out.println("Failed to deploy : " + result.cause().getMessage());

        });

    }

}
