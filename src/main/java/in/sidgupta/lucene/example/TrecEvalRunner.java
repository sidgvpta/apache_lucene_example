package in.sidgupta.lucene.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class TrecEvalRunner {
    private static String TREC_EVAL_COMMAND = "trec_eval/trec_eval";

    public TrecEvalRunner(String cranQueryRelevance_path, String cranQueryRelevanceTrecEval_path, String results_root, String similarity_type, String analyzer_type) {

        try {
            CranQueryRelevanceFormatter(cranQueryRelevance_path, cranQueryRelevanceTrecEval_path);

            String queryResults_location = (String.format(results_root, similarity_type, analyzer_type) + "query_results");
            String trecAnalysis_output = (String.format(results_root, similarity_type, analyzer_type) + "trec_eval");

            //ProcessBuilder pb = new ProcessBuilder(TREC_EVAL_COMMAND, queryRelevanceLocation, queryResultsLocation);
            ProcessBuilder pb = new ProcessBuilder(TREC_EVAL_COMMAND, cranQueryRelevanceTrecEval_path, queryResults_location, "-l=3");
            Path workingDirectory  = FileSystems.getDefault().getPath(".").toAbsolutePath();
            pb.directory(new File(workingDirectory.toString()));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Path outputPath = Paths.get(trecAnalysis_output);
            String line;
            List<String> lines = new ArrayList<>();
            System.out.println(similarity_type + " " + analyzer_type);
            System.out.println("---------------------------");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                lines.add(line);
            }

            Files.write(outputPath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CranQueryRelevanceFormatter(String cranQueryRelevance_path, String cranQueryRelevanceTrecEval_path) throws IOException {

        List<String> lines = Files.lines(Paths.get(cranQueryRelevance_path)).collect(Collectors.toList());
        List<String> relevanceStrings = new ArrayList<String>();

        String[] decompose;
        String queryID;
        String iterNum = "0";
        String docID;
        String relevanceScore;
        int relevanceScore_int;

        for(String line : lines) {
            decompose = line.split("\\s+");
            queryID = decompose[0];
            docID = decompose[1];
            relevanceScore = decompose[2];
            relevanceScore_int = Integer.parseInt(relevanceScore);
            if(relevanceScore_int >= 1 && relevanceScore_int <= 5) {
                relevanceScore_int = 6 - relevanceScore_int;
            }
            //System.out.println(queryID + " " + iterNum + " " + docID + " " + relevanceScore_int);
            relevanceStrings.add(queryID + " " + iterNum + " " + docID + " " + Integer.toString(relevanceScore_int));
        }
        Files.write(Paths.get(cranQueryRelevanceTrecEval_path), relevanceStrings, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}
