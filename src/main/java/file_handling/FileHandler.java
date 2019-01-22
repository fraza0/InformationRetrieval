package file_handling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * File writing using BufferedWriter
 *
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class FileHandler {

    public FileHandler() {
    }
    
    public String readIndexInfo(String filepath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            return br.readLine();
        } finally {
            br.close();
        }
    }

    public void write(String filepath, String line) {
        try (
                FileWriter fw = new FileWriter(filepath, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);) {
            out.println(line);
        } catch (IOException e) {
            System.err.println("IOException - " + e.getMessage());
        }
    }

}
