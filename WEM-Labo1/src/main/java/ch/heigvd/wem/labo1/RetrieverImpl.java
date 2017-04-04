package ch.heigvd.wem.labo1;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import ch.heigvd.wem.data.Document;
import ch.heigvd.wem.interfaces.Index;
import ch.heigvd.wem.interfaces.Retriever;
import ch.heigvd.wem.labo1.IndexerImpl.Posting;
import ch.heigvd.wem.tools.Tools;

public class RetrieverImpl extends Retriever {
	private Index index;
	private WeightingStrategy weightingStrategy;
	
	public RetrieverImpl(Index index, WeightingType weightingType) {
		super(index, weightingType);
		this.index = index;
		switch(weightingType) {
		case TF_IDF:
			this.weightingStrategy = new TfIdfWeightingStrategy();
			break;
		case NORMALIZED_FREQUENCY:
			this.weightingStrategy = new NormalizedFrequencyWeightingStrategy();
			break;
		default:
			throw new IllegalArgumentException("Unsupported weighting type: " + weightingType);
		}
	}
	
	public Map<Document, Double> processQuery(String query) {
		Map<String, Double> vectorized = vectorize(query);
		return processQuery(vectorized);
	}
	
	/**
	 * Return a vector representation of a text string using word counts as value
	 * (this is used for queries,
	 * since the word counts for documents is stored in the index to avoid repeating the
	 * steps for each new query).
	 * The map is sorted in descending order allow quick access to the maximum frequency.
	 * @param text
	 * @return
	 */
	private Map<String, Integer> countVector(String text) {
		StringTokenizer tokenizer;
		try {
			tokenizer = new CustomStringTokenizer(text, Labo1.COMMON_WORDS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//count and store in a regular unsorted map
		Map<String, Integer> unsortedVector = new HashMap<String, Integer>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (unsortedVector.containsKey(token)) {
				unsortedVector.put(token, unsortedVector.get(token) + 1);
			} else {
				unsortedVector.put(token, 1);
			}
		}
		
		//sort and return vector
		LinkedHashMap<String, Integer> sortedVector = new LinkedHashMap<String, Integer>();
		unsortedVector.entrySet().stream()
				.sorted(new Tools.ReverseMapEntryComparator<>(String.class, Integer.class))
				.forEach(entry->sortedVector.put(entry.getKey(), entry.getValue()));
		return sortedVector;
	}

	/**
	 * Convert a count vector into a weight vector.
	 * @param text
	 * @return
	 */
	private Map<String, Double> weightVector(Map<String, Integer> countVector) {
		Map<String, Double> unsortedWeights = new HashMap<String, Double>();
		countVector.entrySet().stream().forEach(entry->{
				String term = entry.getKey();
				long documentFrequency = 0;
				Posting posting = index.find(term);
				if (posting != null) documentFrequency = posting.getDocumentFrequency();
				unsortedWeights.put(
						term,
						weightingStrategy.calculate(
								term,
								documentFrequency,
								countVector));
			});
		
		return unsortedWeights;
	}
	
	/**
	 * Vectorize a text string (used to vectorize the query)
	 * @param text to vectorize
	 * @return {@link Map} of terms and weights
	 */
	private Map<String, Double> vectorize(String text) {
		return weightVector(countVector(text));
	}
	
	/**
	 * Return a sorted list of documents mapped to their cosine similarity the the {@code queryVector}
	 * in descending order.
	 * @param queryVector a document-score map, sorted by descending cosine score
	 * @return
	 */
	private Map<Document, Double> processQuery(Map<String, Double> queryVector) {
		//maps documentIds to their cosine score
		Map<Long, Double> unsortedSimilarities = new HashMap<Long, Double>();		
		for (String term : queryVector.keySet()) {
			Posting posting = index.find(term);
			if (posting == null) continue;
			for (Long documentId : posting) {
				Map<String, Double> documentVector = weightVector(index.getWordCounts(documentId));
				unsortedSimilarities.put(documentId, cosineScore(queryVector, documentVector));
			}
		}
		
		//sort and return results
		LinkedHashMap<Document, Double> sortedResults = new LinkedHashMap<Document, Double>();
		unsortedSimilarities.entrySet().stream()
				.sorted(new Tools.ReverseMapEntryComparator<>(Long.class, Double.class))
				.forEach(entry->sortedResults.put(
						index.getDocument(entry.getKey()),
						entry.getValue()));
					
		return sortedResults;
	}
	
	/**
	 * Calculate cosine score by means of vector dot product and dividing only by the norm of the
	 * document vector since the query vector length is constant
	 * @param queryVector
	 * @param documentVector
	 * @return cosine score of {@code queryVector} and {@code documentVector}
	 */
	private Double cosineScore(Map<String, Double> queryVector, Map<String, Double> documentVector) {
		double dotProduct = 0.d;
		
		for (Map.Entry<String, Double> queryEntry : queryVector.entrySet()) {
			if (documentVector.containsKey(queryEntry.getKey())) {
				dotProduct += queryEntry.getValue() * documentVector.get(queryEntry.getKey());
			}
		}
		return dotProduct / norm(documentVector);
	}

	private double norm(Map<String, Double> vector) {
		double normSquared = 0.d;
		for (double value : vector.values()) {
			normSquared += value * value;
		}
		return Math.sqrt(normSquared);
	}
	
	public static abstract class WeightingStrategy {
		protected double log2(double val) {return Math.log(val)/Math.log(2);}
		/**
		 * Normalize weight vector by finding the maximum wieght and dividing all values by it
		 * @param vector to normalize
		 */
		protected void normalizeWeights(Map<String, Double> vector) {
			Double maxWeight = Double.NEGATIVE_INFINITY;
			//find maximum weight
			for (Map.Entry<String, Double> entry : vector.entrySet()) {
				if (entry.getValue() > maxWeight) maxWeight = entry.getValue();
			}
			
			//divide all weigth by the maximum
			for (Map.Entry<String, Double> entry : vector.entrySet()) {
				entry.setValue(entry.getValue()/maxWeight);
			}
		}
		public abstract double calculate(String term, double documentFrequency, Map<String, Integer> wordCounts);
	}
	
	public class TfIdfWeightingStrategy extends WeightingStrategy {
		public double calculate(String term, double documentFrequency, Map<String, Integer> wordCounts) {
			int termFrequency = wordCounts.get(term);
			double idf = log2(index.documentCount() / documentFrequency);
			double tf = log2(termFrequency + 1);
			return tf * idf;
		}
	}
	
	public class NormalizedFrequencyWeightingStrategy extends WeightingStrategy {
		public double calculate(String term, double documentFrequency, Map<String, Integer> wordCounts) {
			//get the most frequent word count, i.e the first since entries are in descending order
			Iterator<Integer> iterator = wordCounts.values().iterator();
			int termFreq = wordCounts.get(wordCounts);
			int maxFreq = 1;
			if (iterator.hasNext()) maxFreq = iterator.next();
			
			return termFreq / (double) maxFreq;
		}
		
		@Override
		protected void normalizeWeights(Map<String, Double> vector) {
			if (weightingStrategy instanceof NormalizedFrequencyWeightingStrategy) {
				return; //already normalized; NormalizedFrequencyWeightStrategy divides by maxFreq in calculate()
			} else {
				super.normalizeWeights(vector);
			}
		}
	}

	@Override
	public Map<String, Double> searchDocument(Integer documentId) {
		return weightVector(index.getWordCounts(documentId));
	}

	@Override
	public Map<Long, Double> searchTerm(String term) {
		//handle as single-word query, our implementation handles all terms in vector form
		//call internal implementation and convert from Document-Double pairs to Long-Double pairs
		//by picking the docID of the document
		Map<Long, Double> results = new LinkedHashMap<Long, Double>();
		Map<Document, Double> docs = processQuery(weightVector(Collections.singletonMap(term, 1)));
		for (Map.Entry<Document, Double> result : docs.entrySet()) {
			results.put(result.getKey().getMetadata().getDocID(), result.getValue());
		}
		return results;
	}

	@Override
	public Map<Long, Double> executeQuery(String query) {
		//call internal implementation and convert from Document-Double pairs to Long-Double pairs
		//by picking the docID of the document
		Map<Long, Double> results = new LinkedHashMap<Long, Double>();
		Map<Document, Double> docs = processQuery(query);
		for (Map.Entry<Document, Double> result : docs.entrySet()) {
			results.put(result.getKey().getMetadata().getDocID(), result.getValue());
		}
		return results;
	}
}
