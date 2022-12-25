package org.elasticsearch.index.analysis;

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
import java.util.Properties;

public class BaikalNlpCaller {
    LanguageServiceGrpc.LanguageServiceBlockingStub client;
    private final static Logger LOGGER = Logger.getGlobal();
    private List<String> stopTokens; // tokenMorphemes, 
    final static String DEF_STOP_TOKENS =  "E,IC,J,MAG,MAJ,MM,NA,NF,NV,SE,SF,SO,SP,SS,SW,VC,VX,XPN,XS";
    final static int DEF_PORT = 5656;
    final static String DEF_ADDRESS = "localhost"; // "10.3.8.44";
    /*
    Boolean isTest = false;
    String configPath;
    */
    final static Boolean isTest = false;
    String configPath = "";
    String ip;
    int port;
    ManagedChannel channel;
    final static String CONFIG_FILE =  "/usr/share/elasticsearch/data/config.properties";

    public static class NlpSettings {
        public String ip;
        public int port;
        public List<String> stopTokens;
        public NlpSettings(String ip, int port, List<String> stopTokens ) {
            this.ip = ip;
            this.port = port;
            this.stopTokens = stopTokens;
        }
    }

    public static NlpSettings getSettingsFromConfig() {
        return getSettingsFromConfig("");
    }
    public static NlpSettings getSettingsFromConfig(String configPath) {
        return AccessController.doPrivileged((PrivilegedAction<NlpSettings>) () -> {
            try {
                if (!isTest) {
                    String pathToRead = configPath;
                    if (pathToRead.isEmpty()) {
                        pathToRead = CONFIG_FILE;
                    }
                    try {
                        File path = new File(pathToRead);
                        FileReader file = new FileReader(path);
                    
                        Properties p = new Properties();			
                        p.load(file); // 파일 열어줌
                        String ip = p.getProperty("nlp_server_address" , DEF_ADDRESS); 
                        int port = Integer.parseInt(p.getProperty("nlp_server_port", String.valueOf(DEF_PORT) )); 
                        // String strs = p.getProperty("token_morphemes",  "NNP,NNG,NNB,NF,VV,SL,SH,SN"); 
                        // tokenMorphemes = new ArrayList<String>(Arrays.asList(strs.split(",")));
            
                        String strs = p.getProperty("stoptags",  DEF_STOP_TOKENS); 
                        ArrayList<String> stopTokens = new ArrayList<String>(Arrays.asList(strs.split(",")));
                        return new NlpSettings(ip, port, stopTokens);
                    } catch(Exception e) {
                        LOGGER.warning(e.getMessage());
                        return new NlpSettings(DEF_ADDRESS, DEF_PORT, new ArrayList<String>(Arrays.asList(DEF_STOP_TOKENS.split(","))));    
                    }
        
                } else {
                    return new NlpSettings(DEF_ADDRESS, DEF_PORT, new ArrayList<String>(Arrays.asList(DEF_STOP_TOKENS.split(","))));                    
                }                
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
                return null;
            }

        });
        
    }

    public BaikalNlpCaller() {
        this(getSettingsFromConfig());
    }

    public BaikalNlpCaller(NlpSettings settings) {
        ip = settings.ip;
        port = settings.port;
        stopTokens = settings.stopTokens;
        LOGGER.info(String.format("NLP SERVER - %s:%d", ip, port));
    }

    public AnalyzeSyntaxResponse send(String text) {
        
        return AccessController.doPrivileged((PrivilegedAction<AnalyzeSyntaxResponse>) () -> {
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
