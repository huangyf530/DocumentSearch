import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.jsoup.Jsoup;
import org.jgrapht.alg.scoring.PageRank;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CalPageRank {
    Graph<String, DefaultEdge> graph;
    HashSet<String> websites;
    private SimpleDateFormat dateFormat;
    long docnum;
    Map<String, Double> score;
    CalPageRank(){
        websites = new HashSet<>();
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        docnum = 0;
    }

    private void indexHtmlFile(String filename, String base,String url){
        graph.addVertex(url);
        websites.add(url);
        docnum++;
        try{
            org.jsoup.nodes.Document doc = Jsoup.parse(new File(filename), "utf-8");
            org.jsoup.select.Elements links = doc.select("a[href]");
            int linknum = 0;
            for (org.jsoup.nodes.Element link : links) {
//                System.out.println(linknum + ": " + link.text() + link.attr("href"));
                linknum++;
                String temp = link.attr("href");
                if(temp.length() >= 4 && temp.substring(0, 4).equals("http")){
//                    System.out.println(temp.substring(temp.indexOf("://") + 3));
                    temp = temp.substring(temp.indexOf("://") + 3);
                }
                else{
//                    System.out.println(base + link.attr("href"));
                    temp = base + temp;
                }
                if(!websites.contains(temp)){
                    websites.add(temp);
                    graph.addVertex(temp);
                }
                graph.addEdge(url, temp);
            }
//            System.out.println("links number is " + linknum);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printPageRank(String filename){
//        int k = 0;
//        for(DefaultEdge e : graph.edgeSet()){
//            System.out.println(k + ". " + graph.getEdgeSource(e) + " --> " + graph.getEdgeTarget(e));
//            k++;
//        }
//        k = 0;
//        for(String temp : graph.vertexSet()){
//            k++;
//            System.out.println(k + ". " + temp + "\t" + score.get(temp));
//        }
        System.out.println(getTime() + " " + "begin write pagerank to " + filename);
        try{
            PrintWriter pw=new PrintWriter(new File(filename));
            for(String temp : graph.vertexSet()){
                pw.println(temp + "\t" + score.get(temp));
            }
            pw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private String getTime(){
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void handleFile(String filename, String base,String url){
        File dataDir  = new File(filename);
        if(!dataDir.isDirectory()) {
            if(dataDir.getName().toLowerCase().endsWith(".html") || dataDir.getName().toLowerCase().endsWith(".htm")){
                indexHtmlFile(filename, base, url);
            }
            return;
        }
        File[] dataFiles = dataDir.listFiles();
        if(dataFiles == null){
            return;
        }
        for(int i = 0; i < dataFiles.length; i++){
            handleFile(filename + "/" + dataFiles[i].getName(), base, url + "/" + dataFiles[i].getName());
        }
    }

    private void beginCalculate(double dampingfactor){
        long startTime = new Date().getTime();
        System.out.println(getTime() + " begin cal pagerank!");
        PageRank<String, DefaultEdge> pageRank = new PageRank<>(graph, dampingfactor);
        long endTime = new Date().getTime();
        System.out.println(getTime() + " It takes " + (endTime - startTime) / 1000
                + " seconds to calculate pagerank for " + websites.size() + " urls");
        score = pageRank.getScores();
    }

    private void indexFromDir(String filename) {
        try {
            File dataDir = new File(filename);
            if (!dataDir.isDirectory()) {
                return;
            }
            File[] dataFiles = dataDir.listFiles();
            if (dataFiles == null) {
                return;
            }
            long startTime = new Date().getTime();
            System.out.println(getTime() + ": begin create index from directory " + dataDir.getCanonicalPath());
            for (int i = 0; i < dataFiles.length; i++) {
                handleFile(filename + "/" + dataFiles[i].getName(), dataFiles[i].getName(), dataFiles[i].getName());
            }
            long endTime = new Date().getTime();
            System.out.println(getTime() + " It takes " + (endTime - startTime) / 1000
                    + " seconds to create index for " + docnum + " files in directory "
                    + dataDir.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        CalPageRank calPageRank = new CalPageRank();
//        calPageRank.indexHtmlFile("input/test.html", "h", "h");
        try {
            File datadir = new File("/Users/huangyf/Dataset/SearchEngine/Big");
            File[] dataFiles = datadir.listFiles();
            for(File file : dataFiles){
                System.out.println("From " + file.getCanonicalPath());
                calPageRank.indexFromDir(file.getCanonicalPath());
            }
            calPageRank.beginCalculate(0.85);
            calPageRank.printPageRank("/Users/huangyf/Desktop/大三春季学期/搜索引擎技术基础/Homework/3BigHomework/ImageSearch/" +
                    "forIndex/pagerank.txt");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
