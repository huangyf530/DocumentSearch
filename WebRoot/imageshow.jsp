<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
String currentQuery=(String) request.getAttribute("currentQuery");
int currentPage=(Integer) request.getAttribute("currentPage");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=currentQuery + " Page" + currentPage%>></title>
<style type="text/css">

#Layer1 {
	position:absolute;
	left:10px;
	top:30px;
	width:1500px;
	height:50px;
	z-index:1;

}
#Layer2 {
	position:absolute;
	left:135px;
	top:100px;
	width:700px;
	height:600px;
	z-index:2;
}
#Layer3 {
	position:absolute;
	left:30px;
	top:700px;
	width:700px;
	height:70px;
	z-index:3;
}

.searchbox{
	height: 30px;
	width: 460px;
	font-size:17px;


	border: 1px solid #ccc;
	border-radius: 3px;
	-webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
	box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
	-webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
	-o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
	transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;

	margin-left: 0px;
}

input:focus{
	outline:none;
	border-color: #F03D33;
	-webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075),0 0 8px rgba(240,61,51,.6);
	box-shadow: inset 0 1px 1px rgba(0,0,0,.075),0 0 8px rgba(240,61,51,.6)
}

.searchbutton{
	-webkit-appearance : none;
	height: 33px;
	width: 100px;
	border-width: 1px; /* 边框宽度 */
	border-radius: 3px; /* 边框半径 */
	background: #F03D33; /* 背景颜色 */
	cursor: pointer; /* 鼠标移入按钮范围时出现手势 */
	outline: none; /* 不显示轮廓线 */
	color: white; /* 字体颜色 */
	font-size: 17px; /* 字体大小 */

	margin-left: 10px;

}

.copyright{
	position: fixed;
	left: 50%;
	top: 100%;
	width: 500px;
	height: 30px;
	margin-left: -250px;
	margin-top: -30px;
	color: gray;
	font-family: Arial;
	font-size: 13px;
	text-align:center;
}
</style>
</head>

<body>
<div id="Layer1">
	<img src="/pic/hgss.png" style="height:30px; float: left; margin-top: 3px;" />

  <form id="form1" name="form1" method="get" action="ImageServer">
    <label>
      <input name="query" value="<%=currentQuery%>" type="text" size="70" class = "searchbox"/>
    </label>
    <label>
    <input type="submit" name="Submit" value="搜索" class = "searchbutton" />
    </label>
  </form>
</div>
<div id="Layer2" style="top: 82px; height: 585px;">
  <div id="imagediv">结果显示如下：
  <br>
  <Table style="left: 0px; width: 594px;">
  <% 
  	String[] titles=(String[]) request.getAttribute("titles");
  	String[] urls = (String[]) request.getAttribute("urls");
  	String[] contents = (String[]) request.getAttribute("contents");
  	if(titles!=null && titles.length>0){
		for(int i = 0; i < titles.length; i++){%>
		<div>
			<a href= <%="https://" + urls[i]%>>
				<h3><%=(currentPage - 1) * 10 + i + 1%>. <%=titles[i] %></h3>
				<br>
			</a>
		</div>
		<div>
			<span>
				<%=contents[i]%>
			</span>
		</div>
		<%}; %>
  	<%}else{ %>
	  <div>no such result</div>
  	<%}; %>
  </Table>
 </div>

  <div>
  	<p>
		<%if(currentPage>1){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage - 1%>">上一页</a>
		<%}; %>
		<%for (int i=Math.max(1,currentPage - 5);i<currentPage;i++){%>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<strong><%=currentPage%></strong>
		<%for (int i=currentPage + 1;i<=currentPage + 5;i++){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage + 1%>">下一页</a>
	</p>
  </div>
</div>
<div id="Layer3" style="top: 839px; left: 27px;">
	
</div>
<div>
</div>

<div id = "CopyRight" class="copyright">Copyright © 2019 Huangyf. All Rights Reserved.</div>
</body>
