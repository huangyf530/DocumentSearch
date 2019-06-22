<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	request.setCharacterEncoding("utf-8");
	response.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
	String currentQuery=(String) request.getAttribute("currentQuery");
	String currentType = (String) request.getAttribute("currentType");
	int currentPage=(Integer) request.getAttribute("currentPage");
	int resultnum = (Integer) request.getAttribute("num");
	double time = (Double) request.getAttribute("time");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=currentQuery + " Page" + currentPage%></title>

	<link rel="stylesheet" href="/speech-input.css">

	<style type="text/css">

#Layer1 {
	position:absolute;
	left:10px;
	top:30px;
	width:1000px;
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

.speech-input{
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
.m,
a.m{
	color:#666;
	font-size: 13px;
}
a.m:visited{color: #606}

.c-showurl{
	font-size: 13px;
	color: forestgreen;
}

.s_tab {

	line-height:30px;
	padding:0 0 0;


	display: block;
	-webkit-box-sizing:border-box;
	box-sizing:border-box;

	width:700px;

	background:#f8f8f8;

/*
	border: 3px solid #F03D33;
	*/
}

#S_tab a{
	width:54px;
	display:inline-block;
	text-decoration:none;
	text-align:center;
	color:#666;
	font-size:14px;
}

#S_tab b{

	width:54px;
	display:inline-block;
	text-decoration:none;
	text-align:center;
	font-size:14px;

	border-bottom:2px solid #F03D33;
	font-weight:700;
	color:#323232;
}
	#S_tab a:hover{
		color:#323232;
	}

</style>
</head>

<body>
<div id="Layer1">
	<img src="/pic/hgss.png" style="height:30px; float: left; margin-top: 3px;" />

  <form id="form1" name="form1" method="get" action="ImageServer">
    <label>
      <input name="query" value="<%=currentQuery%>" type="text" class = "speech-input" autocomplete="off"/>
    </label>
    <label>
    <input type="submit" name="Submit" value="搜索" class = "searchbutton" />
    </label>
  </form>
</div>
<div id="Layer2" style="top: 82px; height: 585px;">

	<div id="S_tab" class="s_tab">
		<%if(currentType.equals("all")) {%>
		<b>全部</b>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=html">HTML</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=pdf">PDF</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=docx">DOCX</a>
		<%} else if(currentType.equals("html")){%>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=all">全部</a>
		<b>HTML</b>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=pdf">PDF</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=docx">DOCX</a>
		<%} else if(currentType.equals("pdf")){%>

		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=all">全部</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=html">HTML</a>
		<b>PDF</b>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=docx">DOCX</a>

		<%} else if(currentType.equals("docx")){%>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=all">全部</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=html">HTML</a>
		<a href="ImageServer?query=<%=currentQuery%>&page=1&type=pdf">PDF</a>
		<b>DOCX</b>

		<%};%>

	</div>

	<br/>


	<div style="color:grey; font-size: 15px;">
		找到 <%=resultnum%> 条结果（用时 <%=time%> s），结果显示如下：
	</div>


	<div id="imagediv">
  <Table style="left: 0px; width: 600px;">
  <% 
  	String[] titles=(String[]) request.getAttribute("titles");
  	String[] urls = (String[]) request.getAttribute("urls");
  	String[] contents = (String[]) request.getAttribute("contents");
  	String[] paths = (String[]) request.getAttribute("paths");
  	String[] base = (String[]) request.getAttribute("base");
  	String[] types = (String[]) request.getAttribute("types");
  	int showpagenum = Math.min(currentPage + 5, Math.floorDiv(resultnum + 9, 10));
  	int remain_num = resultnum - (currentPage - 1) * 10;
  	if(titles!=null && remain_num>0){
		for(int i = 0; i < 10 && i< remain_num; i++){%>
		<div>
			<a href= <%="https://" + urls[i]%>>
				<h3><%=types[i]%><%=titles[i] %></h3>
			</a>
		</div>
		<div>
			<span>
				<%=contents[i]%>
			</span>
		</div>
	    <div>
			<a href="<%="https://" + base[i]%>" class="c-showurl"><%=base[i] + "/"%></a>
			<span>
				 -
				<a href="<%="/" + paths[i]%>" class="m">威哥快照</a>
			</span>
	    </div>
	  <br/>
		<%}; %>
  	<%}else{ %>
	  <div style="font-size: 70px; color:#F03D33">no such result</div>
  	<%}; %>
  </Table>
 </div>

	<br/>
	<br/>


  <div>
  	<p style="text-align:center">
		<%if(currentPage>1){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage - 1%>">上一页</a>
		<%}; %>
		<%for (int i=Math.max(1,currentPage - 5);i<currentPage;i++){%>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<strong><%=currentPage%></strong>
		<%for (int i=currentPage + 1;i<=showpagenum;i++){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<%if (currentPage < Math.floorDiv(resultnum + 9, 10)){ %>
			<a href="ImageServer?query=<%=currentQuery%>&page=<%=currentPage + 1%>">下一页</a>
		<%};%>
	</p>
	  <p> </p>
	  <p> </p>

	  <p style="color: white">1</p>
  </div>
</div>

<div>


</div>
<div id = "CopyRight" class="copyright">Copyright © 2019 Huangyf. & GengW. All Rights Reserved.</div>
</body>
<script src="/speech-input.js"></script>

</html>