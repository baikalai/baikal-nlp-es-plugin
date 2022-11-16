package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.*;

import java.io.Reader;

public class BaikalAnalyzer extends Analyzer {
    public BaikalAnalyzer() {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        Tokenizer tokenizer = new NlpTokenizer();
        TokenStream stream = new NlpTokenFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, stream);
    }
}
