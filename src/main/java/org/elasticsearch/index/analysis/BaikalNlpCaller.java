package org.elasticsearch.index.analysis;

import baikal.ai.AnalyzeSyntaxRequest;
import baikal.ai.AnalyzeSyntaxResponse;
import baikal.ai.Document;
import baikal.ai.LanguageServiceGrpc;
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
// import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public class BaikalNlpCaller {
    LanguageServiceGrpc.LanguageServiceBlockingStub client;
    private final static Logger LOGGER = Logger.getGlobal();
    private List<String> stopTokens; // tokenMorphemes, 
    final String DEF_STOP_TOKENS =  "E,IC,J,MAG,MAJ,MM,SP,SSC,SSO,SC,SE,XPN,XSA,XSN,XSV,UNA,NA,VSV";
    final int DEF_PORT = 5656;
    final String DEF_ADDRESS = "localhost";
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
                    ip = p.getProperty("NLP_server_address" , DEF_ADDRESS); 
                    port = Integer.parseInt(p.getProperty("NLP_server_port", String.valueOf(DEF_PORT) )); 
                    // String strs = p.getProperty("token_morphemes",  "NNP,NNG,NNB,NF,VV,SL,SH,SN"); 
                    // tokenMorphemes = new ArrayList<String>(Arrays.asList(strs.split(",")));

                    String strs = p.getProperty("stop_tokens",  DEF_STOP_TOKENS); 
                    stopTokens = new ArrayList<String>(Arrays.asList(strs.split(",")));

                   

                } else {                    
                    ip = DEF_ADDRESS; 
                    port = DEF_PORT;
                    stopTokens = new ArrayList<String>(Arrays.asList(DEF_STOP_TOKENS.split(",")));
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

    boolean inIn(String s, List<String> list) {
        if( s == null || s == "" ) return false;
        for (String s2 : list) {
            if ( s.startsWith(s2) ) return true;
        }
        return false;
    }

    public String isEsToken(String morpheme) {
        if( morpheme == null || morpheme == "" ) return "UNKOWN";
        return !inIn(morpheme, stopTokens) ? morpheme : "";
    }

    public void shutdownChannel() {
        channel.shutdown();
    }
}
