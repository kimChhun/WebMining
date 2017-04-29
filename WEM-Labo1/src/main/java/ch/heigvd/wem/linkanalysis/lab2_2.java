package ch.heigvd.wem.linkanalysis;

import ch.heigvd.wem.labo1.Labo1;

public class lab2_2 {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		/**
		 * Run Labo1 (search query) with the {@link PageRank} ranking strategy, which calculates
		 * pagerank for each results and sorts the results by descending score (similarity and pagerank)
		 */
		Labo1.rankingStrategy = PageRank.class;
		Labo1.main(args);
	}
}
