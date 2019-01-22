package retrieval;

import file_handling.MemoryManager;
import indexer.Indexer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tokenizer.ImprovedTokenizer;

/**
 *
 * @author fraza0
 */
public class RankedRetrieval {

    private final String finalIndex = "inverted_index/";
//    private final String indexInfo = "index_info.txt";

    private final String dictionaryPath = "dictionary/";
    private final String tokensDictionary = "dictionary.dict";
    private final Map<String, Double> termIDFDictionary = new HashMap<>();

    private final int threshold = 70;
    private final Map<String, Map<String, String>> filesRead = new HashMap<>();
    private final List<String> neededIndexedTerms = new ArrayList<>();
    private final float minimumIDF = 0f;
    private final int invIndexFilenamesize;

    public RankedRetrieval() throws IOException {
        clear_directoryFile();
        loadDictionary();
        
        File[] invIndFiles = new File(finalIndex).listFiles();
        this.invIndexFilenamesize = invIndFiles[0].getName().split("\\.")[0].length();
//        System.out.println("VER SOBRE PRIORITY QUEUE - https://nlp.stanford.edu/IR-book/html/htmledition/computing-vector-scores-1.html \n\n");
    }

    /**
     * Load dictionary file containing token;IDF
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void loadDictionary() throws FileNotFoundException, IOException {
        String line, term, idf;
        String[] parts;
        BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath + tokensDictionary));
        while ((line = reader.readLine()) != null) {
            parts = line.split(";");
            term = parts[0];
            idf = parts[1];
            termIDFDictionary.put(term, Double.parseDouble(idf));
        }
    }

    private double queryWeight(String query) {
        String[] queryTerms = query.split(" ");
        if (query.length() == 1) {
            return termWeight(queryTerms[0]);
        }
        double sum = 0;
        for (String term : queryTerms) {
            sum += Math.pow(termWeight(term), 2);
        }

        return Math.round(Math.sqrt(sum) * 1000.0) / 1000.0;
    }

    private double termWeight(String term) {
        return termIDFDictionary.get(term);
    }

    /**
     * Fetch term's postings list
     *
     * @param queryTerm
     * @return Map of parsed postings
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Map<Integer, Double> fetchTermPostingsListsMemory(String queryTerm) throws FileNotFoundException, IOException {
        Indexer i = new Indexer();

        String filename = queryTerm.substring(0, invIndexFilenamesize) + i.extension;

        return parsePostings(filesRead.get(filename).get(queryTerm));
    }

    /**
     * Parse Postings from terms the file where the searched term is if it
     * exists, and returns the map sorted by TF(-IDF) ranking
     *
     * @param postingsString
     * @return Sorted Map by TF(-IDF) Ranking
     */
    private Map<Integer, Double> parsePostings(String postingsString) {
        Map<Integer, Double> parsedPostings = new HashMap<>();
        Indexer i = new Indexer();
        String[] p;
        for (String posting : postingsString.split(i.termDelimiter)) {
            p = posting.split(i.postingsDelimiter);
            parsedPostings.put(Integer.parseInt(p[0]), Double.parseDouble(p[1]));
        }

        return parsedPostings;
    }

    /**
     * Calculate cosine score between documents where the terms of a query exist
     * and the query itself
     *
     * @param query
     * @return List of Scores ordered by Rank
     * @throws IOException
     */
    private List<Map.Entry<Integer, Double>> cosineScoreMemory(String query) throws IOException {
        Map<Integer, Double> scores = new HashMap<>();
        String[] queryTerms = query.split(" ");
        Map<Integer, Double> postings;
        double queryWeight = queryWeight(query), normalizedTermWeight;

        for (String term : queryTerms) {
            normalizedTermWeight = termWeight(term) / queryWeight;
            postings = fetchTermPostingsListsMemory(term);
            for (int docid : postings.keySet()) {
                scores.put(docid, scores.getOrDefault(docid, 0d)
                        + (postings.get(docid) * normalizedTermWeight));        //postings hold the TF value of the term
            }
        }

        return orderScoresByRank(scores);
    }

