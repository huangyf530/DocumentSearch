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
*{
  margin: 0;
  padding: 0;
}
  .wrapper{
    position: fixed;
    left: 50%;
    top: 30%;
    width: 500px;
    height: 250px;
    margin-left: -250px;
    margin-top: -30px;
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
  }

  .title{
    position: fixed;
    left: 50%;
    top: 10%;

    width: 191px;
    height: 45px;


    margin-top: 0;
    margin-left: -95px;
  }

  .searchbox{
    height: 30px;
    width: 460px;
    font-size:17px;
    margin-top: 220px;
    background-image: url(pic/sousuo.png);
    background-repeat: no-repeat;
    background-size: 25px;
    padding:0 0 0 30px;

  }

  .searchbutton{
    -webkit-appearance : none;
    height: 40px;
    width: 100px;
    border-width: 0px; /* 边框宽度 */
    border-radius: 3px; /* 边框半径 */
    background: #F03D33; /* 背景颜色 */
    cursor: pointer; /* 鼠标移入按钮范围时出现手势 */
    outline: none; /* 不显示轮廓线 */
    color: white; /* 字体颜色 */
    font-size: 17px; /* 字体大小 */
  }

  .hyfpic{
    position: absolute;
    margin-left: 100px;
    margin-top: 0px;
    width: 300px;
    height: 200px;
  }



</style>

  <img class="title" src="/pic/hgss.png"/>

</head>



<body style="text-align:center">

<div id="Layer1" class="wrapper">

  <div id="Layer0" class="hyfpic">
    <img src="/pic/logo.jpg" style="height: 200px; width: 300px;"/>
  </div>

  <form id="form1" name="form1" method="get" action="servlet/ImageServer">
    <label>
      <input name="query" type="text" class="searchbox" placeholder="请输入要查询的关键字"/>
    </label>
    <br/>
    <br/>
    <label>
    <input type="submit" name="Submit" class="searchbutton" value="搜索" />
    </label>
  </form>
</div>

<div id = "CopyRight" class="copyright">Copyright © 2019 Huangyf. All Rights Reserved.</div>



</body>
</html>
