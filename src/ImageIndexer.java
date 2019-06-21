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
import java.util.*;


public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private double averageLength=1.0f;
    
    public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer4Lucene7(true);
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    		Directory dir = FSDirectory.open(Paths.get(indexDir));
    		iwc.setSimilarity(new BM25Similarity());
    		indexWriter = new IndexWriter(dir,iwc);
    		// indexWriter.setSimilarity(new SimpleSimilarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    
    public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(averageLength);
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
		System.out.println(url);
		try{
			org.jsoup.nodes.Document doc = Jsoup.parse(new File(filename), "utf-8");
//			System.out.println("Title is:"+doc.title());
//			System.out.println("Body is:"+doc.body().text());
			String title = doc.title();
			String content = doc.body().text();
			Document document  = new Document();
			Field contentField  = new TextField("content" ,content,Field.Store.YES);
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field urlField = new StringField("url", url, Field.Store.YES);
			document.add(contentField);
			document.add(titleField);
			document.add(urlField);
			indexWriter.addDocument(document);
			averageLength += content.length();
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
	public void indexPdfFile(String filename, String url)
	{
		String ans;
		try {
			File file = new File(filename);

			PDFParser pdfParser = new PDFParser(new RandomAccessFile(file, "r"));
			pdfParser.parse();
			PDDocument pdDocument = pdfParser.getPDDocument();
			ans = new PDFTextStripper().getText(pdDocument);
			pdDocument.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("file error");
			ans = "file not found.";
		}
		catch(IOException e)
		{
			ans = "file open error.";
		}

		System.out.println(delCharSymbol(ans));
	}

	/**
	 * <p>
	 * docxfile
	 *
	 */
	public void indexdocxFile(String filename, String url) {
		String ans;

		try{
			InputStream is = new FileInputStream(filename);

			if(filename.endsWith(".doc") || filename.endsWith(".DOC"))
			{
				WordExtractor extractor;
				extractor = new WordExtractor(is);
				ans = extractor.getText();
			}
			else if(filename.endsWith(".docx") || filename.endsWith(".DOCX"))
			{
				XWPFDocument doc = new XWPFDocument(is);
				XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
				ans = extractor.getText();
			}
			else
			{
				ans = "file name error.";
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println("file error");
			ans = "file not found.";
		}
		catch(IOException e)
		{
			ans = "file open error.";
		}

		System.out.println(delCharSymbol(ans));
	}

	private String delCharSymbol(String str)
	{
		String temp = str.replace('\t',' ');
		temp = temp.replaceAll("\n|\r","");
		return temp;
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
			if(dataDir.getName().toLowerCase().endsWith(".html")){
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
			for (int i = 0; i < dataFiles.length; i++) {
				readFile(filename + "/" + dataFiles[i].getName(), dataFiles[i].getName());
			}
			indexWriter.close();
			long endTime = new Date().getTime();
			System.out.println("It takes " + (endTime - startTime)
					+ " milliseconds to create index for the files in directory "
					+ dataDir.getPath());
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		ImageIndexer indexer=new ImageIndexer("forIndex/index");
		indexer.indexFromDir("input");
		//indexer.indexSpecialFile("input/sogou-utf8.xml");
		//indexer.saveGlobals("forIndex/global.txt");


		//indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/(JOB)application_form.doc");
		//indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/1.docx");
		//indexer.indexPdfFile("/Users/gengwei/Desktop/校园搜索引擎（python）/cjjzgd.pdf");
//		indexer.indexHtmlFile("input/index.html");
//		indexer.indexSpecialFile("input/sogou-utf8.xml");
//		indexer.saveGlobals("forIndex/global.txt");
	}
}