    /**
     * Order map by descending values
     *
     * @param map
     * @return
     */
    private List<Map.Entry<Integer, Double>> orderScoresByRank(Map<Integer, Double> map) {
        List<Map.Entry<Integer, Double>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries, (Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) -> e2.getValue().compareTo(e1.getValue()));

        return sortedEntries;
    }

    /**
     * Retrieve results of the ranked retrieval process
     *
     * @param query
     * @param topKresults
     * @return 
     * @throws IOException
     */
    public List<Map.Entry<Integer, Double>> retrieveResultsMemory(String query, int topKresults) throws IOException {
        List<Map.Entry<Integer, Double>> results = new ArrayList<>(cosineScoreMemory(filterQuery(query)));
        if (results.size() < topKresults) {
            topKresults = results.size();
        }

        System.out.println("Showing first "+topKresults+"\nDocid\tScore");
        for (int i = 0; i < topKresults; i++) {
            System.out.println(results.get(i).getKey() + "\t" + results.get(i).getValue());
        }

        return results;
    }
    
    public List<Integer> retrieveResults(String query) throws IOException {
        List<Map.Entry<Integer, Double>> results = new ArrayList<>(cosineScoreMemory(filterQuery(query)));
        List<Integer> docs = new ArrayList<>();
        for (Map.Entry<Integer, Double> result : results) {
            docs.add(result.getKey());
        }
        return docs;
    }
    
    public List<Integer> retrieveResults(String query, int topKResults) throws IOException {
        List<Map.Entry<Integer, Double>> results = new ArrayList<>(cosineScoreMemory(filterQuery(query)));
        List<Integer> docs = new ArrayList<>();
        if (results.size() < topKResults) {
            topKResults = results.size();
        }

        for (int i = 0; i < topKResults; i++) {
            docs.add(results.get(i).getKey());
        }

        return docs;
    }

    private String filterQuery(String query) throws IOException {
        Indexer i = new Indexer();
        ImprovedTokenizer it = new ImprovedTokenizer();
        StringBuilder sb = new StringBuilder();
        String[] querySplit = it.queryTokenization(query, false).split(" ");
        boolean isUniqueWord = (querySplit.length == 1);

        for (String term : querySplit) {
            if (termIDFDictionary.containsKey(term) && (isUniqueWord || termIDFDictionary.get(term) > minimumIDF)) {
                sb.append(term).append(" ");
                neededIndexedTerms.add(term.substring(0, invIndexFilenamesize) + i.extension);
                loadTermsToMemory(term.substring(0, invIndexFilenamesize) + i.extension);
            }
        }

        query = sb.toString();
//        System.out.println("Query: " + query);
        return query;
    }

    private boolean loadTermsToMemory(String termsfile) throws FileNotFoundException, IOException {
        MemoryManager mm = new MemoryManager();
        Indexer i = new Indexer();
        File termsFile = new File(finalIndex + termsfile);

        if (!termsFile.exists()) {
            return false;
        }

        //Check if is possible to load terms
        long totalMemory = mm.getTotalMemory(), freeMemory = mm.getFreeMemory(), estimated = (mm.getUsedMemory() + termsFile.length() - freeMemory) / totalMemory * 100;
        if (estimated > threshold) {
//            System.out.println("COULD NOT LOAD: " + termsfile);
            return false;
        }

        String[] parts;
        String line, term, postings;
        Map<String, String> terms = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(termsFile));
        while ((line = reader.readLine()) != null) {
            parts = line.split(i.termDelimiter);
            term = parts[0];
            postings = line.replace(term + i.termDelimiter, "");

            terms.put(term, postings);
        }
        filesRead.put(termsFile.getName(), terms);
//        System.out.println("LOADED: " + termsfile);
        return true;
    }

    /**
     * Workaround: .directory file delete
     */
    public final void clear_directoryFile() {
        File f = new File(finalIndex + ".directory");
        if (f.exists()) {
            f.delete();
        }
    }

//    public static void main(String[] args) throws IOException {
//        RankedRetrieval r = new RankedRetrieval();
//        ImprovedTokenizer it = new ImprovedTokenizer();
//        String query = "motorola modern holatestsff smart watch black derfjbaba";
////        String query = "women";
//        query = it.queryTokenization(query, false);
//        r.retrieveResultsMemory(query, 5);
//    }
}
