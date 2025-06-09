//This the servlet contract that all servlets must abid by

package servlet.interfaces;

public interface Servlet {

    public void init(ServletConfig servletConfig) throws Exception;
    public ServletConfig getServerConfig();
    public void service(ServletRequest req, ServletResponse res) throws Exception;
    public String getServletInfo();
    public void destroy();

}