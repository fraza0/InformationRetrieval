package indexer;

import file_handling.FileHandler;
import file_handling.MemoryManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

/**
 * Create an inverted index of terms in documents
 *
 * @author Rui Frazão
 * @author Fábio Ferreira
 */
public class Indexer {

    private final String indexPath = "./index/";
    private final int threshold = 70;
    public final String termDelimiter = ";", postingsDelimiter = ":";
    public final String extension = ".ind";

    /**
     * Check if index folder exists and is empty; If not creates it or deletes
     * its content.
     *
     * @param clearDirectory
     */
    public Indexer(boolean clearDirectory) {
        if (!clearDirectory) {
            return;
        }
        File baseDir = new File(indexPath);

        if (!baseDir.exists()) {
            baseDir.mkdir();
        } else {
            File[] files = baseDir.listFiles();
            if (files.length > 0) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Do not clean index folder before starting indexing
     */
    public Indexer() {
    }

    /**
     * SPIMIInvert algorithm implementation
     *
     * @param inputFileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void SPIMIInvert(String inputFileName) throws FileNotFoundException, IOException {
        FileHandler fh = new FileHandler();
        MemoryManager mm = new MemoryManager();
        Map<String, Map> dictionary = new HashMap<>();
        Map<String, Map<Integer, Double>> tmpDict = new HashMap<>();
        Map<Integer, Double> postingList = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        String line = br.readLine();
        long nLines = 0;
        double docNorm;

        int indexPartCounter = 0;
        String indexPartName;
        int docid;
        String[] tokens;

        while (line != null) {
            nLines += 1;
            postingList = new HashMap<>();
            docid = Integer.parseInt(line.substring(0, line.indexOf("\t")));
            tokens = line.split("\\s+");
            tmpDict.clear();

            for (int i = 1; i < tokens.length; i++) {                           //starts in i=1 to ignore docid
                if (!dictionary.containsKey(tokens[i])) {
                    postingList = new HashMap<>();
                    postingList.put(docid, 1.0);
                } else {
                    postingList = dictionary.get(tokens[i]);
                    postingList.put(docid, postingList.getOrDefault(docid, 0.0) + 1);
                }

                tmpDict.put(tokens[i], postingList);
                dictionary.put(tokens[i], postingList);
                if (i == tokens.length - 1) {       //Calculate normalized TF
                    
                    docNorm = calculateDocumentNorm(tmpDict, docid);
                    for (String t: tmpDict.keySet()){
                        dictionary.get(t).replace(docid, calculateTF(tmpDict.get(t).get(docid)) / docNorm);
                    }
                    
                }
//                System.out.println(dictionary);
            }
            
            tmpDict.clear();
            line = br.readLine();

            if (mm.getMemoryUsage() >= threshold) {
                indexPartName = "indexPart" + indexPartCounter + extension;
                System.out.println("DUMP " + docid + " " + indexPath + "" + indexPartName);
                writeDictionaryToFile(dictionary, indexPath + "indexPart" + indexPartCounter + extension);            //dump to file
                postingList.clear();
                dictionary.clear();
                tmpDict.clear();
                System.gc();
                indexPartCounter++;
            }

        }

        writeDictionaryToFile(dictionary, indexPath + "indexPart" + indexPartCounter + extension);            //dump to file
        postingList.clear();
        dictionary.clear();
        tmpDict.clear();
        System.gc();
        br.close();

        File f = new File("index_info.txt");
        f.delete();
        fh.write(f.getName(), String.valueOf(nLines));
        clear_directoryFile();
    }

    private double calculateDocumentNorm(Map<String, Map<Integer, Double>> docMap, int id) {
        double squareSum = 0;
        for (String s : docMap.keySet()) {
            squareSum += Math.pow(calculateTF(docMap.get(s).get(id)), 2);
        }
        return Math.sqrt(squareSum);
    }

    /**
     * Calculates Term Frequency weight of a term in a document
     *
     * @param term_freq
     * @return Term Frequency Weight
     */
    private double calculateTF(double term_freq) {
        return (1 + Math.log10(term_freq));
    }

    /**
     * Write dictionary
     *
     * @param dict
     * @param outputFile
     */
    public void writeDictionaryToFile(Map<String, Map> dict, String outputFile) {
        FileHandler fh = new FileHandler();
        StringBuilder sb = new StringBuilder();

        dict.keySet().stream().sorted().forEach((term) -> {
            sb.setLength(0);
            sb.append(term).append(termDelimiter);
            dict.get(term).keySet().stream().sorted().forEach((docid) -> {
                sb.append(docid).append(postingsDelimiter).append(String.format(Locale.US, "%.2f", dict.get(term).get(docid))).append(termDelimiter);
            });
            fh.write(outputFile, sb.deleteCharAt(sb.length() - 1).toString());
        });
    }

    /**
     * Workaround: .directory file delete
     */
    public void clear_directoryFile() {
        File f = new File(indexPath + ".directory");
        if (f.exists()) {
            f.delete();
        }
    }

}
