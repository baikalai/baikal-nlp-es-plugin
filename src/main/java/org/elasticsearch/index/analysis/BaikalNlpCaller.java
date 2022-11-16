package org.elasticsearch.index.analysis;

import baikal.ai.AnalyzeSyntaxRequest;
import baikal.ai.AnalyzeSyntaxResponse;
import baikal.ai.Document;
import baikal.ai.LanguageServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public class BaikalNlpCaller {
    LanguageServiceGrpc.LanguageServiceBlockingStub client;
    private final static Logger LOGGER = Logger.getGlobal();
    private List tokenMorphemes;
    /*
    Boolean isTest = false;
    String configPath;
    */
    Boolean isTest = true;
    String configPath = "";
    String ip;
    int port;
    ManagedChannel channel;
    final String CONFIG_FILE =  "/usr/share/elasticsearch/data/config.properties";

    public BaikalNlpCaller() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                if (!isTest) {
                    String pathToRead = configPath;
                    if (pathToRead.isEmpty()) {
                        pathToRead = CONFIG_FILE;
                    }
                    File path = new File(pathToRead);
                    FileReader file = new FileReader(path);
                
                    Properties p = new Properties();			
                    p.load(file); // 파일 열어줌
                    ip = p.getProperty("NLP_server_address" , "localhost"); 
                    port = Integer.parseInt(p.getProperty("NLP_server_port", "8161")); 
                    String strs = p.getProperty("token_morphemes",  "NNP,NNG,NNB,NF,VV,SL,SH,SN"); 
                    tokenMorphemes = new ArrayList<String>(Arrays.asList(strs.split(",")));
                    
                   

                } else {
                    String strs = "NNP,NNG,NNB,NF,VV,SL,SH,SN";
                    ip = "localhost";
                    port = 8261;
                    tokenMorphemes = new ArrayList<String>(Arrays.asList(strs.split(",")));
                }
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
                return null;
            }
            //LOGGER.setLevel(Level.INFO);
            //LOGGER.info("Client is ok");
            return null;
        });
    }

    public AnalyzeSyntaxResponse send(String text) {
        return AccessController.doPrivileged((PrivilegedAction<AnalyzeSyntaxResponse>) () -> {
            AnalyzeSyntaxResponse response = null;
            try {
                channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
                client = LanguageServiceGrpc.newBlockingStub(channel);
                //LOGGER.setLevel(Level.INFO);
                //LOGGER.info(text);
                Document document = Document.newBuilder().setContent(text).setLanguage("ko-KR").build();
                AnalyzeSyntaxRequest request = AnalyzeSyntaxRequest.newBuilder().setDocument(document).build();
                response = client.analyzeSyntax(request);

            } catch (StatusRuntimeException e) {
                LOGGER.warning(e.getMessage());
                LOGGER.warning(text);
                return null;
            } finally {
                channel.shutdown();
            }
            return response;
        });
    }

    public String isEsToken(String morpheme) {
        int at = this.tokenMorphemes.indexOf(morpheme);
        if (at > -1) {
            return this.tokenMorphemes.get(at).toString();
        }
        return "";
    }

    public void shutdownChannel() {
        channel.shutdown();
    }
}
