package text_corpus;

import file_handling.FileHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XML Corpus Reader
 * 
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class XMLCorpusReader {

    /**
     * Check if the output file already exists; in affirmative case delete it;
     * Initialize the corpus reader
     * 
     * @param inputpath
     * @param outputfile
     * @param columns
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public void corpusReader(String inputpath, String outputfile, String[] columns) throws ParserConfigurationException, SAXException, IOException {
        File baseFile = new File(outputfile);
        if (baseFile.length() > 0) {
            baseFile.delete();
        }
        readCorpus(inputpath, outputfile, columns);
    }
    
    /**
     * Get all files from corpus directory
     * 
     * @param directory
     * @return Array with all files in directory
     */
    private File[] getCorpusFiles(String directory) {
        File docsDir = new File(directory);
        
        File[] files = docsDir.listFiles((File pathname) -> {
            String name = pathname.getName().toLowerCase();
            return name.startsWith(directory) && pathname.isFile();
        });
        return files;
    }
    
    /**
     * Checks if the output file already exists and in affirmative case cleans it;
     * Read the corpus from one file or multiple files in a directory;
     * 
     * @param inputpath
     * @param outputfile
     * @param columns
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    private void readCorpus(String inputpath, String outputfile, String[] columns) throws ParserConfigurationException, SAXException, IOException {
        File old = new File(outputfile);
        if(old.isFile()) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(outputfile);
                writer.print("");
            writer.close();
            } catch (FileNotFoundException ex) {
                return;
            }
        }
        
        File path = new File(inputpath);
        
        if(path.isDirectory()) {
            inputpath = inputpath.replaceAll("\\.", "").replaceAll("/","");
            File[] files = getCorpusFiles(inputpath);
            
            for (File f: files){
                parseDocument(f, outputfile, columns);
            }        
        }
        else {
            File f = new File(inputpath);
            parseDocument(f, outputfile, columns);
        }
    }
    
    /**
     * Parse a XML document; Get the DOCNO field and allows to choose the other columns;
     * Write the results on outputfile
     * 
     * @param file
     * @param outputfile
     * @param columns
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    private void parseDocument(File file, String outputfile, String[] columns) throws SAXException, IOException, ParserConfigurationException {
        FileHandler fh = new FileHandler();
        
        StringBuilder sb;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        sb = new StringBuilder();
        sb.append(document.getElementsByTagName("DOCNO").item(0).getTextContent().trim()).append("\t");
        for (String col : columns){
            sb.append(document.getElementsByTagName(col.toUpperCase()).item(0).getTextContent().replaceAll("\\s+", " ").trim()).append("\t");
        }
        fh.write(outputfile, sb.toString());
    }
    
    /*public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        XMLCorpusReader xcr = new XMLCorpusReader();
        String[] cols = new String[1];
        //cols[0] = "TITLE";
        //cols[1] = "text";
        cols[0] = "author";
        
        
        //xcr.corpusReader("./cranfield","xmlOut.txt",cols);
        //xcr.corpusReader("solo.xml","xmlOut.txt",cols);
    }*/
}
