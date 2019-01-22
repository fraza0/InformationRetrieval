package tokenizer;

import file_handling.FileHandler;
import file_handling.MemoryManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import text_operations.TextOperations;

/**
 * Tokenizer with tolowercase conversion and alphanumeric character removal,
 * stopword filter and stemming
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class ImprovedTokenizer {

    private final double threshold = 70;

    public ImprovedTokenizer() {

    }

    /**
     * Apply tokenization to a file
     *
     * @param source_path
     * @param output_path
     * @param escapeDigits
     * @param isFirstLineHeader
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean improvedTokenization(String source_path, String output_path, boolean escapeDigits, boolean isFirstLineHeader) throws FileNotFoundException, IOException {
        FileHandler fh = new FileHandler();
        MemoryManager mm = new MemoryManager();
        TextOperations to = new TextOperations(true);

        List<String> arrayBuffer = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(source_path));
        
        File old = new File(output_path);
        if(old.isFile()) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(output_path);
                writer.print("");
            writer.close();
            } catch (FileNotFoundException ex) {}
        }

        String customRegex = to.buildCustomRegex(escapeDigits);

        try {
            String line = br.readLine();

            if (isFirstLineHeader) {
                line = br.readLine();
            }

            String docid;
            while (line != null) {

                docid = line.substring(0, line.indexOf("\t"));
                line = line.replace(docid, " ").toLowerCase();
                line = to.removeAlphanumeric(line, customRegex);
                line = to.stopwordRemover(line);
                line = to.stemming(line);
                line = to.removeShortHugeWords(line);
                line = to.removeWhitespaces(line);

//                System.out.println(docid+"\t"+line);
//                fh.write(output_path, docid + "\t" + line);
                arrayBuffer.add(docid + "\t" + line);

                if (mm.getMemoryUsage() >= threshold) {
                    arrayBuffer.forEach(s -> fh.write(output_path, s));
                    arrayBuffer.clear();
                    System.gc();
                }

                line = br.readLine();
            }

            arrayBuffer.forEach(s -> fh.write(output_path, s));
            arrayBuffer.clear();
            System.gc();

        } finally {
            br.close();
        }

        return true;
    }

    /**
     * Apply improved tokenization to query for ranked retrieval
     *
     * @param query
     * @param escapeDigits
     * @return
     * @throws IOException
     */
    public String queryTokenization(String query, boolean escapeDigits) throws IOException {
        TextOperations to = new TextOperations(true);

        String customRegex = to.buildCustomRegex(escapeDigits);

        query = query.toLowerCase();
        query = to.removeAlphanumeric(query, customRegex);
        query = to.stopwordRemover(query);
        query = to.stemming(query);
        query = to.removeShortHugeWords(query);
        query = to.removeWhitespaces(query);

//        System.out.println(query);
        return query;
    }

}
