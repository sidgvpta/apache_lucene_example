package in.sidgupta.lucene.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Search {
    public static String cranQueries_path = "src/main/resources/cran.qry";
    private static final Logger LOG = LoggerFactory.getLogger(CreateIndex.class);
    public static final int MAX_RESULTS = 1400;
    private int counter = 0;

    public Search(String index_root, String results_root, String similarity_type, String analyzer_type) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(String.format(index_root, similarity_type, analyzer_type))));
        final IndexSearcher searcher = new IndexSearcher(reader);

        Similarity similarity = null;
        Analyzer analyzer = null;

        switch (similarity_type) {
            case "vector_space_model" : similarity = new ClassicSimilarity();
                break;
            case "bm_25" : similarity = new BM25Similarity();
                break;
            case "boolean" : similarity = new BooleanSimilarity();
                break;
            case "lm_dirichlet" : similarity = new LMDirichletSimilarity();
                break;
        }

        switch (analyzer_type) {
            case "standardAnalyzer" : analyzer = new StandardAnalyzer();
                break;
            case "keywordAnalyzer" : analyzer = new KeywordAnalyzer();
                break;
            case "whitespaceAnalyzer" : analyzer = new WhitespaceAnalyzer();
                break;
            case "simpleAnalyzer" : analyzer = new SimpleAnalyzer();
                break;
            case "stopAnalyzer" : analyzer = new StopAnalyzer();
                break;
        }

        List<String> lines = Files.lines(Paths.get(cranQueries_path)).collect(Collectors.toList());
        ArrayList<String> queryList = parseText(lines);
        searcher.setSimilarity(similarity);
        //QueryParser parser = new QueryParser("title", analyzer);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"title", "author", "published", "body"}, analyzer);
        parser.setAllowLeadingWildcard(true);
        int query_id = 0;
        String query_body = new String();

        List<String> query_results = new ArrayList<String>();
        String iter_num = "0";
        String run_id = "0";

        for(String query : queryList) {
            //query_id = query.substring(0, 3);
            query_id++;
            query_body = query.substring(3).trim();
            TopDocs result = searcher.search(parser.parse(query_body), MAX_RESULTS);
            counter++;
            //System.out.print(counter);
            ScoreDoc score_docs[] = result.scoreDocs;
            for(int i = 0; i < score_docs.length; i++) {
                query_results.add(Integer.toString(query_id) + " " + iter_num + " " + (score_docs[i].doc + 1) + " " + i + " " + score_docs[i].score + " " + run_id);
            }
        }
        reader.close();
        String results_location = (String.format(results_root, similarity_type, analyzer_type) + "query_results");
        Files.write(Paths.get(results_location), query_results, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        //Files.write(Paths.get(results_location), query_results);
    }

    private ArrayList<String> parseText(List<String> lines) {
        String query_body = new String();
        String query_id = new String();
        ArrayList<String> queryList = new ArrayList<String>();

        for(String line : lines) {
            if(line.matches("^\\.\\w(\\s+\\d+)?")) {
                if(line.contains("I")) {
                    if(query_body.length() > 1) {
                        queryList.add(query_id + query_body);
                    }
                    query_id = line.substring(3);
                    query_body = "";
                }
                else if(line.contains("W")) {
                    query_body = "";
                }
                else {
                    System.out.print("ERROR: INVALID TAG");
                }
            }
            else {
                query_body += " ";
                query_body += line;
            }
        }
        queryList.add(query_id + query_body);
        return queryList;
    }
}