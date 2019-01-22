package text_operations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.tartarus.snowball.ext.porterStemmer;

/**
 * Text operations executed on tokenizers
 *
 * @author Fábio Ferreira
 * @author Rui Frazão
 */
public class TextOperations {

    public TextOperations(boolean useStopWordFilter) throws IOException {
        if (useStopWordFilter) {
            parseStopWords();
        }
    }

    public TextOperations() {
    }

    private final String stopWordsFile = "stop.txt";
    private final porterStemmer stemmer = new porterStemmer();
    private ArrayList<String> stopWords = new ArrayList<>();
    private ArrayList<Character> escapeChars = new ArrayList<>();

    public void setStopWords(ArrayList<String> stopWords) {
        this.stopWords = stopWords;
    }

    public void setRelevantChars(ArrayList<Character> relevantChars) {
        this.escapeChars = relevantChars;
    }

    public String removeAlphanumeric(String line) {
        return line.replaceAll("[^a-z \\n]", " ");
    }

    public String removeShortHugeWords(String line) {
        return line.replaceAll("(\\b\\w{1,2}\\b)|(\\b\\w{25,}\\b)", " ");
    }

    public String removeWhitespaces(String line) {
        return line.replaceAll("[\\s+]{2,}", " ").trim();
    }

    //=== Improved Tokenizer ===\\
    public String removeAlphanumeric(String line, boolean KeepNumbers) {
        return line.replaceAll("[^a-z\\d \\n]", " ");
    }

    public String removeAlphanumeric(String line, String customRegex) {
        return line.replaceAll(customRegex, " ");
    }

    public void addSpecialChar(char specialChar) {
        escapeChars.add(specialChar);
    }

    public String buildCustomRegex(boolean digits) {
        StringBuilder sb = new StringBuilder();
        String regexBase;
        if (digits) {
            regexBase = "^a-z\\d \\n";
        } else {
            regexBase = "^a-z \\n";
        }

        String re_append = "";
        String re_spaces_append = "";
        for (char ch : escapeChars) {
            re_append += "\\" + ch;
            re_spaces_append += "|(\\s+\\" + ch + "\\s+)|(\\" + ch + " )|( \\" + ch + ")";
        }

        return sb.append("[").append(regexBase).append(re_append).append("]").append(re_spaces_append).toString();
    }

    private void parseStopWords() throws IOException {
        ArrayList<String> stopWordsList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(stopWordsFile));

        String line = br.readLine();
        String stopword;

        while (line != null) {
            stopword = line.split(" | ")[0].trim();
            if (!stopword.isEmpty() && !stopword.contains("|")) {                 //CORRIGIR PARSE POR |
                stopWordsList.add(stopword);
            }
            line = br.readLine();
        }

        setStopWords(stopWordsList);
    }

    public String stopwordRemover(String line) throws IOException {
        StringBuilder sb = new StringBuilder();
        String[] lineParse = line.split("\\s+");
//        parseStopWords();
        for (String word : lineParse) {
            if (!stopWords.contains(word)) {
                sb.append(word).append(" ");
            }
        }
        return sb.toString();
    }

    public String stemming(String line) {
        StringBuilder sb = new StringBuilder();
        String[] lineParse = line.split("\\s+");
        for (String word : lineParse) {
            stemmer.setCurrent(word);
            stemmer.stem();
            sb.append(stemmer.getCurrent()).append(" ");
        }
        return sb.toString();
    }

}
