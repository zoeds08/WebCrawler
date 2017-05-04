package io.bittiger.dedupe;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.*;

public class DedupeAds {

    final List<? extends Serializable> stop_words = Arrays.asList('.', ',', '"', "'", '?', '!', ':', ';', '(', ')', '[', ']', '{', '}', '&','/', "...",'-','+','*','|',"),");
    final CharArraySet stopSet = new CharArraySet(Version.LUCENE_40, stop_words,true);

    public String clean(String input) throws Exception{
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_40, new StringReader(input.trim()));
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream,stopSet);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while(tokenStream.incrementToken()){
            String term = charTermAttribute.toString();
            sb.append(term + " ");
        }
        return sb.toString();
    }

    public void dedupe(String inputFile, String outputFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        File file = new File(outputFile);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        Set<Integer> url_set = new HashSet<>();
        int id = 2000;
        String line;
        while ((line = br.readLine()) != null) {
            JsonObject jsonObject = new JsonParser().parse(line.trim()).getAsJsonObject();
            String detail_url = jsonObject.get("detail_url").getAsString();
            String title = jsonObject.get("title").getAsString();
            if(url_set.add(detail_url.hashCode())){
                String query = jsonObject.get("query").getAsString();
                String tokens = clean(query);
                jsonObject.addProperty("query", query + " " + tokens);
                jsonObject.addProperty("id",id);
                if(jsonObject.get("price").getAsDouble()==0.0){
                    jsonObject.addProperty("price", 30 + Math.random()*(480-30+1));
                }
                String keywords = clean(title);
                jsonObject.addProperty("keyWords",keywords);
                jsonObject.addProperty("title",title);
                id += 1;
                mapper.writeValue(bw,jsonObject);
            }
        }
        bw.close();
    }

}
