<%@ page language="java" contentType="text/html; charset=utf-8"  import="java.net.InetAddress" pageEncoding="utf-8"%>
<%InetAddress addr = InetAddress.getLocalHost();out.print("v3 : " + addr.getHostName()+"   : " + addr.getHostAddress());%>
