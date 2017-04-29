package ch.heigvd.wem.linkanalysis;

import java.util.Map;

import ch.heigvd.wem.interfaces.Index;

public interface RankingStrategy {
	void setIndex(Index index);
	Map<Long, Double> rank(Map<Long, Double> results);
}
