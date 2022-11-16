package org.elasticsearch.index.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestBaikalAnalyzer extends TestCase {
    public void testBaikalAnalyzer() throws Exception {
        String input = "아버지가 방에 들어가신다\n어머니는 부엌에 들어가시지 않고 피자를 드신다";
        BaikalAnalyzer a = new BaikalAnalyzer();

        TokenStream ts = a.tokenStream("baikal", input);

        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);

        ts.reset();

        ArrayList<String> strs = new ArrayList();
        while (ts.incrementToken()) {
            strs.add(termAtt.toString());
            System.out.println(termAtt.toString());
            System.out.println(typeAtt.type());
            System.out.println(offsetAtt.startOffset() + "," + offsetAtt.endOffset());
        }

        assertEquals(8, strs.size());

        BaikalAnalyzer a2 = new BaikalAnalyzer();

        TokenStream ts2 = a2.tokenStream("baikal", "너의 이름은 무엇이니?");

        CharTermAttribute termAtt2 = ts2.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt2 = ts2.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt2 = ts2.addAttribute(TypeAttribute.class);

        ts2.reset();
        strs = new ArrayList();
        while (ts2.incrementToken()) {
            strs.add(termAtt.toString());
            System.out.println(termAtt2.toString());
            System.out.println(typeAtt2.type());
            System.out.println(offsetAtt2.startOffset() + "," + offsetAtt2.endOffset());
        }
        assertEquals(1, strs.size());


        ts.end();
        ts.close();

        ts2.end();
        ts2.close();
    }

    public void testBaikalAnalyzer2() throws Exception {
        String input = "아버지가 방에 들어가신다\n어머니는 부엌에 들어가시지 않고 피자를 드신다";
        BaikalAnalyzer a = new BaikalAnalyzer();

        TokenStream ts = a.tokenStream("baikal", input);

        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);

        ts.reset();

        ArrayList<String> strs = new ArrayList();
        while (ts.incrementToken()) {
            strs.add(termAtt.toString());
            System.out.println(termAtt.toString());
            System.out.println(typeAtt.type());
            System.out.println(offsetAtt.startOffset() + "," + offsetAtt.endOffset());
        }

        assertEquals(8, strs.size());

        ts.end();
        ts.close();

        TokenStream ts2 = a.tokenStream("baikal", "너의 이름은 무엇이니?");

        CharTermAttribute termAtt2 = ts2.addAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAtt2 = ts2.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAtt2 = ts2.addAttribute(TypeAttribute.class);

        ts2.reset();
        strs = new ArrayList();
        while (ts2.incrementToken()) {
            strs.add(termAtt.toString());
            System.out.println(termAtt2.toString());
            System.out.println(typeAtt2.type());
            System.out.println(offsetAtt2.startOffset() + "," + offsetAtt2.endOffset());
        }
        assertEquals(1, strs.size());

        ts2.end();
        ts2.close();
    }

    public void test2() throws Exception {
        for (int i = 0; i < 1; i++) {
            String input = "신년사\n" +
                    "-----------------------------------------------------------------------------------------\n" +
                    "중구민";

            System.out.println((i + 1) + "th Test");

            int count = 0;
            try {
                BaikalAnalyzer a = new BaikalAnalyzer();


                TokenStream ts = a.tokenStream("baikal", input);

                CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
                OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
                TypeAttribute typeAtt = ts.addAttribute(TypeAttribute.class);

                ts.reset();

                ArrayList<String> strs = new ArrayList();
                while (ts.incrementToken()) {
                    strs.add(termAtt.toString());
                    if (offsetAtt.startOffset() < 0) {
                        System.out.println("Has Minus Offset");
                    }
                    System.out.println(termAtt.toString());
                    /*System.out.println(typeAtt.type());*/
                    System.out.println(offsetAtt.startOffset() + "," + offsetAtt.endOffset());
                }

                assertEquals(true, strs.size() > 0);
                count += strs.size();
                a.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("count : " + count);
        }
    }

    public void testMass() throws Exception {
        BufferedReader br = null;
        int docCount = 0;
        int titleCount = 0;
        int noneTokenCountOfDocument = 0;
        int noneTokenCountOfTitle = 0;
        try {
            br = Files.newBufferedReader(Paths.get("/Users/reddol18/dev/baikal.im/es_tools/gen_documents/files/2019/20190199.csv"));
            String line;
            boolean hasHead = false;
            String title = "";
            String text = "";
            while ((line = br.readLine()) != null) {
                if (!hasHead) {
                    hasHead = true;
                } else {
                    if (line.contains("␝")) {
                        if (text.length() > 0) {
                            BaikalAnalyzer a2 = new BaikalAnalyzer();
                            text = text.substring(1, text.length()-1);
                            TokenStream ts2 = a2.tokenStream("baikal", text);
                            CharTermAttribute termAtt2 = ts2.addAttribute(CharTermAttribute.class);
                            OffsetAttribute offsetAtt2 = ts2.addAttribute(OffsetAttribute.class);
                            TypeAttribute typeAtt2 = ts2.addAttribute(TypeAttribute.class);

                            ts2.reset();

                            //System.out.println(text);
                            int lastOffset = -1;
                            boolean hasFault = false;

                            ArrayList<String> strs2 = new ArrayList();
                            while (ts2.incrementToken()) {
                                strs2.add(termAtt2.toString());
                                if (lastOffset > 5400 && lastOffset > offsetAtt2.startOffset()) {
                                    System.out.println("D " + lastOffset + "," + offsetAtt2.startOffset());
                                    hasFault = true;
                                }
                                lastOffset = offsetAtt2.startOffset();
                            }
                            if (strs2.size() == 0) {
                                noneTokenCountOfDocument++;
                            }
                            if (hasFault) {
                                System.out.println(text);
                            }
                            //assertEquals(true, strs2.size() > 0);
                            docCount++;
                            text = "";
                        } else {
                            docCount++;
                        }
                        String array[] = line.split("␝");
                        if (array.length >= 2) {
                            title = array[1];
                        }
                        BaikalAnalyzer a1 = new BaikalAnalyzer();
                        TokenStream ts1 = a1.tokenStream("baikal", title);
                        CharTermAttribute termAtt = ts1.addAttribute(CharTermAttribute.class);
                        OffsetAttribute offsetAtt = ts1.addAttribute(OffsetAttribute.class);
                        TypeAttribute typeAtt = ts1.addAttribute(TypeAttribute.class);

                        ts1.reset();

                        //System.out.println(title);

                        ArrayList<String> strs = new ArrayList();
                        while (ts1.incrementToken()) {
                            strs.add(termAtt.toString());
                        }
                        if (title.length() > 0) {
                            //assertEquals(true, strs.size() > 0);
                            if (strs.size() == 0) {
                                noneTokenCountOfTitle++;
                            }
                            titleCount++;
                        }
                        if (array.length >= 3) {
                            text = array[2];
                        } else {
                            text = "";
                        }
                    } else {
                        text = text + "\n" + line;
                    }
                }
            }
            if (text.length() > 0) {
                BaikalAnalyzer a2 = new BaikalAnalyzer();
                TokenStream ts2 = a2.tokenStream("baikal", text);
                CharTermAttribute termAtt2 = ts2.addAttribute(CharTermAttribute.class);
                OffsetAttribute offsetAtt2 = ts2.addAttribute(OffsetAttribute.class);
                TypeAttribute typeAtt2 = ts2.addAttribute(TypeAttribute.class);

                ts2.reset();

                //System.out.println(text);

                ArrayList<String> strs2 = new ArrayList();
                while (ts2.incrementToken()) {
                    strs2.add(termAtt2.toString());
                }
                if (strs2.size() == 0) {
                    noneTokenCountOfDocument++;
                }
                assertEquals(true, strs2.size() > 0);
                docCount++;
                text = "";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(titleCount+","+docCount);
            System.out.println(noneTokenCountOfTitle+","+noneTokenCountOfDocument);
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
