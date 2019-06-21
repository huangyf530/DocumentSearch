import java.io.*;
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


public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private double averageLength=1.0f;
    private long docnum;
	private SimpleDateFormat dateFormat;

    public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer4Lucene7(true);
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    		Directory dir = FSDirectory.open(Paths.get(indexDir));
    		iwc.setSimilarity(new BM25Similarity());
    		indexWriter = new IndexWriter(dir,iwc);
    		docnum = 0;
			dateFormat = new SimpleDateFormat("HH:mm:ss");
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
			String content = null;
			String title = doc.title();
			org.jsoup.nodes.Element head1 = doc.select("h1").first();
			if(head1 != null && !head1.text().contains(title)) {
				title = title + "——" + head1.text();
			}
			doc.select("header").remove();    // delete header
			doc.select("nav").remove();       // delete navigate information
			doc.select("footer").remove();    // delete footer
			org.jsoup.nodes.Element content2 = doc.select("content").first();
			if(content2 != null) {
				content = content2.text();
				System.out.println("content: " + content2.text());
			}
			else{
				StringBuilder builder = new StringBuilder();
				org.jsoup.nodes.Element body = doc.body();
				int k = 0;
				for(org.jsoup.nodes.Element temp : body.children()){
					if(temp.hasClass("header") || temp.hasClass("footer")){
						continue;
					}
					builder.append(temp.text() + " ");
//					System.out.println(k + " " + temp.text());
//					System.out.println(temp.attributes());
//					k ++;
				}
				content = builder.toString();
			}
//			System.out.println("Title is: " + title);
//			System.out.println("Body is:" + content);
//			org.jsoup.select.Elements links = doc.select("li");
//			int linknum = 0;
//			for (org.jsoup.nodes.Element link : links) {
//				System.out.println(linknum + ": " + link.text() + link.attr("href"));
//				linknum++;
//			}
//			System.out.println("links number is " + linknum);
			addDocument(content, title, url, "html");
		}
		catch (Exception e){
			e.printStackTrace();
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
			content = delCharSymbol(content);
			addDocument(content, title, url, "pdf");
//			System.out.println(title);
//			System.out.println(content);
//			System.out.println(url);
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
			content = delCharSymbol(content);
			addDocument(content, title, url, "docx");
//			System.out.println(title);
//			System.out.println(content);
//			System.out.println(url);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private String delCharSymbol(String str)
	{
		String temp = str.replace('\t',' ');
		temp = temp.replaceAll("\n|\r","");
		return temp;
	}

	private void addDocument(String content, String title, String url, String type) throws IOException{
		Document document  = new Document();
		Field contentField  = new TextField("content" ,content,Field.Store.YES);
		Field titleField = new TextField("title", title, Field.Store.YES);
		Field urlField = new StringField("url", url, Field.Store.YES);
		Field typeField = new StringField("type", type, Field.Store.YES);
		document.add(contentField);
		document.add(titleField);
		document.add(urlField);
		document.add(typeField);
		indexWriter.addDocument(document);
		averageLength += content.length();
		indexWriter.commit();
		docnum++;
		if(docnum % 500 == 0){
			System.out.println(getTime() + " Handle " + docnum + " files!");
		}
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
			long startTime = new Date().getTime();
			System.out.println(getTime() + ": begin create index from directory " + dataDir.getCanonicalPath());
			for (int i = 0; i < dataFiles.length; i++) {
				readFile(filename + "/" + dataFiles[i].getName(), dataFiles[i].getName());
			}
			indexWriter.close();
			long endTime = new Date().getTime();
			System.out.println(getTime() + " It takes " + (endTime - startTime) / 1000
					+ " seconds to create index for "+ docnum + " files in directory "
					+ dataDir.getCanonicalPath());
			averageLength = averageLength / docnum;
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	static {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

	public static void main(String[] args) {
//		ImageIndexer indexer=new ImageIndexer("/Users/huangyf/Dataset/SearchEngine/apache-tomcat-9.0.21/bin/forIndex/index");
		ImageIndexer indexer=new ImageIndexer("./forIndex/index");
//		indexer.indexFromDir("/Users/huangyf/Dataset/SearchEngine/test/web1");
		//indexer.indexSpecialFile("input/sogou-utf8.xml");
//		indexer.saveGlobals("/Users/huangyf/Dataset/SearchEngine/apache-tomcat-9.0.21/bin/forIndex/global.txt");

//		indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/(JOB)application_form.doc","");
//		indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/1.docx","");
//		indexer.indexPdfFile("/Users/gengwei/Desktop/校园搜索引擎（python）/cjjzgd.pdf","");
		indexer.indexHtmlFile("input/test.html", "");
	}
}
