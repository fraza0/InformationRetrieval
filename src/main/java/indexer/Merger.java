package indexer;

import file_handling.FileHandler;
import file_handling.MemoryManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Final Step of Index creation - Merging the documents created by the Indexer
 * Class
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class Merger {

    private final Indexer i = new Indexer();
    private final String indexPath = "index/";
    private final String finalIndexPath = "inverted_index/";
    private final int nFiles;
    private final File baseDir = new File(indexPath);
    private final File[] files = baseDir.listFiles((File file) -> file.isFile());
    private List<BufferedReader> bufferList = new ArrayList<>();

    private final Map<String, String> finalIndex = new HashMap<>();
    private final double threshold = 70;
    
    private final int invIndexFilenamesize;

    private final String dictionaryPath = "dictionary/";
//    Map<String, Double> tokenIDFDictionary = new HashMap<>();
    private final String indexInfo = "index_info.txt";
    private long indexSize;
//    private final int postingsLimit = 500;

    /**
     * Check if index folder exists and is empty; If not creates it or deletes
     * its content; Initialize a BufferedReader per file;
     *
     * @param filenameSize
     * @throws FileNotFoundException
     */
    public Merger(int filenameSize) throws FileNotFoundException, IOException {
        FileHandler fh = new FileHandler();
        this.nFiles = files.length;
        this.bufferList = initializeBuffers();

        File finalIndexDir = new File(finalIndexPath);
        File dictionaryDir = new File(dictionaryPath);

        if (!finalIndexDir.exists() || !dictionaryDir.exists()) {
            finalIndexDir.mkdir();
            dictionaryDir.mkdir();
        } else {
            File[] finalIndexFiles = finalIndexDir.listFiles();
            if (finalIndexFiles.length > 0) {
                for (File f : finalIndexFiles) {
                    f.delete();
                }
            }
            File[] finalDictFiles = dictionaryDir.listFiles();
            if (finalDictFiles.length > 0) {
                for (File f : finalDictFiles) {
                    f.delete();
                }
            }
        }

        if (!new File(indexInfo).exists()) {
            System.err.println("File indexInfo does not exist!");
        } else {
            this.indexSize = Long.parseLong(fh.readIndexInfo(indexInfo));
        }
        
        this.invIndexFilenamesize = filenameSize;
    }

    public void setBufferList(List<BufferedReader> bufferList) {
        this.bufferList = bufferList;
    }

    /**
     * Initialize a BufferedReader per file and add to List
     *
     * @return List of BufferedReader objects
     * @throws FileNotFoundException
     */
    public final List<BufferedReader> initializeBuffers() throws FileNotFoundException {
        List<BufferedReader> brList = new ArrayList<>();
        Arrays.sort(files);
        for (int buffcounter = 0; buffcounter < nFiles; buffcounter++) {
            brList.add(new BufferedReader(new FileReader(files[buffcounter])));
        }
        return brList;
    }

    /**
     * Check if term is the smallest term in array of last read terms from index
     * files
     *
     * @param term
     * @param lastTerms
     * @return true if term is the smallest; false otherwise
     */
    public boolean smallestInArray(String term, String[] lastTerms) {
        for (String lastTerm : lastTerms) {
            if (lastTerm != null && term.compareTo(lastTerm) >= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write dictionary to disk
     *
     * @param lastTerms
     * @param forceWriteLast
     */
    public void writeDictionaryToFinalIndex(String[] lastTerms, boolean forceWriteLast) {
        FileHandler fh = new FileHandler();

        Stream<String> sortedDict = finalIndex.keySet().stream().sorted();

        if (forceWriteLast) {
            sortedDict.forEach(s -> {
                fh.write(finalIndexPath + s.substring(0, invIndexFilenamesize) + ".ind", s + i.termDelimiter + finalIndex.get(s));
                fh.write(dictionaryPath + "dictionary.dict", s + i.termDelimiter + String.format(java.util.Locale.US, "%.2f", calculateTermIDF(finalIndex.get(s))));
                finalIndex.remove(s);
            });
        } else {
            sortedDict.filter(s -> smallestInArray(s, lastTerms)).forEach(s -> {
                fh.write(finalIndexPath + s.substring(0, invIndexFilenamesize) + ".ind", s + i.termDelimiter + finalIndex.get(s));
                fh.write(dictionaryPath + "dictionary.dict", s + i.termDelimiter + String.format(java.util.Locale.US, "%.2f", calculateTermIDF(finalIndex.get(s))));
                finalIndex.remove(s);
            });

        }
    }

    /**
     * Read terms from every file created by the indexer and merge results.
     *
     * @throws IOException
     */
    public void createFinalIndex() throws IOException {
        System.out.println("Merging");
        clear_directoryFile();
        MemoryManager mm = new MemoryManager();
        String line, term = null, postings;
        int fileCounter;
        String[] currentTerms = new String[nFiles], postingsSplit;

        while (!bufferList.isEmpty()) {
            fileCounter = 0;
            for (BufferedReader br : bufferList) {
                line = br.readLine();
                if (line == null) {
                    currentTerms = chopTermOfArray(currentTerms, term);
                    br.close();
                    bufferList.remove(br);
                    break;
                }
                term = line.split(i.termDelimiter)[0];
                postings = line.substring(line.indexOf(term) + term.length() + 1);
//                postingsSplit = postings.split(":");
//                if (postingsSplit.length > postingsLimit){
//                    postings = postings.substring(0, postings.indexOf(postingsSplit[postingsLimit]));
//                }
                currentTerms[fileCounter] = term;
                if (!finalIndex.containsKey(term)) {
                    finalIndex.put(term, postings);
                } else {
                    finalIndex.put(term, finalIndex.get(term) + i.termDelimiter + postings);
                }

                fileCounter++;
            }

            if (mm.getMemoryUsage() >= threshold) {
                writeDictionaryToFinalIndex(currentTerms, false);
//                System.out.println(finalIndex.size()+"\t"+ Arrays.toString(currentTerms));
//                System.out.println("WRITING "+mm.getMemoryUsage());
                System.gc();
            }

        }
        writeDictionaryToFinalIndex(currentTerms, true);
    }

    private String[] chopTermOfArray(String[] termsArray, String term) {
        List<String> newArray = new ArrayList<>(Arrays.asList(termsArray));
        newArray.remove(term);
        return newArray.toArray(new String[0]);
    }

    private int getTermFreq(String postingsString) {
        Indexer i = new Indexer();
        return postingsString.split(i.termDelimiter).length;
    }

    /**
     *
     *
     * @param termFreq - Num of documents where the term is
     * @return
     * @throws IOException
     */
    private double calculateTermIDF(String postingsString) {
        return Math.log10(indexSize / getTermFreq(postingsString));
    }

    /**
     * Workaround: .directory file delete
     */
    private void clear_directoryFile() {
        File f = new File(indexPath + ".directory");
        if (f.exists()) {
            f.delete();
        }
    }
    
//    public static void main(String[] args) throws IOException {
//        Merger m = new Merger();
//        m.createFinalIndex();
//    }

}
