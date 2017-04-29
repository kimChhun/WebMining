package ch.heigvd.wem.linkanalysis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import ch.heigvd.wem.data.Document;
import ch.heigvd.wem.interfaces.Index;
import ch.heigvd.wem.tools.Tools;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageRank implements RankingStrategy {
	private Index index;
	
	@Override
	public void setIndex(Index index) {
		this.index = index;
	}
	
	/**
	 * Process similarity-based search results,
	 * calculate PageRank for each one and order them by
	 * descending score (similarity and pageRank)
	 */
	@Override
	public Map<Long, Double> rank(Map<Long, Double> results) {
		long[] documentIds = documentIdArray(results);
		Map<Long, Integer> indices = indices(documentIds);
		AdjacencyMatrix matrix = buildAdjacencyMatrix(results, indices);
		Map<Long, Double> unsortedResults = new HashMap<Long, Double>();
		
		//run 5 iterations of the PageRank algorhithm
		Vector<Double> pr = new Vector<Double>(results.size());
		for (int i=0 ; i<results.size() ; i++) pr.add(1D/results.size());
		Vector<Double> pageRanks = null;
		for (int i=0 ; i<5 ; i++) {
			pageRanks = LinkAnalysis.calculatePRc(matrix, pr);
			pr = pageRanks;
		}
		
		//assign new score to results
		int i=0;
		for (Map.Entry<Long, Double> entry : results.entrySet()) {
			long documentId = entry.getKey();
			double similarity = entry.getValue();
			double pageRank = pageRanks.get(i);
			unsortedResults.put(documentId, .5D * (similarity + pageRank));
			i++;
		}
		
		//sort by descending score
		Map<Long, Double> sortedResults = new LinkedHashMap<Long, Double>();
		unsortedResults.entrySet().stream()
		.sorted(new Tools.ReverseMapEntryComparator<>(Long.class, Double.class))
		.forEach(entry -> sortedResults.put(entry.getKey(), entry.getValue()));
		
		return sortedResults;
	}

	private long[] documentIdArray(Map<Long, Double> results) {
		long[] output = new long[results.size()];
		int i=0;
		for (Long documentId : results.keySet()) {
			output[i] = documentId;
			i++;
		}
		return output;
	}
	
	private Map<Long, Integer> indices(long[] documentIds) {
		Map<Long, Integer> output = new LinkedHashMap<Long, Integer>();
		for (int i = 0 ; i<documentIds.length ; i++) output.put(documentIds[i], i);
		return output;
	}
	
	private AdjacencyMatrix buildAdjacencyMatrix(Map<Long, Double> results, Map<Long, Integer> indices) {
		AdjacencyMatrix matrix = new AdjacencyMatrixImpl(results.size());
		int i=0;
		for (Long result : results.keySet()) {
			for (WebURL url : index.getDocument(result).getMetadata().getLinks()) {
				Document document = index.getDocumentByUrl(url);
				if (document == null) continue;
				long child = document.getMetadata().getDocID();
				Integer j = indices.get(child);
				if (j == null) continue;
				matrix.set(i, j, matrix.get(i, j) + 1);
			}
			
		}
		return matrix;
	}
}