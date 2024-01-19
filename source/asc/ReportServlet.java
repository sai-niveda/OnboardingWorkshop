package asc;
/* This web application has been simplified for demonstration purposes 
 * and do not represent a real production implementation - 
 * For example, production systems typically use tag libraries and a third-party MVC architecture (see Spring Framework,Tapestry,WebWork,Struts,JSF,JSTL). 
 * Original author L.Angrave. 
 * Copyright 2005 Isthmus Group LLC, Madison Wisconsin.
 * Licensed under the Apache License 2.0 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// external source not beeing scanned in this project
import io.lib.external_exec;

public class ReportServlet extends HttpServlet {
  
  public void service(HttpServletRequest request, HttpServletResponse response)
     throws IOException, ServletException {

    
    String reportName = ""; // request.getParameter("name");
    
    String path = request.getPathInfo();
    if(path != null) {
      reportName = path.startsWith("/") ? path.substring(1) : path; 
    }
    
    String format = "pdf";

    String htmlValue = null;
    Connection conn = null;
    InputStream is = null;
    try {
      
      is = getServletContext().getResourceAsStream("WEB-INF/reports/" +reportName.trim());

      if (is != null) {
        BufferedInputStream bis = new BufferedInputStream(is);
        if(reportName.endsWith(".pdf")) {
          response.setContentType("application/pdf");
          response.setHeader("Content-Disposition", "inline; filename=\"report.pdf\"");
        }
        ServletOutputStream ouputStream = response.getOutputStream();
        byte byteBuffer[] = new byte[8192];
        while(true) {
        int bytesRead = is.read(byteBuffer);
        if(bytesRead < 0) break;
        ouputStream.write(byteBuffer, 0, bytesRead);
        }
        
        ouputStream.flush();
        ouputStream.close();
        
        byteBuffer = null;
      } else {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Report Error</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("Report '"+reportName+"' not found");
        saveCustomReportAccessAttempt(reportName);
        saveCustomReportError(reportName);
        out.println("</body>");
        out.println("</html>");
        out.flush();        
      }

    } catch (Exception e) {
      String internal_report_id = getCurrentLoadedReport();
      saveCustomReportError(internal_report_id);
      saveCustomReportAccessAttempt(internal_report_id);
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Report Error</title>");
      out.println("</head>");
      out.println("<body bgcolor=\"white\">");
      out.println("The Report could not be generated:"+e.getMessage());
      out.println("<p>");
      // this is a safe Output
      out.println("Please make sure report identified: " + make_safe(reportName) + " exists.");
      out.println("</p>");
      out.println("<p>");
      out.println("Internal report name: " + internal_report_id + " not exists.");
      // this a safe Output but using validation strategy - validation != sanitization
      if (is_safe(reportName)) {
        out.println("Contact our support team with the name " + reportName + " to get your report.");       
      }
      out.println("</p>");
      out.println("</body>");
      out.println("</html>");
      out.flush();
    } finally {
      if(is != null ) try { is.close();} catch(Exception ignored) {}
    }


  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws IOException, ServletException {

    String theAgent = request.getHeader("user-agent");

    if (request.getHeader("user-agent").equals("contype")) {
      response.setContentType("application/pdf");
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }

    else if (theAgent.indexOf("MSIE") != -1){
      if (request.getHeader("accept-language") != null) {
        response.setContentType("application/pdf");
        response.setStatus(HttpServletResponse.SC_OK); 
        return; 
      }
    }
  }

  // custom implementation of sanitizer - XSS and SQLi
  public String make_safe(String value) {
    value = value.replaceAll("[^a-zA-Z0-9]", "");
    return value;
  }

  //custom implementation of validator
  public boolean is_safe(String value) {
    if (value.equals(make_safe(value))) {
      return true;
    } else {
      return false;
    }
  }

  @get
  public String getCurrentLoadedReport() {
    // custom implementation of DB read access - input
    String internal_report_id = external_exec.db.read.custom.session.getLoadedReport();
    return internal_report_id;
  }

  @post
  public void saveCustomReportError(String value) {
    // custom implementation of DB write access - output
    external_exec.db.write.custom.reportError("UPDATE tb.log.error SET msg=344 WHERE id=" + value);
  }

  @post
  public void saveCustomReportAccessAttempt(String value) {
    value = make_safe(value);
    // custom implementation of DB write access - output
    external_exec.db.write.custom.accessAttempt("UPDATE tb.log.error SET msg=406 WHERE id=" + value);
  }

  public string unusedMethod() {
    return "This method is declared but not used!"; // ToDo
  }

  public static int getExponential(int pow, int num) {
    if (pow < 1) { 
      return 1;
    }
    else {
      return num * getExponential(pow-1, num);
    }
  }

}
