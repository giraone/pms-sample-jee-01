<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>JSP snoop page</title>
<%@ page import="
    java.util.Enumeration,
    javax.servlet.http.Cookie,
    javax.naming.InitialContext,
    javax.sql.DataSource,
    java.sql.Connection,
    javax.naming.*,
    java.net.*" %>
</head>
<body>

<h1>WebApp JSP Snoop page</h1> 
  
<h3>Request information</h3>
  
<table border="1">
<tr>
    <TH align=right>Servlet Name:</TH>
    <td><%= getServletName() %></td>
</tr>
<tr>
    <TH align=right>Request protocol:</TH>
    <td><%= request.getProtocol() %></td>
</tr>
<tr>
    <TH align=right>Scheme:</TH>
    <td><%= request.getScheme() %></td>
</tr>
<tr>
    <TH align=right>Server name:</TH>
    <td><%= request.getServerName() %></td>
<tr>
<tr>
    <TH align=right>Server port:</TH>
    <td><%= request.getServerPort() %></td>
<tr>
  
<tr>
    <TH align=right>Server Info:</TH>
    <td><%=getServletContext().getServerInfo() %></td>
<tr>
<tr>
    <TH align=right>Requested URL:</TH>
    <td><%= HttpUtils.getRequestURL(request) %></td>
</tr>
<tr>
    <TH align=right>Request method:</TH>
    <td><%= request.getMethod()%></td>
</tr>
<tr>
    <TH align=right>Request URI:</TH>
    <td><%= request.getRequestURI() %></td>
</tr>
<tr>
    <TH align=right>Servlet path:</TH>
    <td><%= request.getServletPath() %></td>
</tr>
<tr>
    <TH align=right>Path info:</TH>
    <td><%= request.getPathInfo() %></td>
</tr>
<tr>
    <TH align=right>Path translated:</TH>
    <td><%= request.getPathTranslated() %></td>
</tr>
<tr>
    <TH align=right>Query string:</TH>
    <td><%= request.getQueryString() %></td>
</tr>
<tr>
    <TH align=right>Content length:</TH>
    <td><%= request.getContentLength() %></td>
</tr>
<tr>
    <TH align=right>Content type:</TH>
    <td><%= request.getContentType() %></td>
<tr>
<tr>
    <TH align=right>Remote user:</TH>
    <td><%= request.getRemoteUser() %></td>
<tr>
<tr>
    <TH align=right>Remote address:</TH>
    <td><%= request.getRemoteAddr() %></td>
<tr>
<tr>
    <TH align=right>Remote host:</TH>
    <td><%= request.getRemoteHost() %></td>
<tr>
<tr>
    <TH align=right>Authorization scheme:</TH>
    <td><%= request.getAuthType() %></td>
<tr>
<tr>
    <TH align=right>Default Response Buffer:</TH>
    <td><%= response.getBufferSize() %></td>
<tr>
</table>
  
