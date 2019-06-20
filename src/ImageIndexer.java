import java.io.*;
import java.nio.file.Paths;
import java.util.*;


import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.similarities.BM25Similarity;

import org.w3c.dom.*;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.xml.parsers.*;

import java.io.FileInputStream;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.text.PDFTextStripper;


public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float averageLength=1.0f;
    
    public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
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
				Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				Node locate=map.getNamedItem("locate");
				Node bigClass=map.getNamedItem("bigClass");
				Node smallClass=map.getNamedItem("smallClass");
				Node query=map.getNamedItem("query");
				String absString=bigClass.getNodeValue()+" "+smallClass.getNodeValue()+" "+query.getNodeValue();
//				Document document  =   new  Document();
//				Field PicPathField  =   new StringField( "picPath" ,locate.getNodeValue(),Field.Store.YES);
//				Field abstractField  =   new TextField("abstract" ,absString,Field.Store.YES);
//				averageLength += absString.length();
//				document.add(PicPathField);
//				document.add(abstractField);
//				indexWriter.addDocument(document);
//				if(i%10000==0){
//					System.out.println("process "+i);
//				}
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
	 * pdffile
	 *
	 */
	public void indexPdfFile(String filename)
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
	public void indexdocxFile(String filename) {
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

	public String delCharSymbol(String str)
	{
		String temp = str.replace('\t',' ');
		temp = temp.replaceAll("\n|\r","");
		return temp;
	}

	public static void main(String[] args) {
		ImageIndexer indexer=new ImageIndexer("forIndex/index");
		//indexer.indexSpecialFile("input/sogou-utf8.xml");
		//indexer.saveGlobals("forIndex/global.txt");


		//indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/(JOB)application_form.doc");
		//indexer.indexdocxFile("/Users/gengwei/Desktop/校园搜索引擎（python）/1.docx");
		//indexer.indexPdfFile("/Users/gengwei/Desktop/校园搜索引擎（python）/cjjzgd.pdf");

	}
}
