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
<%!
private static String classPresent(String className)
{
    try
    {
        return Class.forName("javax.jws.WebService") != null ? "y" : "n";
    }
    catch (Exception ex)
    {
        return "n";
    }
}
%>
<body>
<h1>WebApp JSP Snoop page</h1> 
  
<h3>Request information</h3>
  
<table>
<tr>
    <th align=right>Servlet Name:</th>
    <td><%= getServletName() %></td>
</tr>  
<tr>
    <th align=right>Server Info:</th>
    <td><%=getServletContext().getServerInfo() %></td>
<tr>
<tr>
    <th align=right>Remote user:</th>
    <td><%= request.getRemoteUser() %></td>
<tr>
<tr>
    <th align=right>Remote address:</th>
    <td><%= request.getRemoteAddr() %></td>
<tr>
<tr>
    <th align=right>Remote host:</th>
    <td><%= request.getRemoteHost() %></td>
<tr>
<tr>
    <th align=right>Authorization scheme:</th>
    <td><%= request.getAuthType() %></td>
<tr>
<tr>
    <th align=right>Default Response Buffer:</th>
    <td><%= response.getBufferSize() %></td>
<tr>
</table>
  
<%
    Enumeration<String> e = request.getHeaderNames();
    if (e != null && e.hasMoreElements()) {
%>
<h3>Request headers</h3>
  
<table>
<tr>
    <th align=left>Header:</th>
    <th align=left>Value:</th>
</tr>
<%
        while (e.hasMoreElements()) {
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
    e = request.getParameterNames();
    if (e != null && e.hasMoreElements()) {
%>
<h3>Request parameters</h3>
<table>
<TR valign=top>
    <th align=left>Parameter:</th>
    <th align=left>Value:</th>
    <th align=left>Multiple values:</th>
</tr>
<%
        while (e.hasMoreElements()) {
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
                    out.print("<br />");
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
<table>
<TR valign=top>
    <th align=left>Parameter:</th>
    <th align=left>Value:</th>
</tr>
<%
        while (e.hasMoreElements()) {
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
    if (e != null && e.hasMoreElements()) {
%>
<h3>Context Init parameters</h3>
<table>
<TR valign=top>
    <th align=left>Parameter:</th>
    <th align=left>Value:</th>
</tr>
<%
        while (e.hasMoreElements()) {
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
    if (e != null && e.hasMoreElements()) {
%>
<h3>Context Attributes</h3>
<table>
<TR valign=top>
    <th align=left>Parameter:</th>
    <th align=left>Value:</th>
</tr>
<%
        while (e.hasMoreElements()) {
            String k = (String) e.nextElement();
            Object val = getServletContext().getAttribute(k);
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
  
<h3>Datasources</h3>
<table ><tr><td valign="top">
<%
String contextName = "datasources"; // "java:/datasources";
try {
    Context initCtx = new InitialContext();
    NamingEnumeration<Binding> ne = initCtx.listBindings(contextName);
    %>
  
    <table>
    <tr><th>Name</th><th>Type</th></tr>
    <%  
    while (ne.hasMore()) {
      Binding binding = (Binding) ne.next();
    %>
    <tr>
      <td><%= binding.getName() %></td>
      <td>Type: <%=binding.getClassName() %></td>
    </tr>
    <%
    }
    %></table>
<%} catch (NamingException ex) {%>
    Nothing found at: <%=contextName%>
<%}%>
</td></tr></table>

<h3>Classes present</h3>
  
<table>
 <tr><td>javax.jws.WebService</td><td><%= classPresent("javax.jws.WebService") %></td></tr>
 <tr><td>org.jboss.weld.servlet.WeldInitialListener</td><td><%= classPresent("org.jboss.weld.servlet.WeldInitialListener") %></td></tr>
 <tr><td>org.eclipse.persistence.exceptions.EntityManagerSetupException</td><td><%= classPresent("org.eclipse.persistence.exceptions.EntityManagerSetupException") %></td></tr>
</table> 

</body>
</html>