<%
    Enumeration e = request.getHeaderNames();
    if(e != null && e.hasMoreElements()) {
%>
<h3>Request headers</h3>
  
<table border="1">
<tr>
    <TH align=left>Header:</TH>
    <TH align=left>Value:</TH>
</tr>
<%
        while(e.hasMoreElements()) {
            String k = (String) e.nextElement();
%>
<tr>
    <td><%= k %></td>
    <td><%= request.getHeader(k) %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
  
<%
    Cookie[] c = request.getCookies();
    if(c !=null) {
%>
<h3>Cookies in this Request</h3>
<table border="1">
<TR valign=top>
    <TH align=left>Parameter:</TH>
    <TH align=left>Value:</TH>
</tr>
<%
        for (int i = 0; i < c.length; i++) {
%>
<TR valign=top>
    <td><%= c[i].getName() %></td>
    <td><%= c[i].getValue() %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
  
<%
    e = request.getParameterNames();
    if(e != null && e.hasMoreElements()) {
%>
<h3>Request parameters</h3>
<table border="1">
<TR valign=top>
    <TH align=left>Parameter:</TH>
    <TH align=left>Value:</TH>
    <TH align=left>Multiple values:</TH>
</tr>
<%
        while(e.hasMoreElements()) {
            String k = (String) e.nextElement();
            String val = request.getParameter(k);
            String vals[] = request.getParameterValues(k);
%>
<TR valign=top>
    <td><%= k %></td>
    <td><%= val %></td>
    <td><%
            for(int i = 0; i < vals.length; i++) {
                if(i > 0)
                    out.print("<BR>");
                out.print(vals[i]);
            }
        %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
<%
    e = getInitParameterNames();
    if(e != null && e.hasMoreElements()) {
%>
<h3>Servlet Init parameters</h3>
<table border="1">
<TR valign=top>
    <TH align=left>Parameter:</TH>
    <TH align=left>Value:</TH>
</tr>
<%
        while(e.hasMoreElements()) {
            String k = (String) e.nextElement();
            String val = getServletConfig().getInitParameter(k);
%>
<TR valign=top>
    <td><%= k %></td>
    <td><%= val %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
<%
    e = getServletConfig().getInitParameterNames();
    if(e != null && e.hasMoreElements()) {
%>
<h3>Context Init parameters</h3>
<table border="1">
<TR valign=top>
    <TH align=left>Parameter:</TH>
    <TH align=left>Value:</TH>
</tr>
<%
        while(e.hasMoreElements()) {
            String k = (String) e.nextElement();
            String val = getServletConfig().getInitParameter(k);
%>
<TR valign=top>
    <td><%= k %></td>
    <td><%= val %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
<%
    e = getServletContext().getAttributeNames();
    if(e != null && e.hasMoreElements()) {
%>
<h3>Context Attributes</h3>
<table border="1">
<TR valign=top>
    <TH align=left>Parameter:</TH>
    <TH align=left>Value:</TH>
</tr>
<%
        while(e.hasMoreElements()) {
            String k = (String) e.nextElement();
            String val = getServletConfig().getInitParameter(k);
%>
<TR valign=top>
    <td><%= k %></td>
    <td><%= val %></td>
</tr>
<%
        }
%>
</table>
<%
    }
%>
  
<h3>Datasource Tests</h3>
<table border="1" width="75%">
    <tr>
        <td valign="top">
<h3>JNDI DataSources</h3>
<%
String contextName = "java:/comp/env/jdbc";
try {
    Context initCtx = new InitialContext();
    NamingEnumeration ne = initCtx.listBindings(contextName);
    %>
  
    <table border="1">
    <tr><TH>Name</TH><TH>Type</TH></tr>
    <%  
    while (ne.hasMore()) {
      Binding binding = (Binding) ne.next();
    %>
    <tr>
      <td><a href="?jndiName=jdbc/<%=binding.getName()%>">
        java:comp/env/jdbc/<%=binding.getName()%></a></td>
      <td>Type: <%=binding.getClassName() %></td>
    </tr>
    <%
    }
    %></table>
<%} catch (NamingException ex) {%>
    Nothing found at: <%=contextName%>
<%}%>
          
        </td>
        <td>
  
<% boolean jndiSuccess = false;
String jndiName = (String)request.getParameter("jndiName");
  
String message = "";
if(jndiName != null && !"".equals(jndiName.trim())) {
    Connection conn = null;
    try {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        if(envCtx!=null){
            DataSource ds = (DataSource)envCtx.lookup(jndiName);
            conn = ds.getConnection();
            if(conn != null) jndiSuccess = true;
            %>
<table border="1">
<tr>
<TH>JDBC URL</TH>
<td><%=conn.getMetaData().getURL() %> </td>
</tr>        
<tr>
<TH>JDBC Username</TH>
<td><%=conn.getMetaData().getUserName() %> </td>
</tr>        
<tr>
<TH>Database </TH>
<td><%=conn.getMetaData().getDatabaseProductName() %>
<%=conn.getMetaData().getDatabaseProductVersion() %></td>
</tr>        
<tr>
<TH>Driver</TH>
<td><%=conn.getMetaData().getDriverName() %> <%=conn.getMetaData().getDriverVersion() %></td>
</tr>        
<tr>
<TH>JDBC Version</TH>
<td><%=conn.getMetaData().getJDBCMajorVersion() %> </td>
</tr>
<tr>
<TH>JDBC Max Connections</TH>
<td><%=conn.getMetaData().getMaxConnections() %> </td>
</tr>        
              
</table>
            <%          
        }
    } catch (Exception ex) {
        message = ex.getMessage();
    } finally {
        try{conn.close();}catch(Exception ex){}
    }
      
}
%>
  
<form action="" method="GET">
<p><label for="jndiName">DataSource JNDI Name: </label> <br/>
java:comp/env/<input name="jndiName" type="text" value="<%=jndiName==null? "" : jndiName%>"></p>
<p>
<%if(jndiSuccess) {%>
Connection Success!
<%} else if (!"".equals(message)) {%>
Connection failed with message: <%=message %>
<%} %>
</p>
<p><input name="submit" type="submit" value="submit"></p>
</form>
          
        </td>
    </tr>
</table>
  
  
<h3>JAX-WS</h3>
  
<table border="1">
<TR valign=top>
    <TH align=left>API</TH>
    <TH align=left>Sun Implementation</TH>
</tr>
<TR valign=top>
    <td>
<%
    try {
        Class.forName("javax.jws.WebService");
        %>Present<%
    } catch (Exception ex) {
        %>Missing<%
    }
%>  
    </td>
    <td>
<%
    try {
        Class.forName("com.sun.istack.Builder");
        %>Present<%
    } catch (Exception ex) {
        %>Missing<%
    }
%>  
    </td>
</tr>
</table>
  

<h3>Classloader Query</h3>
<p>The form below returns information about where a class is being served from.</p>
 
<form action="#classLocation" method="GET">
<p>
<select name="className">
<option value="java.lang.String">JDK CORE</option>
<option value="org.springframework.context.ApplicationContext">spring-context</option>
</select><input type="submit" value="Submit"/>
<%
String className = request.getParameter("className");
URL location = null;
if(className!=null) {
    Class klass = null;
    try {
    klass = Class.forName(className);
    location = klass.getResource('/'+klass.getName().replace('.','/')+".class");
    } catch(Exception ex) {}
}
%>
<a id="classLocation"></a><%=location==null ? "Class Not Found in any JAR in classpath." : location%>
</p>
</form>
 

</body>
</html>
