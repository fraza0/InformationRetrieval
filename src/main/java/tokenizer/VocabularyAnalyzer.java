/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokenizer;

import file_handling.FileHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VocabularyAnalyzer {
    
    public void getVocabulary(String filepath){
        FileHandler fh = new FileHandler();
        HashMap<String, HashMap<Integer, Integer>> vocabulary = new HashMap<>();
        HashMap<Integer, Integer> docid_frequence;
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            try {
                String line = br.readLine();   
                String[] words;
                int word_counter, docid;
                while (line != null) {
                    docid = Integer.parseInt(line.split("\t")[0]);
                    words = line.replaceAll("\\d[\\s]{2,}", "").split(" ");
                    for(String word: words){
                        docid_frequence = new HashMap<>();
                        if(vocabulary.containsKey(word)){
                            if(!vocabulary.get(word).containsKey(docid)){
                                docid_frequence.put(docid, 1);
                                vocabulary.get(word).put(docid, 1);
                            } else {
                                docid_frequence.put(docid, vocabulary.get(word).get(docid)+1);
                                vocabulary.get(word).put(docid, vocabulary.get(word).get(docid)+1);
                            }
                        } else {
                            docid_frequence.put(docid, 1);
                            vocabulary.put(word, docid_frequence);
                        }
//                        fh.write("vocab_analysis.txt", docid+"\t"+word+"\t"+vocabulary.get(word).get(docid));
                    }
                    line = br.readLine();
                }
                
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            System.err.println("IOExeption " + ex.getMessage());
        }
        
        
//        for(String word: vocabulary.keySet()){
//            for(Integer docid: vocabulary.get(word).keySet()){
//                System.out.println("vocab_analysis.txt", docid+"\t"+word+"\t"+vocabulary.get(word).get(docid));
//            }
//        }
    }
    
    public void countFirstTerms(int nTerms, String filepath, String outputpath) throws IOException {
        FileHandler fh = new FileHandler();
        int count = 10;
		
        List<Map<String, List<String>>> first = new ArrayList<>();
        BufferedReader br = null;

        try {
            for(File file : new File(filepath).listFiles()) {
                br = new BufferedReader(new FileReader(file));
                String line;
                Map<String, List<String>> first_terms = new HashMap<>();

                while(((line = br.readLine()) != null) && count != 0) {
                    String[] numPostings = line.split(",");
                    if(numPostings.length > 2) {
                        continue;
                    } else {
                        String term = numPostings[0];
                        if(!first_terms.containsKey(file.getPath())) {
                            first_terms.put(file.getPath(), new ArrayList<String>());
                            first_terms.get(file.getPath()).add(term);
                        } else {
                            first_terms.get(file.getPath()).add(term);
                        }
                        count--;
                    }
                }

                first.add(first_terms);

                System.out.println("FILE: " + file.getPath());
                count = 10;
            }
            
            for(Map<String, List<String>> file : first) {
                for(Map.Entry<String, List<String>> entry : file.entrySet()) {
                    fh.write(outputpath, entry.getKey()+" "+ entry.getValue()+"\n");
                }				
            }
        } catch(IOException e) {
                e.printStackTrace();
        }
        br.close();
    }
    
    public static void main(String[] args) throws IOException {
        VocabularyAnalyzer va = new VocabularyAnalyzer();
        va.countFirstTerms(10, "index/Improved", "index/counts_improved_2.txt");
    }
    
}
