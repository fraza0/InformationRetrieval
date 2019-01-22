/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokenizer;

import file_handling.FileHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import text_operations.TextOperations;

/**
 * Simple tokenizer with tolowercase conversion and alphanumeric character removal.
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class SimpleTokenizer {

    public void simpleTokenization(String source_path, String output_path, boolean isFirstLineHeader) throws FileNotFoundException, IOException {
        FileHandler fh = new FileHandler();
        TextOperations to = new TextOperations();

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

        try {
            String line = br.readLine();

            if (isFirstLineHeader) {
                line = br.readLine();
            }

            line = line.toLowerCase();

            String docid;
            while (line != null) {
                docid = line.substring(0, line.indexOf("\t"));
                line = to.removeWhitespaces(to.removeShortHugeWords(to.removeAlphanumeric(line.replace(docid, ""))));

//                System.out.println(docid+"\t"+line);
                fh.write(output_path, docid + "\t" + line);
                line = br.readLine();
            }

        } finally {
            br.close();
        }

    }

}
