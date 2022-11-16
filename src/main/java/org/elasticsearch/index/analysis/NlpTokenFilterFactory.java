package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class NlpTokenFilterFactory extends AbstractTokenFilterFactory {
    public NlpTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
    }
    @Override
    public TokenStream create(TokenStream stream) {
        return new NlpTokenFilter(stream);
    }
}
