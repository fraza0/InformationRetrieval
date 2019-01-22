package indexer;

import file_handling.FileHandler;
import file_handling.MemoryManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Create an inverted index of terms in documents
 *
 * @author Rui Frazão
 * @author Fábio Ferreira
 */
public class IndexerPosition {

    private final String indexPath = "./index/";
    private final int threshold = 70;
    public final String termDelimiter = ";", postingsDelimiter = ":", termPositionDelimiter = ",";

    /**
     * Check if index folder exists and is empty; If not creates it or deletes
     * its content.
     * 
     * @param clearDirectory
     */
    public IndexerPosition(boolean clearDirectory) {
        if (!clearDirectory) return;
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
    public IndexerPosition() {
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
        Map<Integer, List<Double>> postingList;
        
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        String line = br.readLine();
        long nLines = 0;

        int indexPartCounter = 0;
        String indexPartName;
        int docid;
        String[] tokens;
        List<Integer> termPositions = new ArrayList<>();

        while (line != null) {
            nLines += 1;
            postingList = new HashMap<>();
            docid = Integer.parseInt(line.substring(0, line.indexOf("\t")));
            tokens = line.split("\\s+");

            for (int i = 1; i < tokens.length; i++) {
                
                List<Double> freqPositions = new ArrayList<>();
                
                // term position
                //System.out.println(tokens[i] + ": ");
                for (int j = -1; (j = line.indexOf(tokens[i], j + 1)) != -1; j++) {
                    double position = j - 2;
                    freqPositions.add(position);
                    //System.out.print(position + " ");
                }

                
                if (!dictionary.containsKey(tokens[i])) {
                    postingList = new HashMap<>();
                    freqPositions.add(0, 1.0);
                    postingList.put(docid, freqPositions);
                    
                } else {
                    double freq_tmp;
                    postingList = dictionary.get(tokens[i]);
                    if(postingList.get(docid) != null)
                        freq_tmp = postingList.get(docid).get(0) + 1;
                    else
                        freq_tmp = 1; // 0.0 + 1
                    
                    freqPositions.add(0, freq_tmp);
                    postingList.put(docid, freqPositions);
                        
                }

                dictionary.put(tokens[i], postingList);

          
            }
            line = br.readLine();

            if (mm.getMemoryUsage() >= threshold) {
                indexPartName = "indexPart" + indexPartCounter + ".ind";
                System.out.println("DUMP " + docid + " " + indexPath + "" + indexPartName);
                writeDictionaryToFile(dictionary, indexPath + "indexPart" + indexPartCounter + ".ind");            //dump to file
                postingList.clear();
                dictionary.clear();
                System.gc();
                indexPartCounter++;
            }

        }

        writeDictionaryToFile(dictionary, indexPath + "indexPart" + indexPartCounter + ".ind");            //dump to file
        br.close();

        fh.write("index_info.txt", String.valueOf(nLines));
        clear_directoryFile();
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

    /**
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
                sb.append(docid).append(postingsDelimiter);
                Object obj = dict.get(term).get(docid);
                List list = (ArrayList) obj;
                sb.append(list.get(0)).append(postingsDelimiter);
                for(int i = 1; i < list.size(); i++) {
                    int pos =(int)(double) list.get(i);
                    sb.append(pos);
                    if(i == list.size() -1)
                        sb.append(termDelimiter);
                    else
                        sb.append(termPositionDelimiter);
                }
            });
            fh.write(outputFile, sb.deleteCharAt(sb.length() - 1).toString());
        });
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

}
