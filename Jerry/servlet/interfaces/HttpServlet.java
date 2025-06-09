package servlet.interfaces;

import servlet.impl.HttpServletRequest;
import servlet.impl.HttpServletResponse;

public abstract class HttpServlet implements Servlet{

    protected void doGet(HttpServletRequest req, HttpServletResponse res){
        
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse res){}
    protected void doPut(HttpServletRequest req, HttpServletResponse res){}
    protected void doPost(HttpServletRequest req, HttpServletResponse res){}

    //head and trace not required

}