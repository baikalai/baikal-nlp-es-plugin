package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class NlpTokenizerFactory extends AbstractTokenizerFactory {
    public NlpTokenizerFactory(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        super(indexSettings, settings, s);
    }

    @Override
    public Tokenizer create() {
        return new NlpTokenizer();
    }
}
