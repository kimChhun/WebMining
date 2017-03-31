package ch.heigvd.wem.interfaces;

import java.io.Serializable;
import java.util.Map;
import ch.heigvd.wem.data.Document;
import ch.heigvd.wem.data.Metadata;
import ch.heigvd.wem.labo1.IndexerImpl.Posting;

/**
 * A dummy class representing the index
 * You should extend it for your implementation
 * This class is Serializable, it will allow you to save it on disk
 */
public abstract class Index implements Serializable {

	private static final long serialVersionUID = -7032327683456713025L;
	public abstract Posting find(String term);
	public abstract void addPosting(String term, long documentId, int offset);
	public abstract void addDocument(Metadata metadata, String content);
	public abstract Document getDocument(long documentId);
	public abstract long documentCount();
	public abstract Map<String, Integer> getWordCounts(long documentId);
	public abstract int getWordCount(long documentId, String word);
	
}
