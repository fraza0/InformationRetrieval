package retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluation of the Document Indexer
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class Evaluation {
    private final String delimiter = "\n";
    private final File queriesFile = new File("cranfield.queries.txt");
    private final File queryAnswersFile = new File("cranfield.query.relevance.txt");
    private Map<Integer, List<Integer>> retrievedDocs = new HashMap<>(), queryRelevance = new HashMap<>();
    private Map<Integer, Map<Integer, Integer>> retrievedDocsRank = new HashMap<>();
    private final int maxRetrievedDocs;
    private final float beta;
    
    private final long queryExecutionTotalTime;
    private final List<Long> queryExecutionTimes = new ArrayList<>();
   
    /**
     * Receive the limit number of results and value of beta;
     * Defines the maximum number of retrived docs as 10
     * @param maxRetrievedDocs
     * @param beta
     * @throws IOException 
     */
    public Evaluation(int maxRetrievedDocs, float beta) throws IOException {
        if (maxRetrievedDocs < 10) maxRetrievedDocs = 10;
        this.maxRetrievedDocs = maxRetrievedDocs;
        this.beta = beta;
        long startTime = System.currentTimeMillis();
        this.retrievedDocs = executeQueries(maxRetrievedDocs);
        this.queryExecutionTotalTime = System.currentTimeMillis()-startTime;
        this.queryRelevance = loadQueryRelevance();
        this.retrievedDocsRank = populateRetrievedFilesRank();
    }

    /**
     * Execute all queries from "cranfield.queries";
     * Saves the time spent in each executation
     * @param rank
     * @return Map with the results for each query
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private Map<Integer, List<Integer>> executeQueries(int rank) throws FileNotFoundException, IOException {
        Map<Integer, List<Integer>> hm = new HashMap<>();
        RankedRetrieval r = new RankedRetrieval();
        BufferedReader br = new BufferedReader(new FileReader(queriesFile));
        String line;
        int queryID = 1;
        long startTime;
        while ((line = br.readLine()) != null) {
            startTime = System.currentTimeMillis();
            hm.put(queryID, r.retrieveResults(line, rank));
            queryID++;
            queryExecutionTimes.add(System.currentTimeMillis()-startTime);
        }
        br.close();
        return hm;
    }
    
    /**
     * Get the relevance for all queries from "cranfield.query.relevance";
     * Assigns the respectives relevances to each query
     * 
     * @return Map with the relevances for each query
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private Map<Integer, List<Integer>> loadQueryRelevance() throws FileNotFoundException, IOException {
        Map<Integer, List<Integer>> hm = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(queryAnswersFile));
        String line;
        int queryID;
        List<Integer> docs = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            queryID = Integer.parseInt(line.split(" ")[0]);
            if (hm.containsKey(queryID)) {
                docs.add(Integer.parseInt(line.split(" ")[1]));
                hm.put(queryID, docs);
            } else {
                docs = new ArrayList<>();
                docs.add(Integer.parseInt(line.split(" ")[1]));
                hm.put(queryID, docs);
            }
        }
        return hm;
    }
    
    /**
     * Initialize a BufferedReader with query answers;
     * To each query assigns the documents who answers to its
     * 
     * @return Map with the documents that respond to each query
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private Map<Integer, Map<Integer, Integer>> populateRetrievedFilesRank() throws FileNotFoundException, IOException {
        Map<Integer, Map<Integer, Integer>> hm = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(queryAnswersFile));
        String line;
        int queryID;
        Map<Integer, Integer> docs = new HashMap<>();
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\\s+", " ");
            queryID = Integer.parseInt(line.split(" ")[0]);
            if (hm.containsKey(queryID)) {
                docs.put(Integer.parseInt(line.split(" ")[1]), 5 - Integer.parseInt(line.split(" ")[2]));
                hm.put(queryID, docs);
            } else {
                docs = new HashMap<>();
                docs.put(Integer.parseInt(line.split(" ")[1]), 5 - Integer.parseInt(line.split(" ")[2]));
                hm.put(queryID, docs);
            }
        }
        
        return hm;
    }
    
    /**
     * Assigns a rank to each document retrieved
     * 
     * @param queryID
     * @return Map with ranking documents
     */
    private Map<Integer, Integer> retrievedDocsRanking(int queryID) {                                             //PROF FALOU EM TER DE INVERTER OS VALORES DA RELEVANCIA. VER QUANDO É IMPORTANTE!
        Map<Integer, Integer> docRank = new LinkedHashMap<>();
        int totalDocs = retrievedDocs.get(queryID).size();

        for (int docID : retrievedDocs.get(queryID)) {
            if (retrievedDocsRank.get(queryID).containsKey(docID)) {
                docRank.put(docID, retrievedDocsRank.get(queryID).get(docID));
            } else {
                docRank.put(docID, 0);
            }
        }

        return docRank;
    }
    
    public double log2(double a) {
        return Math.log(a) / Math.log(2);
    }

    /**
     * Order map by descending values
     *
     * @param map
     * @return List of documents ordered by rank
     */
    private List<Map.Entry<Integer, Integer>> orderDocsByRank(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries, (Map.Entry<Integer, Integer> e1, Map.Entry<Integer, Integer> e2) -> e2.getValue().compareTo(e1.getValue()));

        return sortedEntries;
    }

    /**
     * Get the precision for one specific query
     * 
     * @param queryID
     * @return value of precision for the given query
     */
    private float queryPrecision(int queryID) {
        int totalDocs = 0, hits = 0;
        totalDocs = retrievedDocs.get(queryID).size();
        for (int docID : retrievedDocs.get(queryID)) {
            if (queryRelevance.get(queryID).contains(docID)) {
                hits++;
            }
        }
        return (float) hits / totalDocs;
    }
    
    /**
     * Get the value of precision for all queries
     * 
     * @return value of precision of all queries
     */
    private float precision() {
        float precision = 0;
        for (int queryID : retrievedDocs.keySet()) {
            precision += queryPrecision(queryID);
        }
        return precision / retrievedDocs.size();
    }

    /**
     * Get the value of recall for one given query
     * 
     * @param queryID
     * @return value of recall for one given query
     */
    private float queryRecall(int queryID) {
        int totalDocs = 0, hits = 0;
        totalDocs = queryRelevance.get(queryID).size();
        for (int docID : queryRelevance.get(queryID)) {
            if (retrievedDocs.get(queryID).contains(docID)) {
                hits++;
            }
        }
        return (float) hits / totalDocs;
    }
    
    /**
     * Get recall value from all queries
     * 
     * @return value of recall to all queries
     */
    private float recall() {
        float recall = 0;
        for (int queryID : retrievedDocs.keySet()) {
            recall += queryRecall(queryID);
        }
        return recall / retrievedDocs.size();
    }
    
    /**
     * Gets the harmonic mean of recall and precision to a given query
     * 
     * @param queryID
     * @return the harmonic mean of recall and precision to a given query
     */
    private float queryFmeasure(int queryID) {
        float p = queryPrecision(queryID), r = queryRecall(queryID);
        if (r + p == 0) {
            return 0;
        }
        return ((beta + 1) * r * p) / (r + ((float)Math.pow(beta, 2) * p));
    }
    
    /**
     * Gets the harmonic mean of recall and precision to all queries of the set
     * 
     * @return harmonic mean of recall and precision to all queries
     */
    private float fmeasure() {
        float fscore = 0;
        for (int queryID : retrievedDocs.keySet()) {
            fscore += queryFmeasure(queryID);
        }
        return fscore / retrievedDocs.size();
    }
    
    /**
     * Gets the mean average precision for a specific given query
     * 
     * @param queryID
     * @return the mean average precision for a specific given query
     */
    private float queryMeanAveragePrecision(int queryID) {
        int totalDocs = 0, totalHits = 1, relevantHits = 0;
        float queryPrecision = 0, mean = 0;
        totalDocs = retrievedDocs.get(queryID).size();
        for (int docID : retrievedDocs.get(queryID)) {
            if (queryRelevance.get(queryID).contains(docID)) {
                relevantHits++;
                if (totalHits == 0) totalHits = 1;
                queryPrecision += (float) relevantHits / totalHits;
            }
            totalHits++;
        }
        if (relevantHits == 0) relevantHits = 1;
        return (float) queryPrecision/relevantHits;
    }
    
    /**
     * Gets the mean average precision for the relevant documents 
     * in an answer to a query
     * 
     * @return mean average precision for of all queries
     */
    private float meanAveragePrecision() {
        float avgPrecision = 0;
        for (int queryID : retrievedDocs.keySet()) {
            avgPrecision += queryMeanAveragePrecision(queryID);
        }
        return avgPrecision / retrievedDocs.size();
    }
    
    /**
     * Gets a a mean precicion of a query to a specific rank;
     * Retrieve the query relevance to all queries in the rank value;
     * Calculates the respective precision
     * 
     * @param queryID
     * @param rank
     * @return mean precision at given rank of a specific query
     * @throws IOException 
     */
    private float meanPrecisionAtRank(int queryID, int rank) throws IOException {
        Map<Integer, List<Integer>> retrievedFilesRank = retrievedDocs;
        List<Integer> sublist;
        float precision = 0;
        int hits = 0, totalDocs = 0;
        
        for (int docID : retrievedFilesRank.get(queryID).subList(0, rank)) {
            totalDocs = retrievedFilesRank.get(queryID).subList(0, rank).size();
            if (queryRelevance.get(queryID).contains(docID)) {
                hits++;
            }
        }
        
        precision = (float) hits / totalDocs;
        return precision;
    }
    
    /**
     * Gets the mean precision at a given rank for all dataset
     * 
     * @param rank
     * @return mean precision at given rank of a given dataset
     * @throws IOException 
     */
    private float meanPrecisionAtRank(int rank) throws IOException {
        float precision = 0;
        for (int queryID : queryRelevance.keySet()) {
            precision += meanPrecisionAtRank(queryID, rank);
        }
        return precision / retrievedDocs.size();
    }
    
    /**
     * Obtains the discounted comulative gain (DCG) of a specific query;
     * For each document retrieved for the query, gets its rank
     * Calculate the DCG with the rank obtained and add the result to an array
     * 
     * @param queryID
     * @return array with DCG results to a given query
     */
    private float[] queryDCG(int queryID) {
        Map<Integer, Integer> docRank = retrievedDocsRanking(queryID);
        
        int counter = 1;
        float[] dcgArray = new float[docRank.size()];
        float dcg = 0;
        for (int docID : docRank.keySet()) {
            if (counter == 1) {
                dcg += docRank.get(docID);
                dcgArray[counter - 1] = dcg;
                counter++;
                continue;
            }
            dcg += docRank.get(docID) / log2(counter);
            dcgArray[counter - 1] = dcg;
            counter++;
        }
        
//        System.out.println(Arrays.toString(dcgArray));
        return dcgArray;
    }
    
    /**
     * Gets the normalized discounted comulative gain (NDCG)
     * for a specific query;
     * Get the rank for each document retrieved from a query;
     * Add the result to an array;
     * Get the value of the last position of that array
     * 
     * @param queryID
     * @return the last value of NDCG array created
     */
    private float queryNDCG(int queryID) {
        Map<Integer, Integer> docRank = retrievedDocsRank.get(queryID);
        List<Map.Entry<Integer, Integer>> idealOrder = orderDocsByRank(docRank);

        int counter = 1;
        float[] idealDCGArray = new float[idealOrder.size()];
        float idealDCG = 0;
        for (Map.Entry<Integer, Integer> docIDRank : idealOrder) {
            if (counter == 1) {
                idealDCG = docIDRank.getValue();
                idealDCGArray[counter - 1] = idealDCG;
                counter++;
                continue;
            }
            idealDCG += docIDRank.getValue() / log2(counter);
            idealDCGArray[counter - 1] = idealDCG;
            counter++;
        }
        float[] actualDCGArray = queryDCG(queryID), ndcgArray = new float[actualDCGArray.length];
//        System.out.println(actualDCGArray.length+" "+idealDCGArray.length);
        float idealDCGValue = 0;
        for (int i = 0; i < actualDCGArray.length; i++) {
            if (i >= idealDCGArray.length){
                idealDCGValue = idealDCGArray[idealDCGArray.length-1];
            } else {
                idealDCGValue = idealDCGArray[i];
            }
            ndcgArray[i] = actualDCGArray[i] / idealDCGValue;
        }

//        System.out.println(queryID+" "+ndcgArray[ndcgArray.length-1]);
        return ndcgArray[ndcgArray.length-1];
    }
    
    /**
     * Gets the normalized discounted comulative gain (NDCG) from all queries
     * 
     * @return NDCG value from all queries of the given dataset
     */
    private float NDCG() {
        float queryndcg, ndcg = 0;
        for (int queryID : retrievedDocs.keySet()) {
            queryndcg = queryNDCG(queryID);
//            System.out.println(queryID+" "+Arrays.toString(queryndcg));
            ndcg += queryndcg;
        }
        
        return ndcg/retrievedDocs.size();
    }
    
    /**
     * Gets the number of queries that are processed per second
     * 
     * @return number of queries that are processed per second
     * @throws IOException 
     */
    private int queryThroughput() throws IOException {
        return (int) Math.floor((float) retrievedDocs.size() / queryExecutionTotalTime * 1000);
    }
    
    /**
     * Allows to determinate the average time of response to a query;
     * Sort the execution times of all queries and calculate the median
     * 
     * @return the median query latency for the given dataset 
     * @throws IOException 
     */
    private float meadianQueryLatency() throws IOException {
        float medianLatency;

        Collections.sort(queryExecutionTimes);
        
        if (queryExecutionTimes.size() % 2 == 0) {
            medianLatency = (queryExecutionTimes.get(queryExecutionTimes.size() / 2) + queryExecutionTimes.get(queryExecutionTimes.size() / 2 - 1));
        } else {
            medianLatency = (queryExecutionTimes.get(queryExecutionTimes.size() / 2));
        }
        
        return medianLatency;
    }
    
    /**
     * Create a String with all values of the evaluation performed
     * 
     * @return string with all metrics of the evaluation performed
     * @throws IOException 
     */
    public String getEvaluation() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Precision: ").append(precision()).append(delimiter);
        sb.append("Recall: ").append(recall()).append(delimiter);
        sb.append("FScore: ").append(fmeasure()).append(delimiter);
        sb.append("Average Precision: ").append(meanAveragePrecision()).append(delimiter);
        sb.append("Mean Precision @ 10 ").append(meanPrecisionAtRank(10)).append(delimiter);
        sb.append("QUERY NDCG: ").append(NDCG()).append(delimiter);
        sb.append("Query throughput: ").append(queryThroughput()).append(" queries per sec.").append(delimiter);
        sb.append("Median query latency: ").append(meadianQueryLatency()).append(" (ms)").append(delimiter);
        
        return sb.toString();
    }

//    public static void main(String[] args) throws IOException {
//        Evaluation ev = new Evaluation(100,1);
//        
//        System.out.println("Precision " + ev.precision());
//        System.out.println("Recall " + ev.recall());
//        System.out.println("FScore " + ev.fmeasure());
//        System.out.println("Average Precision " + ev.meanAveragePrecision());
//        System.out.println("Mean Precision @ 10 " + ev.meanPrecisionAtRank(10));
//        System.out.println("NDCG: " + ev.NDCG());
//        System.out.println("Query throughput: " + ev.queryThroughput() + " queries per sec.");
//        System.out.println("Median query latency: " + ev.meadianQueryLatency() + " (ms)");
//    }
}
