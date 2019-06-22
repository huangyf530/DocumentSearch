import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.nio.charset.StandardCharsets;
import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class ImageServer extends HttpServlet{
	public static final int PAGE_RESULT = 10;
	public static final String indexDir = "forIndex";
	public static final String picDir = "";
	String[] titles = null;
	String[] paths = null;
	String[] contents = null;
	String[] types = null;
	private ImageSearcher search = null;
	public ImageServer(){
		super();
		search = new ImageSearcher(new String(indexDir+"/index"));
		search.loadGlobals(new String(indexDir+"/global.txt"));
		titles = new String[100];
		paths = new String[100];
		contents = new String[100];
		types = new String[100];
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		if(results==null || results.length<(page - 1) * PAGE_RESULT){
			return null;
		}
		int start=Math.max((page-1)*PAGE_RESULT, 0);
		int docnum=Math.min(results.length-start,PAGE_RESULT);
		ScoreDoc[] ret=new ScoreDoc[docnum];
		for(int i=0;i<docnum;i++){
			ret[i]=results[start+i];
		}
		return ret;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		int page = 1;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		if(queryString==null){
			System.out.println("null query");
			//request.getRequestDispatcher("/Image.jsp").forward(request, response);
		}else{
			System.out.println("Query        : " + queryString);
//			System.out.println("Query(utf-8) : " + URLDecoder.decode(queryString, StandardCharsets.UTF_8));
//			System.out.println("Query(gb2312): " + URLDecoder.decode(queryString, "gb2312"));
			TopDocs results=search.searchQuery(queryString, "title", "content",100, contents);
			if (results != null) {
				ScoreDoc[] hits = showList(results.scoreDocs, page);
				if (hits != null) {
					for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
						Document doc = search.getDoc(hits[i].doc);
						System.out.println("doc = " + hits[i].doc + " score = "
								+ hits[i].score + " url = "
								+ doc.get("url")+ " title = "+ doc.get("title"));
						titles[i] = doc.get("title");
						paths[i] = picDir + doc.get("url");
						types[i] = doc.get("type");
					}
				} else {
					System.out.println("page null");
				}
			}else{
				System.out.println("result null");
			}
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("titles", titles);
			request.setAttribute("urls", paths);
			request.setAttribute("contents", contents);
			request.getRequestDispatcher("/imageshow.jsp").forward(request,
					response);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}
