package text_corpus;

import file_handling.FileHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Corpus Reader
 * 
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class CorpusReader {
//    public static void main (String[] args) {
//        FileHandler fh = new FileHandler();
//        CorpusReader c = new CorpusReader();
//        String path = "./amazon_reviews_us_Watches_v1_00.tsv";
//        String bigPath = "./amazon_reviews_us_Wireless_v1_00.tsv";
//        String[] columns = new String[]{"product_title", "review_headline", "review_body"};
//        String outputPath = "corpus.tsv";
//        
//        c.corpusReader(path, outputPath, columns);
//
//    }
    
    public void corpusReader(String filename, String outputfile, String[] columns) {
        
            String header[];
            int[] headerIndexes = null;
            BufferedReader TSVFile = null;
            String line = "";
            StringBuilder sb = new StringBuilder();
            FileHandler fh = new FileHandler();
            boolean last = false;
            int docId = 0;
            
            // Clear output file if it already exists
            File f = new File(outputfile);
            if(f.isFile()) {
                PrintWriter writer;
                try {
                    writer = new PrintWriter(outputfile);
                    writer.print("");
                writer.close();
                } catch (FileNotFoundException ex) {
                    return;
                }
            }
            
            
            try {
                TSVFile = new BufferedReader(new FileReader(filename));
            } catch (FileNotFoundException ex) {
                System.err.println("File not found");
                return;
            }
            
            try {
                line = TSVFile.readLine();
                header = line.split("\t");
                headerIndexes = selectedColumns(header, columns);
            } catch (IOException ex) {
            }
           
        try {
            while(line != null) {
                String[] tmp = line.split("\t");
                String[] productTmp = new String[columns.length + 1];
                
                if(docId == 0)
                    productTmp[0] = "docId";
                else
                    productTmp[0] = Integer.toString(docId);
                
                for(int i = 1; i <= columns.length; i++)
                    productTmp[i] = tmp[headerIndexes[i-1]];
                
                for(int j = 0; j < productTmp.length; j++) {
                    if(j == productTmp.length - 1)
                        last = true;
                    if(!last) {
                        sb.append(productTmp[j]);
                        sb.append("\t");
                    }
                    else {
                        sb.append(productTmp[j]);
                        last = false;
                    }
                }
                
                fh.write(outputfile, sb.toString());
                sb.setLength(0);
                
                docId++;
                line = TSVFile.readLine();
            }
        } catch (IOException ex) {
        }
    }
   
    // return indexes of desired columns
    public static int[] selectedColumns(String[] header, String[] columns) {
        int[] columnsPositions = new int[columns.length];
        int index = 0;
        for(int i = 0; i < header.length; i++) {
            if(Arrays.asList(columns).contains(header[i])) {
                columnsPositions[index] = i;
                index++;
            }
        }
        return columnsPositions;
    }
}