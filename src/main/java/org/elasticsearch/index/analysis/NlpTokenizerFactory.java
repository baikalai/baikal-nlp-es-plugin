package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class NlpTokenizerFactory extends AbstractTokenizerFactory {
    IndexSettings indexSettings;
    Environment environment;
    Settings settings;

    public NlpTokenizerFactory(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        super(indexSettings, settings, s);
        this.indexSettings = indexSettings;
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public Tokenizer create() {
        return new NlpTokenizer(indexSettings, environment, settings);
    }
}
