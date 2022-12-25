package org.elasticsearch.index.analysis;

// import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
// import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class NlpAnalyzerProvider extends AbstractIndexAnalyzerProvider<BaikalAnalyzer> {
    private final BaikalAnalyzer analyzer;
    public NlpAnalyzerProvider(IndexSettings indexSettings, Environment environment, String s, Settings settings) {
        super(s, settings);
        analyzer = new BaikalAnalyzer();
    }

    @Override
    public BaikalAnalyzer get() {
        return analyzer;
    }
}
