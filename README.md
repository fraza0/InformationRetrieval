# InformationRetrieval
Information Retrieval Project - Document Indexer | Master's Degree in Informatics Engineering

Create a weighed document indexer (TF-IDF).

This project contains:
* Corpus Reader;
* Tokenizers:
  * Simple String parsing;
  * Stemming (SnowballStemmer);
  * Stopwords Filter (stop.txt);
* Indexer (TF-IDF weights);
* Ranked Retrieval;
* Positional Indexing;
* Indexer Evaluation (Using Cranfield corpus and evaluation queries):
  * Precision;
  * Recall;
  * F-Measure (F1-Score);
  * Mean Average Precision;
  * Mean Precision @ Rank 10;
  * Normalized Discounted Cumulative Gain (NDCG);
  * Query Throughput;
  * Median Query Latency;

To check functionalities execute the following command:
```bash
java -cp target/DocumentIndexer-3.0.jar:src/main/resources/libstemmer.jar main.DocumentIndexer -h
```

Flags: 
* -h, --help
 * -c, --corpusreader --<formato ficheiro input> <caminho ficheiro input> <caminho ficheiro output> -cols col1 col2 (...) coln
  * --tsv
  * --xml
* -t, --tokenizer <tokenizer type: simple or improved> <input path> <output path> <boolean: is first line header?> <if tokenizer type==improved: keep numeric values in text?>
* -i, --indexer <input path> <boolean merge?> <optional: size of index files names (def:3)>
* -m, --merge
* -s, --search "<query>" <number of results>
* -e, --evaluation <number of top documents> <optional:beta (for [f-measure](https://en.wikipedia.org/wiki/F1_score) calculation)>
