package indexer;

import java.util.HashMap;
import java.util.Map;

/**
 * Matching of first letter of a term to the file in each will be written, based on language.
 * 
 * Based on data retrieved from http://norvig.com/mayzner.html
 * 
 * @author Rui Frazão
 * @author Fábio Ferreira
 */
public class IndexCorrespondenceMatrix {

    private Map<Character, String> ICM = new HashMap<>();
    private final String extension = ".ind";
    
    /**
     * Creates a Map of the matches
     * 
     * @param lang
     */
    public IndexCorrespondenceMatrix(String lang) {
        switch(lang){
            case "ENGLISH":
                ICM.put('e', "e.ind");
                ICM.put('t', "t.ind");
                ICM.put('a', "a.ind");
                ICM.put('o', "o.ind");
                ICM.put('i', "i.ind");
                ICM.put('n', "n.ind");
                ICM.put('s', "s.ind");
                ICM.put('r', "rh.ind");
                ICM.put('h', "rh.ind");
                ICM.put('l', "l.ind");
                ICM.put('d', "d.ind");
                ICM.put('c', "c.ind");
                ICM.put('u', "um.ind");
                ICM.put('m', "um.ind");
                ICM.put('f', "fp.ind");
                ICM.put('p', "fp.ind");
                ICM.put('g', "gwybv.ind");
                ICM.put('w', "gwybv.ind");
                ICM.put('y', "gwybv.ind");
                ICM.put('b', "gwybv.ind");
                ICM.put('v', "gwybv.ind");
                ICM.put('k', "kxjqz.ind");
                ICM.put('x', "kxjqz.ind");
                ICM.put('j', "kxjqz.ind");
                ICM.put('q', "kxjqz.ind");
                ICM.put('z', "kxjqz.ind");
                break;
            default:
                System.out.println("Language not supported yet.");
                return;
        }
    }
    
    public String getFileByInitialLetter(char initialLetter){
        return ICM.get(initialLetter);
    }
    
}
