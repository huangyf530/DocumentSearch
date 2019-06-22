import java.io.*;
import java.io.IOException;
import java.nio.file.Paths;

import CNAnalyzer.IKAnalyzer4Lucene7;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Field;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class ImageSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private float avgLength=1.0f;
	
	public ImageSearcher(String indexdir){
		analyzer = new IKAnalyzer4Lucene7(true);
		try{
			Directory dir = FSDirectory.open(Paths.get(indexdir));
			reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString,String field1, String field2,int maxnum, String[] content){
		try {
			StringReader stringReader = new StringReader(queryString);
			TokenStream tokenStream = analyzer.tokenStream("", stringReader);
			CharTermAttribute attribute = tokenStream.getAttribute(CharTermAttribute.class);
			//final query
			BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
			tokenStream.reset();
			System.out.print("分词结果：");
			while(tokenStream.incrementToken()){
				String tmpString = attribute.toString();
				System.out.print(tmpString + " ");
				Term term1 = new Term(field1, tmpString);
				Term term2 = new Term(field2, tmpString);
				Query query1 = new TermQuery(term1);
				Query query2 = new TermQuery(term2);
				finalQueryBuilder.add(new BoostQuery(query1, 2.0f), BooleanClause.Occur.SHOULD);
				finalQueryBuilder.add(new BoostQuery(query2, 0.5f), BooleanClause.Occur.SHOULD);
			}
			System.out.println();
			tokenStream.close();
			finalQueryBuilder.setMinimumNumberShouldMatch(1);
			BooleanQuery finalQuery = finalQueryBuilder.build();
			Query pagerank = FeatureField.newSaturationQuery("features", "pagerank");
			Query boostedQuery = new BooleanQuery.Builder()
					.add(finalQuery, BooleanClause.Occur.MUST)
					.add(new BoostQuery(pagerank, 10f), BooleanClause.Occur.SHOULD)
					.build();
			TopDocs results = searcher.search(boostedQuery, maxnum);
			if(results != null) {
				highLightDisplay(results, boostedQuery, content);
				System.out.println("Total hits num is " + results.totalHits);
			}

			return results;
//			Term term = new Term(field1,queryString);
//			Query query=new SimpleQuery(term,avgLength);
//			query.setBoost(1.0f);
//			Weight w=searcher.createNormalizedWeight(query);
//			System.out.println(w.getClass());
//			Query query = new TermQuery(term);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void loadGlobals(String filename){
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line=reader.readLine();
			avgLength=Float.parseFloat(line);
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public float getAvg(){
		return avgLength;
	}

	private void highLightDisplay (TopDocs topDocs, Query queryToSearch, String[] contents) throws InvalidTokenOffsetsException, IOException {
		ScoreDoc [] scoreDoc = topDocs.scoreDocs;
		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");

		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(queryToSearch));
		highlighter.setTextFragmenter(new SimpleFragmenter(20));
		for(int i = 0; i < scoreDoc.length; i++){
			int id =scoreDoc[i].doc;
			Document docHit = getDoc(id);
			String text = docHit.get("content");
			TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
			String highLightText = highlighter.getBestFragments(tokenStream, text, 5, "...");
			contents[i] = highLightText;
		}
	}
	
	public static void main(String[] args){
		ImageSearcher search=new ImageSearcher("/Users/huangyf/Dataset/SearchEngine/apache-tomcat-9.0.21/bin/forIndex/index");
//		search.loadGlobals("/Users/huangyf/Dataset/SearchEngine/apache-tomcat-9.0.21/bin/forIndex/global.txt");
		System.out.println("avg length = "+search.getAvg());
		String[] contents = new String[100];
		TopDocs results=search.searchQuery("清华大学生命科学学院",
									"title", "content", 100, contents);
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0; i < hits.length; i++) { // output raw format
			Document doc = search.getDoc(hits[i].doc);
			System.out.println("doc = " + hits[i].doc + " score = "
					+ hits[i].score+" url = "+doc.get("url"));
		}
	}
}
