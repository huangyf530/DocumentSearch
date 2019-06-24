import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.document.*;
import org.apache.lucene.search.similarities.BM25Similarity;

import org.w3c.dom.*;
import CNAnalyzer.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.*;

import javax.xml.parsers.*;

import java.io.FileInputStream;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.text.PDFTextStripper;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private double averageLength=1.0f;
    private long docnum;
    private long pdfnum;
    private long htmlnum;
    private long docxnum;
    private double time;
    private int rootlength;
	private SimpleDateFormat dateFormat;
	private float average_pagarank;
	private Map<String, Float> scores;
	Pattern p;

	public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer4Lucene7();
		scores = new HashMap<>();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    		Directory dir = FSDirectory.open(Paths.get(indexDir));
    		iwc.setSimilarity(new BM25Similarity());
    		indexWriter = new IndexWriter(dir,iwc);
    		docnum = 0;
    		pdfnum = 0;
    		docxnum = 0;
    		htmlnum = 0;
    		time = 0;
			dateFormat = new SimpleDateFormat("HH:mm:ss");
			p = Pattern.compile("(?<=charset=)(.+)(?=\")");
    		// indexWriter.setSimilarity(new SimpleSimilarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

    private String getTime(){
    	Date date = new Date();
		return dateFormat.format(date);
	}
    
    
    public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(averageLength);
    		pw.println(docnum);
    		pw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
	/** 
	 * <p>
	 * index sogou.xml 
	 * 
	 */
	public void indexSpecialFile(String filename){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
			DocumentBuilder db = dbf.newDocumentBuilder();    
			org.w3c.dom.Document doc = db.parse(new File(filename));
			NodeList nodeList = doc.getElementsByTagName("pic");
			for(int i=0;i<nodeList.getLength();i++){
				org.w3c.dom.Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				org.w3c.dom.Node locate=map.getNamedItem("locate");
				org.w3c.dom.Node bigClass=map.getNamedItem("bigClass");
				org.w3c.dom.Node smallClass=map.getNamedItem("smallClass");
				org.w3c.dom.Node query=map.getNamedItem("query");
				String absString=bigClass.getNodeValue()+" "+smallClass.getNodeValue()+" "+query.getNodeValue();
				Document document  =   new  Document();
				Field PicPathField  =   new StringField( "picPath" ,locate.getNodeValue(),Field.Store.YES);
				Field abstractField  =   new TextField("abstract" ,absString,Field.Store.YES);
				averageLength += absString.length();
				document.add(PicPathField);
				document.add(abstractField);
				indexWriter.addDocument(document);
				if(i%10000==0){
					System.out.println("process "+i);
				}
				//TODO: add other fields such as html title or html content
			}
//			averageLength /= indexWriter.numDocs();
//			System.out.println("average length = "+averageLength);
//			System.out.println("total "+indexWriter.numDocs()+" documents");
			indexWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * htmlfile
	 *
	 */
	private void indexHtmlFile(String filename, String url){
		try{
			org.jsoup.nodes.Document doc = Jsoup.parse(new File(filename), "utf-8");
			org.jsoup.nodes.Element eles = doc.select("meta[http-equiv=Content-Type]").first();
			if(eles != null) {
				Matcher m = p.matcher(eles.toString());
				if (m.find()) {
					try {
						doc = Jsoup.parse(new File(filename), m.group());
					} catch (UnsupportedEncodingException e) {
						doc = Jsoup.parse(new File(filename), "utf-8");
					}
				}
			}
			String content = null;
			String title = doc.title();
			org.jsoup.nodes.Element head1 = doc.select("h1").first();
			if(head1 != null && !head1.text().contains(title)) {
				title = title + "——" + head1.text();
			}
			doc.select("header").remove();    // delete header
			doc.select("nav").remove();       // delete navigate information
			doc.select("footer").remove();    // delete footer
			org.jsoup.nodes.Element body = doc.body();
			StringBuilder builder = new StringBuilder();
			int k = 0;
			for(org.jsoup.nodes.Element temp : body.children()){
				if(temp.hasClass("header") || temp.hasClass("footer")){
					continue;
				}
				builder.append(temp.text() + " ");
//					System.out.println(k + " " + temp.text());
//					System.out.println(temp.attributes());
				k ++;
			}
			content = builder.toString();
//			System.out.println(title);
//			System.out.println(content);
            Path temp = Paths.get(filename);
            filename = temp.subpath(rootlength, temp.getNameCount()).toString();
			addDocument(content, title, url, "html", scores.get(url) / average_pagarank, filename);
//			addDocument(content, title, url, "html", 1, filename);
			htmlnum ++;
		}
		catch (Exception e){
			System.out.println(getTime() + " " + scores.get(url) + " " + url);
			System.out.println(getTime() + " " + e.getMessage());
		}
	}

	/**
	 * <p>
	 * pdffile
	 *
	 */
	private void indexPdfFile(String filename, String url)
	{
		String title;
		String content;
		try {
			File file = new File(filename);


//			PDFParser pdfParser = new PDFParser(new RandomAccessFile(file, "r"));
//			pdfParser.parse();
//			PDDocument pdDocument = pdfParser.getPDDocument();
			PDDocument pdDocument = PDDocument.load(file);

			String all = new PDFTextStripper().getText(pdDocument);
			pdDocument.close();
			all = all.trim();
			int breakpoint = all.indexOf("\n");

			while(breakpoint==0)
			{
				all = all.substring(1);
				all = all.trim();
				breakpoint = all.indexOf("\n");
			}

			if(breakpoint < 0)
			{
				title = "";
				content = all;
			}
			else {
				title = all.substring(0, breakpoint);
				content = all.substring(breakpoint + 1);
			}
			title = delCharSymbol(title);
//			content = delCharSymbol(content);
            Path temp = Paths.get(filename);
            filename = temp.subpath(rootlength, temp.getNameCount()).toString();
			addDocument(content, title, url, "pdf", 1.0f, filename);
			pdfnum++;
//			System.out.println(title);
//			System.out.println(content);
//			System.out.println(url);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * <p>
	 * docxfile
	 *
	 */
	public void indexdocxFile(String filename, String url) {
		String title;
		String content;

		try{
			InputStream is = new FileInputStream(filename);
			String all;

			if(filename.endsWith(".doc") || filename.endsWith(".DOC"))
			{
				WordExtractor extractor;
				extractor = new WordExtractor(is);
				all = extractor.getText();
			}
			else if(filename.endsWith(".docx") || filename.endsWith(".DOCX"))
			{
				XWPFDocument doc = new XWPFDocument(is);
				XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
				all = extractor.getText();
			}
			else
			{
				all = "file type error.";
			}

			all = all.trim();
			int breakpoint = all.indexOf("\n");
			while(breakpoint==0)
			{
				all = all.substring(1);
				all = all.trim();
				breakpoint = all.indexOf("\n");
			}

			if(breakpoint < 0)
			{
				title = "";
				content = all;
			}
			else {
				title = all.substring(0, breakpoint);
				content = all.substring(breakpoint + 1);
			}
			title = delCharSymbol(title);
//			content = delCharSymbol(content);
            Path temp = Paths.get(filename);
            filename = temp.subpath(rootlength, temp.getNameCount()).toString();
			addDocument(content, title, url, "docx", 1.0f, filename);
			docxnum ++;
//			System.out.println(title);
//			System.out.println(content);
//			System.out.println(url);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private String delCharSymbol(String str)
	{
		String temp = str.replace('\t',' ');
		temp = temp.replaceAll("\n|\r","");
		return temp;
	}

	private void addDocument(String content, String title, String url, String type, float boost, String path) throws IOException{
		Document document  = new Document();
		Field contentField  = new TextField("content" ,content,Field.Store.YES);
		Field titleField = new TextField("title", title, Field.Store.YES);
		Field urlField = new StringField("url", url, Field.Store.YES);
		Field typeField = new StringField("type", type, Field.Store.YES);
		Field scoreField = new FeatureField("features", "pagerank", boost);
//		Field scoreField = new FloatDocValuesField("pagerank", boost);
		Field pathField = new StringField("path", path, Field.Store.YES);
		document.add(contentField);
		document.add(titleField);
		document.add(urlField);
		document.add(typeField);
		document.add(scoreField);
		document.add(pathField);
		indexWriter.addDocument(document);
		averageLength += content.length();
//		indexWriter.commit();
		if(docnum % 2000 == 0){
			System.out.println(getTime() + " Handle " + docnum + " files! HTML: " + htmlnum + "  PDF: " + pdfnum + "  DOCX: " + docxnum);
		}
		docnum++;
	}

	private void readFile(String filename, String url){
		File dataDir  = new File(filename);
		if(!dataDir.isDirectory()){
			if(dataDir.getName().toLowerCase().endsWith(".pdf")){
				indexPdfFile(filename, url);
			}
			if(dataDir.getName().toLowerCase().endsWith(".docx") || dataDir.getName().toLowerCase().endsWith(".doc")){
				indexdocxFile(filename, url);
			}
			if(dataDir.getName().toLowerCase().endsWith(".html") || dataDir.getName().toLowerCase().endsWith(".htm")){
				indexHtmlFile(filename, url);
			}
			return;
		}
		File[] dataFiles = dataDir.listFiles();
		if(dataFiles == null){
			System.out.println(filename + "is empty!");
			return;
		}
		for(int i = 0; i < dataFiles.length; i++){
			readFile(filename + "/" + dataFiles[i].getName(), url + "/" + dataFiles[i].getName());
		}
	}

	private void loadPageRank(String filename){
		int urlnum = 0;
		float totalscore = 0f;
		String line;
		System.out.println(getTime() + " Begin Load Page Rank Score from " + filename);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while ((line=reader.readLine()) != null) {
				if(line.equals("")){
					continue;
				}
				String[] contents = line.split("\t");
				try {
					totalscore += Double.parseDouble(contents[1]);
					scores.put(contents[0], Float.parseFloat(contents[1]));
					urlnum += 1;
				}
				catch (ArrayIndexOutOfBoundsException e){
					System.out.println(line + ": out of array index");
					//e.printStackTrace();
				}
				catch (NumberFormatException e){
					System.out.println(line + ": number format wrong");
					// e.printStackTrace();
				}
			}
			System.out.print(getTime() + " Load Page Rank Score Over " + filename);
			System.out.println("  average score = " + (totalscore / urlnum));
			average_pagarank = totalscore / urlnum;
			reader.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	private void indexFromDir(String filename){
		try {
			File dataDir = new File(filename);
			if (!dataDir.isDirectory()) {
				System.out.println("ERROR: " + filename + " should be a directory!");
				return;
			}
			File[] dataFiles = dataDir.listFiles();
			if (dataFiles == null) {
				System.out.println("ERROR: " + filename + "is empty!");
				return;
			}
			rootlength = Paths.get(dataDir.getCanonicalPath()).getNameCount() - 2;
			long startTime = new Date().getTime();
			System.out.println(getTime() + ": begin create index from directory " + dataDir.getCanonicalPath());
			for (int i = 0; i < dataFiles.length; i++) {
				readFile(filename + "/" + dataFiles[i].getName(), dataFiles[i].getName());
			}
			long endTime = new Date().getTime();
			time += (double)(endTime - startTime) / 1000;
			System.out.println(getTime() + " It takes " + time
					+ " seconds to create index for "+ docnum + " files " + "HTML: " + htmlnum + "  PDF: " +
					pdfnum + "  DOCX: " + docxnum);
			averageLength = averageLength / docnum;
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private void writeOver() throws IOException{
		indexWriter.commit();
		indexWriter.close();
	}

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

	public static void main(String[] args) {
		String indexpath = "/Users/huangyf/Dataset/SearchEngine/apache-tomcat-9.0.21/bin/forIndex2";
		String datapath = "/Users/huangyf/Dataset/SearchEngine/Big";
		String pagerankpath = "forIndex/pagerank.txt";
		ImageIndexer indexer=new ImageIndexer(indexpath + "/index");
		indexer.loadPageRank(pagerankpath);
		try {
			File rootdir = new File(datapath);
			File[] files = rootdir.listFiles();
			for (File temp : files) {
				indexer.indexFromDir(temp.getCanonicalPath());
			}
			indexer.saveGlobals(indexpath + "/global.txt");
			indexer.writeOver();
		}
		catch (IOException e){
			e.printStackTrace();
		}
//
//		indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/(JOB)application_form.doc","");
//		indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/1.docx","");
//		indexer.indexPdfFile("/Users/gengwei/Desktop/校园搜索引擎（python）/cjjzgd.pdf","");
//		long startTime = new Date().getTime();
//		for(int i = 0; i < 1000; i++) {
//			indexer.indexHtmlFile("/Users/huangyf/Desktop/大三春季学期/搜索引擎技术基础/Homework/3BigHomework/ImageSearch/input/shimin.htm", "");
//		}
//		long endTime = new Date().getTime();
//		System.out.println(" It takes " + (endTime - startTime) / 1000
//				+ " seconds");
	}
}
