package org.network.Servelet;


import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class ServletHandle
 */
@WebServlet("/ServletHandle")
public class ServletHandle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String csvFilePath = new String("");
    private NetworkDataAnalysis netTool = new NetworkDataAnalysis();
       
    public ServletHandle() {
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
    	
    	// load original data
    	if (request.getParameter("csv") != null) {
	    	String path = getClass().getClassLoader().getResource("").getPath();
	    	String fullPath = URLDecoder.decode(path, "UTF-8");
	    	String pathArr[] = fullPath.split("/.metadata/.plugins");
	    	csvFilePath = pathArr[0] + "/Final_Project_6893/WebContent/" + request.getParameter("csv");
	    	netTool.shutDown();
		    netTool.createDb(csvFilePath);   
		    
		    response.sendRedirect("index.html");
    	} 
    	// path find
    	else if (request.getParameter("start") != null) {
    		String startNode = request.getParameter("start");
    		String endNode = request.getParameter("end");
        	String output = netTool.getPath(Integer.parseInt(startNode),
											Integer.parseInt(endNode));
        	response.sendRedirect("index.html");
    	}
    	// cluster
    	else {
    		netTool.clustering();
    	}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

    void exit() {
    	netTool.shutDown();
    }
	
}
