import java.io.*;
import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import CNAnalyzer.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.*;
import org.jsoup.*;

import javax.xml.parsers.*; 

public class ImageIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private double averageLength=1.0f;
    
    public ImageIndexer(String indexDir){
    	analyzer = new IKAnalyzer4Lucene7();
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
	private void indexHtmlFile(String filename){
		try{
			org.jsoup.nodes.Document doc = Jsoup.parse(new File(filename), "utf-8");
//			System.out.println("Title is:"+doc.title());
//			System.out.println("Body is:"+doc.body().text());
			String title = doc.title();
			String content = doc.body().text();
			Document document  = new Document();
			Field contentField  = new TextField("content" ,content,Field.Store.YES);
			Field titleField = new TextField("title", title, Field.Store.YES);
			document.add(contentField);
			document.add(titleField);
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
	public void indexPdfFile(String filename){

	}

	/**
	 * <p>
	 * docxfile
	 *
	 */
	public void indexdocxFile(String filename){

	}
	public static void main(String[] args) {
		ImageIndexer indexer=new ImageIndexer("forIndex/index");
		indexer.indexHtmlFile("input/index.html");
//		indexer.indexSpecialFile("input/sogou-utf8.xml");
//		indexer.saveGlobals("forIndex/global.txt");
	}a
}
