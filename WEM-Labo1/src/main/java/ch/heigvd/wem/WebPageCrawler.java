package ch.heigvd.wem;

import java.util.LinkedList;
import java.util.List;

import ch.heigvd.wem.data.Metadata;
import ch.heigvd.wem.data.VisitedPage;
import ch.heigvd.wem.labo1.Labo1;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class WebPageCrawler extends WebCrawler {
	
	public static List<String> excludedExtensions;
	
	private static WebURL initialWebURL;
	private static Object initialWebURLMonitor = new Object();
	
	static {
		excludedExtensions = new LinkedList<String>();
		excludedExtensions.add("png");
		excludedExtensions.add("jpg");
		excludedExtensions.add("gif");
		excludedExtensions.add("bmp");
		excludedExtensions.add("pdf");
		System.out.println("Satic initializer done");
	}

	private static WebPageIndexerQueue indexer = null;

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		/* A IMPLEMENTER */
		
		if(Labo1.DEBUG) {
			System.out.println("shouldVisit called");
			System.out.println("Referring page: " + referringPage.getWebURL());
			System.out.println("Url: " + url);
		}
		
		if (excludedExtensions.contains(getextension(url))) {
			if (Labo1.DEBUG) System.out.println("skipping url: "+url);
			return false;
		}
		
		synchronized(initialWebURLMonitor) {if (initialWebURL == null) {
			initialWebURL = referringPage == null ? url : referringPage.getWebURL();
			System.err.println("set initial url to " + initialWebURL);
		}}
		
		//get pages from the same fully qualified domain name only (e.g en.wikipedia.org)
		if (!(initialWebURL.getDomain().equals(url.getDomain()) &&
				(initialWebURL.getSubDomain().equals(url.getSubDomain())))) {
			return false;
		}
		
		return true;
	}

	private String getextension(WebURL url) {
		String[] parts = url.getPath().split("/");
		if (parts.length < 1) return "";
		String extension = parts[parts.length - 1];
		if (extension.length() < 3) return "";
		extension = extension.substring(extension.length() -3, extension.length());
		return extension;
	}
	
	@Override
	public void visit(Page page) {
		
		if(indexer == null) {
			throw new IllegalStateException("WebPageSaver doesn't contain a WebPageIndexer, you must set it in Labo1.java before starting the crawling");
		}

		Metadata metadata = new Metadata();
		String content = null;
		
		ParseData data = page.getParseData();
		if(data instanceof HtmlParseData) {
			HtmlParseData htmlData = (HtmlParseData) data;
			metadata.setTitle(htmlData.getTitle());
			metadata.setUrl(page.getWebURL());
			metadata.setLinks(htmlData.getOutgoingUrls());
			content = htmlData.getText();
		}

		//we queue the page for indexer
		VisitedPage visitedPage = new VisitedPage(metadata, content);
		indexer.queueVisitedPage(visitedPage);
	}

	public static void setIndexerQueue(WebPageIndexerQueue indexer) { WebPageCrawler.indexer = indexer; }
	public static WebPageIndexerQueue getIndexerQueue() { return indexer; }
	
}
