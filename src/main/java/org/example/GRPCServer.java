package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.services.FileServiceIMPL;

public class GRPCServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50051)
                .addService(new FileServiceIMPL())
                .build()
                .start();
        System.out.println("gRPC server started on port: " + server.getPort());
        server.awaitTermination();
    }
}