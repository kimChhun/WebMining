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
	
	public static List<String> imageExtensions;
	
	static {
		imageExtensions = new LinkedList<String>();
		imageExtensions.add("png");
		imageExtensions.add("jpg");
		imageExtensions.add("gif");
		imageExtensions.add("bmp");
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
		
		if (imageExtensions.contains(getextension(url))) {
			if (Labo1.DEBUG || true) System.out.println("skipping url: "+url);
			return false;
		}
		return true;
	}

	private String getextension(WebURL url) {
		String[] parts = url.getPath().split("/");
		String extension = parts[parts.length - 1];
		if (extension.length() < 3) return "";
		extension = extension.substring(extension.length() -3, extension.length());
		System.out.println("url part : "+ extension);
		return extension;
	}
	
	@Override
	public void visit(Page page) {
		
		if(indexer == null) {
			System.err.println("WebPageSaver doesn't contains a WebPageIndexer, you must set it in Labo1.java before starting the crawling");
			System.exit(1);
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
