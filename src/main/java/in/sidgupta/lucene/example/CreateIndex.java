package in.sidgupta.lucene.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CreateIndex {
    public static String cranTexts_path = "src/main/resources/cran.all.1400";
    private static final Logger LOG = LoggerFactory.getLogger(CreateIndex.class);
    private int counter = 0;

    public CreateIndex(String index_root, String similarity_type, String analyzer_type) throws IOException {
        this.create(index_root, similarity_type, analyzer_type);
    }

    public void create(String index_root, String similarity_type, String analyzer_type) throws IOException {

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

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setSimilarity(similarity);

        final IndexWriter writer = new IndexWriter(FSDirectory.open(Paths.get(String.format(index_root, similarity_type, analyzer_type))), config);
        List<String> lines = Files.lines(Paths.get(cranTexts_path)).collect(Collectors.toList());
        //lines.forEach(line -> addToIndex(line, writer));
        parseText(lines, writer);
        //LOG.info(String.format("Wrote %d titles to index", lines.size()));
        //System.out.println(counter);
        writer.close();
    }

    private void parseText(List<String> lines, IndexWriter writer) {
        String textField_flag = new String();
        String addToIndex_input = new String();
        Document doc = new Document();

        for(String line : lines) {
            if(line.matches("^\\.\\w(\\s+\\d+)?")) {
                if(line.contains("I")) {
                    doc.add(new TextField(textField_flag, addToIndex_input, Field.Store.YES));
                    List<IndexableField> fields = doc.getFields();
                    if(fields.size() > 1) {
                        addToIndex(doc, writer);
                    }
                    addToIndex_input = line.substring(3);
                    doc = new Document();
                    textField_flag = "cran_id";
                }
                else if(line.contains("T")) {
                    doc.add(new TextField(textField_flag, addToIndex_input, Field.Store.YES));
                    addToIndex_input = "";
                    textField_flag = "title";
                }
                else if(line.contains("A")) {
                    doc.add(new StringField(textField_flag, addToIndex_input, Field.Store.YES));
                    addToIndex_input = "";
                    textField_flag = "author";
                }
                else if(line.contains("B")) {
                    doc.add(new StringField(textField_flag, addToIndex_input, Field.Store.YES));
                    addToIndex_input = "";
                    textField_flag = "published";
                }
                else if(line.contains("W")) {
                    doc.add(new TextField(textField_flag, addToIndex_input, Field.Store.YES));
                    addToIndex_input = "";
                    textField_flag = "body";
                }
                else {
                    System.out.print("ERROR: INVALID TAG");
                }
            }
            else {
                addToIndex_input += " ";
                addToIndex_input += line;
            }
        }
        doc.add(new TextField(textField_flag, addToIndex_input, Field.Store.YES));
        addToIndex(doc, writer);
    }

    private void addToIndex(Document doc, IndexWriter writer) {
        try {
            writer.addDocument(doc);
            counter++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}