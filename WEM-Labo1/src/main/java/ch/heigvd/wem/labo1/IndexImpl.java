package ch.heigvd.wem.labo1;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import ch.heigvd.wem.data.Document;
import ch.heigvd.wem.data.Metadata;
import ch.heigvd.wem.interfaces.Index;
import ch.heigvd.wem.labo1.IndexerImpl.Posting;

public class IndexImpl extends Index {
	private static final long serialVersionUID = 3728664384283588715L;
	
	/** Inverted index */
	private Map<String, Posting> postings = new HashMap<String, Posting>();
	/** Metadata indexed by docId */
	private Map<Long, Metadata> metadata = new HashMap<Long, Metadata>();
	/** Content by docId, used to display an excerpt in the result page */
	private Map<Long, String> data = new HashMap<Long, String>();
	/** For docId, a list of words and their counts, to avoid counting occurrences during the retrieval phase */
	private Map<Long, Map<String, Integer>> wordCounts = new HashMap<Long, Map<String, Integer>>();
	@Override
	public Posting find(String term) {
		return postings.get(term);
	}
	@Override
	public void addPosting(String term, long documentId, int offset) {
		Posting posting;
		if (!postings.containsKey(term)) {
			posting = new Posting(term);
			postings.put(term, posting);
		} else {
			posting = postings.get(term);
		}
		
		posting.put(documentId, offset);
	}
	@Override
	public void addDocument(Metadata metadata, String data) {
		this.metadata.put(metadata.getDocID(), metadata);
		this.data.put(metadata.getDocID(), data);
		countWords(metadata.getDocID());
	}
	
	private void countWords(long documentId) {
		StringTokenizer tokenizer;
		try {
			tokenizer = new CustomStringTokenizer(data.get(documentId), Labo1.COMMON_WORDS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<String, Integer> unsortedCounts = new HashMap<String, Integer>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (unsortedCounts.containsKey(token)) {
				unsortedCounts.put(token, unsortedCounts.get(token) + 1);
			} else {
				unsortedCounts.put(token, 1);
			}
		}
		
		LinkedHashMap<String, Integer> sortedCounts = new LinkedHashMap<String, Integer>();
		unsortedCounts.entrySet().stream().sorted(new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				if (o1.getValue() > o2.getValue()) return -1; //reverse comparator
				else if(o1.getValue() < o2.getValue()) return 1;
				else return 0;
			}
		}).forEach(entry->sortedCounts.put(entry.getKey(), entry.getValue()));
		
		this.wordCounts.put(documentId, sortedCounts);
	}
	
	@Override
	public Map<String, Integer> getWordCounts(long documentId) {
		return this.wordCounts.get(documentId);
	}
	
	@Override
	public int getWordCount(long documentId, String word) {
		return this.wordCounts.get(documentId).get(word);
	}
	
	@Override
	public Document getDocument(long documentId) {
		Metadata metadata = this.metadata.get(documentId);
		if (null == metadata) return null;
		String data = this.data.get(documentId);
		
		Document doc = new Document();
		doc.setMetadata(metadata);
		doc.setContent(data);
		return doc;
	}
	@Override
	public long documentCount() {
		return (long) data.size();
	}
}
