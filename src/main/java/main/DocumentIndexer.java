package main;

import indexer.Indexer;
import indexer.Merger;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import retrieval.Evaluation;
import retrieval.RankedRetrieval;
import text_corpus.CorpusReader;
import text_corpus.XMLCorpusReader;
import tokenizer.ImprovedTokenizer;
import tokenizer.SimpleTokenizer;

/**
 * Document Indexer Terminal Interface
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class DocumentIndexer {

    public DocumentIndexer(String[] args) throws IOException, ParserConfigurationException, SAXException {
        long initTime = System.currentTimeMillis(), endTime;
        switch (args[0]) {
            case "-h":
            case "--help":
                System.out.println("This program is a Document Indexer, which consists of a corpus reader, tokenizer and indexer.");
                System.out.println("  -c,  --corpusreader \tParse a document using the Corpus Reader.");
                System.out.println("  -t,  --tokenizer \tTokenize a document using a simple tokenizer (text to lowercase, alphanumeric characters removal) or an improved tokenizer (stopword removal, stemming, acceptance of alphanumeric characters).");
                System.out.println("  -i,  --indexer \tCreate a document inverted index.");
                System.out.println("  -m,  --merge \t\tMerge indexer files in a final inverted index structure.");
//                System.out.println("  -f, --fullindex \t Execute the full indexer pipeline.");
                System.out.println("  -s,  --search \tSearch query in documents.");
                System.out.println("  -e,  --evaluation\tEvaluation and efficiency metrics (Cranfield example).");

                break;
            case "-c":
            case "--corpusreader":
                if (args.length < 4) {
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " --<input file format> <input file path> <output file path> -cols col1 col2 (...) coln");
                    break;
                }
                if (args.length < 5 || !args[4].equalsIgnoreCase("-cols")) {
                    System.out.println("Please use the -cols flag to indicate which columns to keep. Use * for every column");
                    break;
                }
                String cInputFile = args[2];
                String cOutputFile = args[3];
                String[] cColumns = new String[args.length - 5];
                int cols_counter = 0;
                for (int i = 5; i < args.length; i++) {
                    cColumns[cols_counter] = args[i];
                    cols_counter++;
                }
                
                switch(args[1]) {
                    case "--tsv":
                        executeCorpusReader(cInputFile, cOutputFile, cColumns);
                        break;
                    case "--xml":
                        executeXMLCorpusReader(cInputFile, cOutputFile, cColumns);
                        break;
                }
               
                endTime = System.currentTimeMillis();
                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
                break;
            case "-t":
            case "--tokenizer":
                if (args.length < 5) {
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " <tokenizer type> <input file path> <output file path> <boolean first line header> <improved: keep numeric values>");
                    break;
                }
                if (!args[1].equalsIgnoreCase("simple") && !args[1].equalsIgnoreCase("improved")) {
                    System.out.println("Please use tokenizer type 'simple' or 'improved'");
                    break;
                }

                String tTokenizerType = args[1];
                String tInputFile = args[2];
                String tOutputFile = args[3];
                boolean tIsFirstLineHeader = args[4].equalsIgnoreCase("true");
                boolean tKeepDigits = false;
                if (args.length > 5) {
                    tKeepDigits = args[5].equalsIgnoreCase("true");
                }

                if (tTokenizerType.equalsIgnoreCase("simple")) {
                    executeSimpleTokenizer(tInputFile, tOutputFile, tIsFirstLineHeader);
                } else {
                    executeImprovedTokenizer(tInputFile, tOutputFile, tKeepDigits, tIsFirstLineHeader);
                }

                endTime = System.currentTimeMillis();
                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
                break;
            case "-i":
            case "--indexer":
                if (args.length < 3) {
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " <input file path> <merge?> <OPT:indexfilenamesize>");
                    break;
                } // <output directory>
                String iInputfile = args[1];
                boolean merge = Boolean.valueOf(args[2]);
                int filenameSize = 3; //default
                if (args.length==4){
                    filenameSize = Integer.parseInt(args[3]);
                }
                
                if (filenameSize < 1 || filenameSize > 3) {
                    System.out.println("Please use a file name size between 1 and 3");
                    break;
                }

                executeIndexer(iInputfile, merge, filenameSize);

                endTime = System.currentTimeMillis();
                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
                break;
                
            case "-m":
            case "--merge":
                if (args.length < 1) {
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " <OPT:indexfilenamesize>");
                    break;
                }
                int filenameSizem = 3; //default
                if (args.length==2){
                    filenameSizem = Integer.parseInt(args[1]);
                }
                                
                executeMerge(filenameSizem);
                
                break;
//            case "-f":
//            case "--fullindex":
//                if (args.length < 6) {//<output file name> 
//                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " <input file path> -cols col1 col2 (...) coln");
//                    break;
//                }
//
//                if (!args[2].equalsIgnoreCase("-cols")) {
//                    System.out.println("Please use the -cols flag to indicate which columns to keep. Use * for every column");
//                    break;
//                }
//                boolean tokenSpecFound = false;
//                int pos = -1;
//                for (int i = 1; i < args.length; i++) {
//                    if (args[i].equalsIgnoreCase("-t")) {
//                        tokenSpecFound = true;
//                        pos = i;
//                    }
//                }
//                if (!tokenSpecFound) {
//                    System.out.println("==Tokenizer Not Specified - Using ImprovedTokenizer==");
//                }
////                if (!args[pos+1].equalsIgnoreCase("simple") && !args[pos+1].equalsIgnoreCase("improved")){ System.out.println("Please use tokenizer type 'simple' or 'improved'"); break; }
//
//                String input = args[1];
////                String output = args[2];
//                String[] columns;
//                if (args[args.length - 1].equalsIgnoreCase("simple")) {
//                    tokenSpecFound = false;
//                }
//
//                if (tokenSpecFound) {
//                    columns = new String[args.length - 6];
//                } else {
//                    columns = new String[args.length - 4];
//                }
//
//                for (int i = 4; i < args.length; i++) {
//                    columns[i - 4] = args[i];
//                }
//
//                executeFullIndexer(input, columns, tokenSpecFound);
//
//                endTime = System.currentTimeMillis();
//                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
//                break;
            case "-s":
            case "--search":
                if (args.length < 3) {//<output file name> 
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " \"<query>\" <number of results>");
                    break;
                }
                try {
                    executeSearch(args[1], Integer.parseInt(args[2])); 
                }
                catch(NullPointerException e) {
                    System.out.println("The search did not match any documents.");
                }
                
                endTime = System.currentTimeMillis();
                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
                break;
            case "-e":
            case "--evaluation":
                if (args.length < 2) {//<output file name> 
                    System.out.println("> Invalid Syntax. Use DocumentIndexer " + args[0] + " <number of results> <OPT:beta>");
                    break;
                }
                
                if (args.length == 2)
                    executeEvaluation(Integer.parseInt(args[1]));
                else
                    executeEvaluation(Integer.parseInt(args[1]),Float.parseFloat(args[2]));
                
                
                endTime = System.currentTimeMillis();
                System.out.println("Total time: " + (endTime - initTime) / 1000 + "seg");
                break;
            default:
                System.out.println("Invalid Option. Try '-h' or '--help' for more information.");
                break;
        }
    }

    private void executeCorpusReader(String inputFile, String outputFile, String[] columns) {
        CorpusReader cr = new CorpusReader();
        cr.corpusReader(inputFile, outputFile, columns);
    }
    
    private void executeXMLCorpusReader(String inputPath, String outputFile, String[] columns) throws ParserConfigurationException, SAXException, IOException {
        XMLCorpusReader cr = new XMLCorpusReader();
        cr.corpusReader(inputPath, outputFile, columns);
    }

    private void executeSimpleTokenizer(String inputFile, String outputFile, boolean isFirstLineHeader) throws IOException {
        SimpleTokenizer st = new SimpleTokenizer();
        st.simpleTokenization(inputFile, outputFile, isFirstLineHeader);
    }

    private void executeImprovedTokenizer(String inputFile, String outputFile, boolean keepDigits, boolean isFirstLineHeader) throws IOException {
        ImprovedTokenizer it = new ImprovedTokenizer();
        it.improvedTokenization(inputFile, outputFile, keepDigits, isFirstLineHeader);
    }

    private void executeIndexer(String inputfile, boolean merge) throws IOException {
        Indexer i = new Indexer(true);
        i.SPIMIInvert(inputfile);
//        MemoryManager mm = new MemoryManager();
//        System.out.println("MEMORY: "+mm.getUsedMemory());
        System.gc();
        if(merge){
            executeMerge();
        }
    }
    
    private void executeIndexer(String inputfile, boolean merge, int filenameSize) throws IOException {
        Indexer i = new Indexer(true);
        i.SPIMIInvert(inputfile);
//        MemoryManager mm = new MemoryManager();
//        System.out.println("MEMORY: "+mm.getUsedMemory());
        System.gc();
        if(merge){
            executeMerge(filenameSize);
        }
    }
    
    private void executeMerge(int filenameSize) throws IOException {
        Merger m = new Merger(filenameSize);
        m.createFinalIndex();
    }
    
    private void executeMerge() throws IOException {
        Merger m = new Merger(3);
        m.createFinalIndex();
    }

    private void executeFullIndexer(String input, String[] columns, boolean useSimpleTokenizer) throws IOException {
        String corpusPath = "corpus.tsv";
        String tokenized = "tokenization.tsv";

        File fcorpus = new File(corpusPath), ftoken = new File(tokenized);
        if(fcorpus.exists()) fcorpus.delete();
        if(ftoken.exists()) ftoken.delete();

        CorpusReader c = new CorpusReader();
        c.corpusReader(input, corpusPath, columns);

        if (useSimpleTokenizer) {
            executeSimpleTokenizer(corpusPath, tokenized, true);
        } else {
            executeImprovedTokenizer(corpusPath, tokenized, false, true);
        }

        executeIndexer(tokenized, true);

//        fcorpus.delete();
//        ftoken.delete();
    }
    
    private void executeSearch(String query, int topKResults) throws IOException {
        RankedRetrieval r = new RankedRetrieval();
        r.retrieveResultsMemory(query, topKResults);
    }
    
    private void executeEvaluation(int rank) throws IOException {
        Evaluation e = new Evaluation(rank, 1);
        System.out.println(e.getEvaluation());
    }
    
    private void executeEvaluation(int rank, float beta) throws IOException {
        Evaluation e = new Evaluation(rank, beta);
        System.out.println(e.getEvaluation());
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        if (args.length < 1) {
            System.out.println("Invalid Syntax. Try '-h' or '--help' for more information.");
            return;
        }
        DocumentIndexer m = new DocumentIndexer(args);
    }

}
