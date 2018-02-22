package in.sidgupta.lucene.example;

import org.apache.lucene.queryparser.classic.ParseException;
import java.io.IOException;

public class main {
    public static String cranQueryRelevance_path = "src/main/resources/cranqrel";
    public static String cranQueryRelevanceTrecEval_path = "src/main/resources/cranqrel_trecEval";
    public static String index_root = "src/main/indexes/%s/%s";
    public static String results_root = "src/main/results/%s/%s/";
    public static int NUM_SIMILARITIES = 4;
    public static int NUM_ANALYZERS = 4;
    private static final String[] SIMILARITIES = {"vector_space_model", "bm_25", "boolean", "lm_dirichlet"};
    private static final String[] ANALYZERS = {"standardAnalyzer", "whitespaceAnalyzer", "simpleAnalyzer", "stopAnalyzer"};

    public static void main(String [] args) {

        String analyzer_type;
        String similarity_type;

        for(int i = 0; i < NUM_SIMILARITIES; i++) {
            for (int j = 0; j < NUM_ANALYZERS; j++) {

                similarity_type = SIMILARITIES[i];
                analyzer_type = ANALYZERS[j];

                indexCreation(similarity_type, analyzer_type);
                newSearch(similarity_type, analyzer_type);
                newTrecEval(similarity_type, analyzer_type);
            }
        }
    }

    public static void indexCreation(String similarity_type, String analyzer_type) {
        try {
            CreateIndex index = new CreateIndex(index_root, similarity_type, analyzer_type);
        }
        catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void newSearch(String similarity_type, String analyzer_type) {
        try {
            Search queryResponse = new Search(index_root, results_root, similarity_type, analyzer_type);
        }
        catch(IOException | ParseException xcp) {
            throw new RuntimeException(xcp);
        }
    }

    public static void newTrecEval(String similarity_type, String analyzer_type) {

        TrecEvalRunner eval = new TrecEvalRunner(cranQueryRelevance_path, cranQueryRelevanceTrecEval_path, results_root, similarity_type, analyzer_type);
    }
}
