package io.bittiger.crawler;

import io.bittiger.ad.Ad;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Zoe on 5/3/17.
 */
public class GenerateSubquery {


    public List<Ad> generateNewAds(String rawFile, String proxyFilePath, String logFilePath) throws Exception {

        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath, logFilePath);
        List<Ad> res = new ArrayList<>();
        //init
        BufferedReader is = new BufferedReader(new FileReader(rawFile)), br=is;
        String queries = IOUtils.toString(is);
        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            String query = fields[0].trim();
            double bidPrice = Double.parseDouble(fields[1].trim());
            int campaignId = Integer.parseInt(fields[2].trim());
            int queryGroupId = Integer.parseInt(fields[3].trim());

            List<Ad> ads = crawler.GetAdBasicInfoByQuery(query,Math.random()*2*bidPrice,campaignId+1,queryGroupId);
            String category = ads.get(0).category;

            List<String> subQueries = NGram(query);
            for(String sub: subQueries){
                if(!queries.contains(sub)){
                    List<Ad> subAds = crawler.GetAdBasicInfoByQuery(sub,Math.random()*2*bidPrice,campaignId+1,queryGroupId);
                    String subCg = subAds.get(0).category;
                    if(subCg.equals(category)) ads.addAll(subAds);
                }
            }
            res.addAll(ads);
        }
        return res;
    }

    public static List<String> NGram(String query){
        List<String> ngrams = new ArrayList<>();
        String[] words = query.split(" ");
        int n = words.length;
        if(n<3) return ngrams;
        for(int length=2;length<=n;length++){
            for(int start=0;start<n-length+1;start++){
                StringBuilder sb = new StringBuilder();
                for(int i=start;i<start+length;i++){
                    sb.append( (i>start ? " " : "") + words[i]);
                }
                ngrams.add(sb.toString());
            }
        }
        return ngrams;
    }

}
