# InformationRetrieval
Information Retrieval Project - Document Indexer | Master's Degree in Informatics Engineering

Create a weighed document indexer (TF-IDF)

This project contains:
* Corpus Reader;
* Tokenizers:
  * Simple String parsing;
  * Stemming;
  * Stopwords Filter;
* Indexer (TF-IDF weights);
* Ranked Retrieval;
* Positional Indexing;
* Indexer Evaluation (Using cranfield-corpus):
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
java -cp target/DocumentIndexer-3.0.jar main.DocumentIndexer -h
```
