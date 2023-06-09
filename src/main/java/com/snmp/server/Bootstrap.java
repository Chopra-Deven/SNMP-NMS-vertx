package com.snmp.server;


import com.snmp.server.api.MainRouter;
import com.snmp.server.api.PollingHandler;
import com.snmp.server.database.DatabaseHandler;
import io.vertx.core.Vertx;


public class Bootstrap
{

    public static void main(String[] args)
    {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainRouter());
        vertx.deployVerticle(new DatabaseHandler());
        vertx.deployVerticle(new PollingHandler());
    }

}
