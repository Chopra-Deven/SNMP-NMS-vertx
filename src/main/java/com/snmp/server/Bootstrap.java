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

        vertx.deployVerticle(new MainRouter()).onComplete(handler -> {

            if (handler.succeeded()){
                System.out.println("Main vertical deployed");
            }
            else
                System.out.println(handler.cause().getMessage());
        });
        vertx.deployVerticle(new DatabaseHandler()).onComplete(handler -> {

            if (handler.succeeded()){
                System.out.println("Database handler deployed");
            }
            else
                System.out.println(handler.cause().getMessage());
        });
        vertx.deployVerticle(new PollingHandler()).onComplete(handler -> {

            if (handler.succeeded()){
                System.out.println("Polling handler deployed");
            }
            else
                System.out.println(handler.cause().getMessage());
        });
    }

}
