package com.baikal.nlp;

import bareun.ai.AnalyzeSyntaxRequest;
import bareun.ai.AnalyzeSyntaxResponse;
import bareun.ai.Document;
import bareun.ai.LanguageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

// import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
// import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.util.JsonFormat;

public class Connector {
    LanguageServiceGrpc.LanguageServiceBlockingStub client;
    private final static Logger LOGGER = Logger.getGlobal();
    protected String ip;
    protected int port;
    protected ManagedChannel channel;
    protected AnalyzeSyntaxResponse lastResponse;

    public final static int DEF_PORT = 5656;
    public final static String DEF_ADDRESS = "nlp.bareun.ai"; // "10.3.8.44";

    public Connector() {
        this(DEF_ADDRESS, DEF_PORT)   ;
    }

    public Connector(String ip) {
        this(ip, DEF_PORT)   ;
    }


    public Connector(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public AnalyzeSyntaxResponse send(String text) {   
        lastResponse = null;
        if( text == null || text.isEmpty() ) return lastResponse;
        lastResponse = AccessController.doPrivileged((PrivilegedAction<AnalyzeSyntaxResponse>) () -> {
            AnalyzeSyntaxResponse response = null;
            try {
                channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
                client = LanguageServiceGrpc.newBlockingStub(channel);
                LOGGER.setLevel(Level.INFO);
                LOGGER.info("analyze - '"+text+"'");
                Document document = Document.newBuilder().setContent(text).setLanguage("ko-KR").build();
                AnalyzeSyntaxRequest request = AnalyzeSyntaxRequest.newBuilder().setDocument(document).build();
                response = client.analyzeSyntax(request);

            } catch (StatusRuntimeException e) {
                e.printStackTrace();
                LOGGER.warning(e.getMessage());
                LOGGER.warning(text);
                return null;
            } finally {
                channel.shutdown();
            }
            return response;
        });
        return lastResponse;
    }

    public AnalyzeSyntaxResponse get() { return lastResponse; }

    public void shutdownChannel() {
        channel.shutdown();
    }

    public String toJson() {
        String jsonString = "";
        
        if( lastResponse == null ) return jsonString;
        try {
            jsonString = JsonFormat.printer().includingDefaultValueFields().print(lastResponse);            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return jsonString;       
    } 
}
