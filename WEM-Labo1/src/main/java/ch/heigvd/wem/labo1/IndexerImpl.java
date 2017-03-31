package ch.heigvd.wem.labo1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ch.heigvd.wem.data.Metadata;
import ch.heigvd.wem.interfaces.Index;
import ch.heigvd.wem.interfaces.Indexer;

public class IndexerImpl implements Indexer {
	
	private Index index = new IndexImpl();

	public synchronized void index(Metadata metadata, String content) {
		StringTokenizer tokenizer = new StringTokenizer(content);
		int offset = 0;
		index.addDocument(metadata, content);
		while (tokenizer.hasMoreElements()) {
			String term = tokenizer.nextToken();
			index.addPosting(term, metadata.getDocID(), offset);
			offset++;
		}
	}

	public void finalizeIndexation() {
		
	}

	public Index getIndex() {
		return index;
	}

	public static class Posting implements Serializable, Iterable<Long> {
		private static final long serialVersionUID = 5797426333884134981L;
		
		String term;
		/**
		 * list of ids
		 */
		private long documentFrequency = 0;
		private Map<Long, List<Integer>> occurences = new HashMap<Long, List<Integer>>();
		
		public Posting(String term) {
			this.term = term;
		}
		
		public void put(long documentId, int offset) {
			if (occurences.containsKey(documentId)) {
				occurences.get(documentId).add(offset);
			} else {
				LinkedList<Integer> offsets = new LinkedList<Integer>();
				offsets.add(offset);
				occurences.put(documentId, offsets);
			}
			documentFrequency++;
		}
		
		public Set<Long> documents() {
			return occurences.keySet();
		}
		
		public Iterator<Long> iterator() {
			return occurences.keySet().iterator();
		}
		
		public List<Integer> offsets(long documentId) {
			return occurences.get(documentId);
		}
		
		public long getDocumentFrequency() {
			return documentFrequency;
		}
	}
}
