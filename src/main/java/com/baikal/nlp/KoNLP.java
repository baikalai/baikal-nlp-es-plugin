package com.baikal.nlp;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import baikal.ai.AnalyzeSyntaxResponse;
import baikal.ai.Morpheme;
import baikal.ai.Sentence;
import baikal.ai.Token;

public class KoNLP {
    protected Connector conn;
    protected String text;
    private final static Logger LOGGER = Logger.getGlobal();

    KoNLP(String str, String ip, int port) {
        conn = new Connector(ip, port);
        text = str;

        if( text != null && !text.isEmpty()) {
            _parse(text); 
        }
    }

    private void _parse(String str) {
        text = str;
       
        conn.send(text);
    }
    String analyze(String str) {
        _parse(str);
        return analyze();
    }

    String analyze() {
        String ret = conn.toJson();

        LOGGER.info(ret);
        return ret;
    }

    private List<Morpheme> getMorphemes(AnalyzeSyntaxResponse response) {
        List<Morpheme> ret = new ArrayList<Morpheme>();

        if( response == null ) return ret;
        List<Sentence> sentences = response.getSentencesList();
        
        for( Sentence sentence : sentences) {
            for( Token t : sentence.getTokensList() ) {
                for( Morpheme morpheme : t.getMorphemesList()) {
                    ret.add(morpheme);
                }
            }
        }
        return ret;
    }

    private List<Morpheme> getMorphemes() {
        return getMorphemes(conn.get());
    }

    public String nouns(String str) {
        _parse(str);

        List<Morpheme> morphemes = getMorphemes();

    }

}
