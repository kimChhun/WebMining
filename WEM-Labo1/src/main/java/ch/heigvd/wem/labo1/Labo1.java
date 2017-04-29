package ch.heigvd.wem.labo1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.heigvd.wem.WebPageIndexerQueue;
import ch.heigvd.wem.data.Document;
import ch.heigvd.wem.WebPageCrawler;
import ch.heigvd.wem.interfaces.Index;
import ch.heigvd.wem.interfaces.Indexer;
import ch.heigvd.wem.interfaces.Retriever;
import ch.heigvd.wem.interfaces.Retriever.WeightingType;
import ch.heigvd.wem.linkanalysis.RankingStrategy;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Labo1 {

	private enum Mode {
		CRAWL,
		RESTORE,
	}
	
	// CONFIGURATION
	public static final String  START_URL 			= "https://en.wikipedia.org/wiki/Web_mining"; //"http://iict.heig-vd.ch";
	public static final boolean DEBUG				= false;
	private static final Mode	mode				= Mode.RESTORE;
	private static final String	indexSaveFileName	= "index.bin";
	public static final double	MIN_SIMILARITY		= 0.0d; // the minimum similarity to display in result list. set to 0 to show all matches.
	public static final int		MAX_RESULTS			= 20; //maximum  number of results to display
	private static final int	EXCERPT_LENGTH		= 120; //the number of characters to display per result
	public static final Set<String> COMMON_WORDS = new HashSet<String>();
	
	public static Class<? extends RankingStrategy> rankingStrategy = null;
	
	static {
		File commonWordsFile = new File(
				Labo1.class.getClassLoader().getResource("common_words").getPath().replaceAll("%20", " "));
		try {
			loadCommonWordsFile(commonWordsFile);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load common words file", e);
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {

		Index index = null;
		
		switch(mode) {
		case CRAWL:
			//we crawl and save the index to disk
			index = crawl(null);
			saveIndex(indexSaveFileName, index);
			break;
			
		case RESTORE:
			//we load the index from disk
			index = loadIndex(indexSaveFileName);
			break;
		}
		
		//initialize retriever and execute query
		Retriever retriever = new RetrieverImpl(index, WeightingType.TF_IDF);
		
		Map<Long, Double> results = retriever.executeQuery(String.join(" ", args));
		
		if (rankingStrategy != null) {
			RankingStrategy strategy = rankingStrategy.newInstance();
			strategy.setIndex(index);
			results = strategy.rank(results);
		}
		
		int i = 1;
		for (Map.Entry<Long, Double>  result : results.entrySet()) {
			Double score = result.getValue();
			Document document = index.getDocument(result.getKey());
			if (score < MIN_SIMILARITY) break;
			if (i > MAX_RESULTS) break;
			printDocument(document, score, args, i);
			i++;
		}
		
	}
	
	private static void printDocument(Document document, double score, String[] terms, int rank) {
		//print the result title, text excerpt, and url
		String content = document.getContent();
		WebURL url = document.getMetadata().getUrl();
		System.out.print(String.format("%s) %s (score: %f)\n%s\n%s\n%s\n",
				rank,	//counter
				document.getMetadata().getTitle(),	//title
				score, //cosine score
				suggestExcerpt(content, terms, EXCERPT_LENGTH),	//excerpt
				url,	//url
				repeat("_", url.toString().length())));
	}

	/**
	 * Suggest a result description for the given document, relative to the terms
	 * in the search query. This implementation uses the first sentence containing
	 * any of the terms
	 * @param content
	 * @param terms
	 * @return
	 */
	private static String suggestExcerpt(String content, String[] terms, int length) {
		content = content.replaceAll("\\s+", " ");
		int excerptOffset = 0;
		boolean found = false;
		for (int j=0 ; !found && j < terms.length ; j++) {
			int idx = content.indexOf(terms[j]);
			if (idx > 0) {
				found = true;
				excerptOffset = idx;
			}
		}
		int sentenceOffset = 0, pastOffset = 0;
		while (sentenceOffset >= 0 && sentenceOffset < excerptOffset) {
			pastOffset = sentenceOffset;
			sentenceOffset = content.indexOf(".", sentenceOffset + 1);
		}
		sentenceOffset = pastOffset;
		
		if ((excerptOffset - sentenceOffset) < length) {
			excerptOffset = sentenceOffset;
			while (Arrays.<Character>asList(new Character[] {'.', ' '}).contains(content.charAt(excerptOffset))) {
				excerptOffset++;
			}
		}
		
		return content.substring(excerptOffset, excerptOffset + Math.min(length, content.length() - excerptOffset));
	}
	
	private static Index crawl(Index index) {
		// CONFIGURATION
		CrawlConfig config = new CrawlConfig();
		config.setMaxConnectionsPerHost(8);		//maximum 10 for tests
		config.setConnectionTimeout(4000); 			//4 sec.
		config.setSocketTimeout(5000);				//5 sec.
		config.setCrawlStorageFolder("temp");
		config.setIncludeHttpsPages(true);
		config.setPolitenessDelay(500); 			//minimum 250ms for tests
		config.setUserAgentString("crawler4j/WEM/2017");
		config.setMaxDepthOfCrawling(2);			//max 2-3 levels for tests on large website
		config.setMaxPagesToFetch(100);			//-1 for unlimited number of pages
		
		RobotstxtConfig robotsConfig = new RobotstxtConfig(); //by default
		
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotsConfig, pageFetcher);
        
		//we create the indexer and the indexerQueue
		Indexer indexer = new IndexerImpl();
		WebPageIndexerQueue queue = new WebPageIndexerQueue(indexer);
		queue.start();
		//we set the indexerQueue reference to all the crawler threads
		WebPageCrawler.setIndexerQueue(queue);
		
		try {
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.addSeed(START_URL);
			controller.start(WebPageCrawler.class, 20); //this method keep the hand until the crawl is done
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		queue.setAllDone(); //we notify the indexerQueue that it will not receive more data
		try {
			queue.join(); //we wait for the indexerQueue to finish
		} catch (InterruptedException e) { /* NOTHING TO DO */ }
		
		//we return the created index
		return indexer.getIndex();
	}
	
	private static void saveIndex(String filename, Index index) {
		try {
			File outputFile = new File("save", filename);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFile));
			out.writeObject(index);
			out.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Index loadIndex(String filename) {
		try {
			File inputFile = new File("save", filename);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(inputFile));
			Object o = in.readObject();
			in.close();
			if(o instanceof Index) {
				return (Index) o;
			} else {
				throw new IllegalStateException(filename + " is not a valid index file!");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * repeat a string
	 * @param string
	 * @param times
	 * @return {@code string} repeated {@code times} times
	 */
	private static String repeat(String string, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i=0 ; i < times ; i++) sb.append(string);
		return sb.toString();
	}
	
	private static void loadCommonWordsFile(File file) throws IOException{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				COMMON_WORDS.add(line.toLowerCase());
			} 
		} finally {
			if (reader != null) reader.close();
		}
	}
}
