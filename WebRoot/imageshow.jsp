<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>无标题文档</title>
<style type="text/css">
<!--
#Layer1 {
	position:absolute;
	left:28px;
	top:26px;
	width:649px;
	height:32px;
	z-index:1;
}
#Layer2 {
	position:absolute;
	left:29px;
	top:82px;
	width:648px;
	height:602px;
	z-index:2;
}
#Layer3 {
	position:absolute;
	left:28px;
	top:697px;
	width:652px;
	height:67px;
	z-index:3;
}
-->
</style>
</head>

<body>
<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
%>
<div id="Layer1">
  <form id="form1" name="form1" method="get" action="ImageServer">
    <label>
      <input name="query" value="<%=currentQuery%>" type="text" size="70" />
    </label>
    <label>
    <input type="submit" name="Submit" value="查询" />
    </label>
  </form>
</div>
<div id="Layer2" style="top: 82px; height: 585px;">
  <div id="imagediv">结果显示如下：
  <br>
  <Table style="left: 0px; width: 594px;">
  <% 
  	String[] imgTags=(String[]) request.getAttribute("imgTags");
  	String[] imgPaths=(String[]) request.getAttribute("imgPaths");
  	if(imgTags!=null && imgTags.length>0){
  		for(int i=0;i<imgTags.length;i++){%>
  		<p>
  		<tr><h3><%=(currentPage-1)*10+i+1%>. <%=imgTags[i] %></h3></tr>
  		<tr><img src="<%=imagePath+imgPaths[i]%>" alt="<%=imagePath+imgPaths[i]%>" width=200 height=100 /></tr>
  		</p>
  		<%}; %>
  	<%}else{ %>
  		<p><tr><h3>no such result</h3></tr></p>
  	<%}; %>
  </Table>
  </div>
  <div>
  	<p>
		<%if(currentPage>1){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a>
		<%}; %>
		<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<strong><%=currentPage%></strong>
		<%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a>
	</p>
  </div>
</div>
<div id="Layer3" style="top: 839px; left: 27px;">
	
</div>
<div>
</div>
</body>
