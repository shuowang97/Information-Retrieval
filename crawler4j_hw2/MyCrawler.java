package csci572_hw2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(html|pdf|doc"
			 + "|jpg|jpeg|tiff|png|gif|psd|raw|eps|svg|bmp|ico))$");
	private static boolean FIRST_TIME = true;
	
    private static int totalUrls = 0;
    private static int successFetches = 0;
    private static int failFetches = 0;
	
	private static File UrlCsv = new File("C:\\Developer\\csci572\\hw2-result\\urls_latimes.csv");
	private static File FetchCsv = new File("C:\\Developer\\csci572\\hw2-result\\fetch_latimes.csv");
	private static File VisitCsv = new File("C:\\Developer\\csci572\\hw2-result\\visit_latimes.csv");
	private static File resultTxt = new File("C:\\Developer\\csci572\\hw2-result\\CrawlReport_latimes.txt");
    
	private static BufferedWriter writeText;
	private static BufferedWriter writeFetch;
	private static BufferedWriter writeVisit;
	private static BufferedWriter writeResult;
	
	private static ConcurrentHashMap<Integer, Integer> failCountMap = new ConcurrentHashMap<Integer, Integer>();
	private static ConcurrentHashMap<Integer, String> codeDescriptionMap = new ConcurrentHashMap<Integer, String>();
	private static ConcurrentHashMap<String, Integer> contentCountMap = new ConcurrentHashMap<String, Integer>();
	
	private static CopyOnWriteArraySet<String> uniqueInsideSet = new CopyOnWriteArraySet<String>();
	private static CopyOnWriteArraySet<String> uniqueOutsideSet = new CopyOnWriteArraySet<String>();

	private static int[] sizeCount = new int[5];
	
    @Override
    public void onStart() {
    	
    	if(!FIRST_TIME) return;
    	FIRST_TIME = false;
    	    	
		try {
			writeText = new BufferedWriter(new FileWriter(UrlCsv, true));
			writeFetch = new BufferedWriter(new FileWriter(FetchCsv, true));
			writeVisit = new BufferedWriter(new FileWriter(VisitCsv, true));
			writeResult = new BufferedWriter(new FileWriter(resultTxt, true));
			
			writeText.write("URL, Resides in LAtimes");
			writeText.flush();
			writeFetch.write("URL, Status Code");
			writeFetch.flush();
			writeVisit.write("URL, Size, Outgoing Links, Content Type");
			writeVisit.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
	
	 /**
	 * This method receives two parameters. The first parameter is the page
	 * in which we have discovered this new url and the second parameter is
	 * the new url. You should implement this function to specify whether
	 * the given url should be crawled or not (based on your crawling logic).
	 * In this example, we are instructing the crawler to ignore urls that
	 * have css, js, git, ... extensions and to only accept urls that start
	 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	 * referringPage parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		
		String href = url.getURL().toLowerCase();
		href = href.replaceAll(",", "-");
		boolean ret = false;
		
		try {
			
			if(!LATime(href)) {
				uniqueOutsideSet.add(href);
				String content = "" + href + "," + "N_OK";
				SynchronizedWrite(writeText, content);
				return false;
			}
			uniqueInsideSet.add(href);
			String content = "" + href + "," + "OK";
			SynchronizedWrite(writeText, content);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		if(!FILTERS.matcher(href).matches() && LATime(href)) {
			String subUrl = href.substring(href.lastIndexOf("/") + 1);
			// no extensions
			if(!subUrl.contains(".")) {
				ret = true;
			};
		}
		if(FILTERS.matcher(href).matches() && LATime(href)) {
			ret = true;
		}

		return ret;
	}
	 
	private boolean LATime(String href) {
		return href.startsWith("https://www.latimes.com") || href.startsWith("http://www.latimes.com");
	}

	  /**
	   * This function is called once the header of a page is fetched. It can be
	   * overridden by sub-classes to perform custom logic for different status
	   * codes. For example, 404 pages can be logged, etc.
	   *
	   * @param webUrl WebUrl containing the statusCode
	   * @param statusCode Html Status Code number
	   * @param statusDescription Html Status COde description
	   */
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		
		failCountMap.put(statusCode, failCountMap.getOrDefault(statusCode, 0) + 1);
		codeDescriptionMap.putIfAbsent(statusCode, statusDescription);
		if(statusCode >= 200 && statusCode < 300) {
			successFetches++;
		}else {
			failFetches++;
		}
		String url = webUrl.getURL();
		url.replaceAll(",", "-");

		try {
			String content = "" + url + "," + statusCode;
			SynchronizedWrite(writeFetch, content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	  * This function is called when a page is fetched and ready
	  * to be processed by your program.
	  */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		
		Set<WebURL> links = new HashSet<WebURL>();
		String html = "";
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			html = htmlParseData.getHtml();
			links = htmlParseData.getOutgoingUrls();
		}else if(page.getParseData() instanceof BinaryParseData) {
			BinaryParseData binaryParseData = (BinaryParseData) page.getParseData();
			html = binaryParseData.getHtml();
			links = binaryParseData.getOutgoingUrls();
		}
		
		totalUrls += links.size();
		
		//KB
		float size = html.length() / 1024;
		if(size < 1) {
			sizeCount[0]++;
		}else if(size < 10) {
			sizeCount[1]++;
		}else if(size < 100) {
			sizeCount[2]++;
		}else if(size < 1024) {
			sizeCount[3]++;
		}else {
			sizeCount[4]++;
		}
		
		String contentType = page.getContentType().split(";")[0];
		contentCountMap.put(contentType, contentCountMap.getOrDefault(contentType, 0) + 1);
		
		// only 200 status code will come here
		System.out.println("----------------------CheckContentType------------------");
		System.out.println(page.getContentType());
		try {
			String content = "" + url + "," + html.length() + "," + links.size() + "," + contentType;
			SynchronizedWrite(writeVisit, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onBeforeExit() {
		
		try {
			
			writeResult.write("Name: Shuo Wang\r\n");
			writeResult.write("USC ID: 5479469586\r\n");
			writeResult.write("News site crawled: https://www.latimes.com\r\n");
			writeResult.write("Number of threads: 7\r\n");
			
			writeResult.write("\r\n");
			writeResult.write("Fetch Statistics:\r\n");
			writeResult.write("================\r\n");
			writeResult.write("# fetches attemped: " + (successFetches + failFetches) + "\r\n");
			writeResult.write("# fetches succeeded: " + successFetches + "\r\n");
			writeResult.write("# fetches failed or aborted: " + failFetches + "\r\n");
			
			writeResult.write("\r\n");
			writeResult.write("Outgoing URLs:\r\n");
			writeResult.write("================\r\n");
			writeResult.write("Total URLs extracted: " + totalUrls + "\r\n");
			writeResult.write("# unique URLs extracted:" + (uniqueInsideSet.size() + uniqueOutsideSet.size()) + "\r\n");
			writeResult.write("# unique URLs within News Site: " + uniqueInsideSet.size() + "\r\n");
			writeResult.write("# unique URLs outside News Site: " + uniqueOutsideSet.size() + "\r\n");
			
			writeResult.write("\r\n");
			writeResult.write("Status Codes:\r\n");
			writeResult.write("================\r\n");			
			for(int code : failCountMap.keySet()) {
				writeResult.write(code + " " + codeDescriptionMap.get(code) + ": " + failCountMap.get(code) + "\r\n");
			}
			
			writeResult.write("\r\n");
			writeResult.write("File Sizes:\r\n");
			writeResult.write("================\r\n");
			writeResult.write("< 1KB: " + sizeCount[0] + "\r\n");
			writeResult.write("1KB ~ <10KB: " + sizeCount[1] + "\r\n");
			writeResult.write("10KB ~ <100KB: " + sizeCount[2] + "\r\n");
			writeResult.write("100KB ~ <1MB: " + sizeCount[3] + "\r\n");
			writeResult.write(">= 1MB: " + sizeCount[4] + "\r\n");
			
			writeResult.write("\r\n");
			writeResult.write("Content Types:\r\n");
			writeResult.write("================\r\n");
			for(String type : contentCountMap.keySet()) {
				writeResult.write(type + ": " + contentCountMap.get(type) + "\r\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(writeText != null) {
					writeText.close();
				}
				if(writeFetch != null) {					
					writeFetch.close();
				}
				if(writeVisit != null) {					
					writeVisit.close();
				}
				if(writeVisit != null) {					
					writeResult.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		

	}
	
	public synchronized void SynchronizedWrite(BufferedWriter writer, String content) throws IOException {
		writer.newLine();
		writer.write(content);
		writer.flush();
	}
	
	 
}
