<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
System.out.println(request.getCharacterEncoding());
response.setCharacterEncoding("utf-8");
System.out.println(response.getCharacterEncoding());
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>搜索</title>
<style type="text/css">
<!--
#Layer1 {
	position:absolute;
	left:489px;
	top:326px;
	width:404px;
	height:29px;
	z-index:1;
}
#Layer2 {
	position:absolute;
	left:480px;
	top:68px;
	width:446px;
	height:152px;
	z-index:2;
}
-->
</style>
</head>
<body>
<div id="Layer1" style="top: 210px; left: 353px; width: 441px;">
  <form id="form1" name="form1" method="get" action="servlet/ImageServer">
    <label>
      <input name="query" type="text" size="50" />
    </label>
    <label>
    <input type="submit" name="Submit" value="搜索" />
    </label>
  </form>
</div>
</body>
</html>
