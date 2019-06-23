import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class ImageServer extends HttpServlet{
	public static final int PAGE_RESULT = 10;
	public static final String indexDir = "forIndex2";
	public static final String picDir = "";

	String[] titles = null;
	String[] paths = null;
	String[] contents = null;
	String[] types = null;
	String[] urls = null;
	String[] base = null;
	private ImageSearcher search = null;
	public ImageServer(){
		super();
		search = new ImageSearcher(new String(indexDir+"/index"));
		search.loadGlobals(new String(indexDir+"/global.txt"));
		titles = new String[100];
		paths = new String[100];
		contents = new String[100];
		types = new String[100];
		urls = new String[100];
		base = new String[100];
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		if(results == null || results.length < (page - 1) * PAGE_RESULT){
			return null;
		}
		int start = Math.max((page - 1) * PAGE_RESULT, 0);
		int docnum = Math.min(results.length - start,PAGE_RESULT);
		ScoreDoc[] ret = new ScoreDoc[docnum];
		for(int i = 0;i < docnum; i++){
			ret[i] = results[start + i];
			contents[i] = contents[start + i];
		}
		return ret;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		String typeString = request.getParameter("type");
		int num = 0;
		int page = 1;
		if(typeString == null){
			typeString = "all";
		}
		if(pageString != null){
			page=Integer.parseInt(pageString);
		}
		if(queryString == null || queryString.equals("")){
			System.out.println("null query");
			request.getRequestDispatcher("/imagesearch.jsp").forward(request,
					response);
			return;
			//request.getRequestDispatcher("/Image.jsp").forward(request, response);
		}
		System.out.println("Query        : " + queryString);
//			System.out.println("Query(utf-8) : " + URLDecoder.decode(queryString, StandardCharsets.UTF_8));
//			System.out.println("Query(gb2312): " + URLDecoder.decode(queryString, "gb2312"));
		long startTime = new Date().getTime();
		TopDocs results = search.searchQuery(queryString, "title", "content", typeString, 100, contents);
		if (results != null) {
			num = results.scoreDocs.length;
			ScoreDoc[] hits = showList(results.scoreDocs, page);
			if (hits != null) {
				for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
					Document doc = search.getDoc(hits[i].doc);
					if(!doc.get("type").equals("html")){
						types[i] = "<sup>[" + doc.get("type") + "]</sup> ";
					}
					else{
						types[i] = "";
					}
					titles[i] = doc.get("title");
					urls[i] = picDir + doc.get("url");
					paths[i] = doc.get("path");
                    Path temp = Paths.get(doc.get("url"));
					base[i] = temp.subpath(0, 1).toString();
					System.out.println("doc = " + hits[i].doc + " score = "
							+ hits[i].score + " base = " + base[i]);
				}
			} else {
				System.out.println("page null");
			}
		}else{
			System.out.println("result null");
		}
		double eclipseTime = (double)(new Date().getTime() - startTime) / 1000D;
		request.setAttribute("currentQuery",queryString);
		request.setAttribute("currentPage", page);
		request.setAttribute("currentType", typeString);
		request.setAttribute("titles", titles);
		request.setAttribute("urls", urls);
		request.setAttribute("contents", contents);
		request.setAttribute("paths", paths);
		request.setAttribute("base", base);
		request.setAttribute("types", types);
		request.setAttribute("num", num);
		request.setAttribute("time", eclipseTime);
		request.getRequestDispatcher("/imageshow.jsp").forward(request,
				response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}
