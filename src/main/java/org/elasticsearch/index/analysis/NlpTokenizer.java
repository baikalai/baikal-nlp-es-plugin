package org.elasticsearch.index.analysis;

import baikal.ai.AnalyzeSyntaxResponse;
import baikal.ai.Morpheme;
import baikal.ai.Sentence;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Stream;

// import static java.util.stream.Collectors.toList;

final class MyToken {
    public String text;
    public int beginOffset;
    public int length;
    public String type;
    public boolean isLast;
    public boolean isEmpty;
}

public final class NlpTokenizer extends Tokenizer {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private Boolean doneGetInputString = false;
    //private String usingRawString = "";
    private int tokenIndex = 0;
    private int sentenceIndex = 0;
    //private Boolean morphemeCheckDoneOnTokens = false;
    BaikalNlpCaller caller;
    List<Sentence> sentences;
    ArrayList<MyToken> tokens;

    public AnalyzeSyntaxResponse getInputString() throws IOException {
        int readDone = -1;
        String text = "";
        do {
            final CharBuffer buffer = CharBuffer.allocate(1024);
            buffer.compact();
            ((Buffer) buffer).clear();
            readDone = input.read(buffer);
            doneGetInputString = true;
            text += new String(buffer.array());
        } while (readDone != -1);

        text = text.trim();
        //usingRawString = text;
        return caller.send(text);
    }

    public NlpTokenizer() {
        super(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
        caller = new BaikalNlpCaller();
    }

    private boolean isLastSentence() {
        if (sentences.size() == sentenceIndex + 1) {
            return true;
        }
        return false;
    }

    private boolean isLastToken() {
        if (tokens.size() <= tokenIndex) {
            return true;
        }
        return false;
    }

    private void makeTokens() {
        tokens = new ArrayList<MyToken>();
        //System.out.println(sentences);
        for (int i = 0; i < sentences.get(sentenceIndex).getTokensCount(); i++) {
            /*MyToken temp = new MyToken();
            temp.text = sentences.get(sentenceIndex).getTokens(i).getText().getContent();
            temp.beginOffset = sentences.get(sentenceIndex).getTokens(i).getText().getBeginOffset();
            temp.length = temp.text.length();
            temp.type = "segment";
            tokens.add(temp);*/
            for (int j = 0; j < sentences.get(sentenceIndex).getTokens(i).getMorphemesCount(); j++) {
                Morpheme morpheme = sentences.get(sentenceIndex).getTokens(i).getMorphemes(j);
                String text = morpheme.getText().getContent();
                String tag = morpheme.getTag().name();
                //System.out.println(text + " " + tag);
                String tokenName = caller.isEsToken(tag);
                if (!tokenName.isEmpty()) {
                    MyToken temp2 = new MyToken();
                    temp2.text = morpheme.getText().getContent();
                    temp2.beginOffset = morpheme.getText().getBeginOffset();
                    temp2.length = temp2.text.length();
                    temp2.type = tokenName;
                    temp2.isLast = false;
                    temp2.isEmpty = false;
                    tokens.add(temp2);
                }
            }
        }
    }

    private MyToken nextToken() {
        if (isLastToken()) {
            if (isLastSentence()) {
                MyToken temp = new MyToken();
                temp.isLast = true;
                return temp;
            }
            tokenIndex = 0;
            sentenceIndex++;
            makeTokens();
        }
        if (!tokens.isEmpty() && tokens.size() > tokenIndex) {
            return tokens.get(tokenIndex++);
        } else {
            MyToken temp = new MyToken();
            temp.isEmpty = true;
            return temp;
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        // TODO proxy로 grpc 보내고 결과를 tokenizing 해서 token filter로 보내기
        clearAttributes();
        if (!doneGetInputString) {
            sentences = null;
            AnalyzeSyntaxResponse temp = getInputString();
            if (temp == null) return false;

            List<Sentence> tempList = temp.getSentencesList();
            sentences = tempList;
            if (sentences == null || sentences.size() == 0) return false;
            makeTokens();
        }
        MyToken item = nextToken();
        if (item.isLast) {
            return false;
        } else {
            while(item.isEmpty) {
                item = nextToken();
            }
            if (item.isLast) {
                return false;
            }
            termAtt.append(item.text);
            typeAtt.setType(item.type);
            offsetAtt.setOffset(item.beginOffset, item.beginOffset + item.length);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        super.close();
        doneGetInputString = false;
        tokenIndex = 0;
        sentenceIndex = 0;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        doneGetInputString = false;
        tokenIndex = 0;
        sentenceIndex = 0;
    }

    @Override
    public void end() throws IOException {
        super.end();
        doneGetInputString = false;
        tokenIndex = 0;
        sentenceIndex = 0;
    }
}
