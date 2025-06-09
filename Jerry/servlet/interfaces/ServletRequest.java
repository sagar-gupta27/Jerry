package interfaces;

import java.util.Enumeration;

public interface ServletRequest {
    public Object getAttributes(String name);

    public Enumeration<String> getAttributeNames();

    public String getCharacterEncoding();

    public void setCharacterEncoding();

    public int getContentLength();

    public String getContentType();

    // public ServletInputStream getInputStream() throws Exception;
    public String getParameter(String name);

    public Enumeration<String> getParaterNames();

    public String[] getParameterValues(String name);

    public String getProtocol(); // version of protocol in form of protocol/majorVersion.miniVersion

    public String getScheme(); // scheme http / https / ftp

    public String getServerName();

    public int getServerPort();

    public String getRemoteAddr(); // Ip addr of client that sent the request

    public String getRemoteHost(); // Fully qualified name of client , CGI variable

    public void setAttribute(String name, Object o);

    public void removeAttribute(String name);
    // public Locale getLocale();
    // public Enumeration<Locale> getLocales();

    public boolean isSecure();
    // public RequestDispatcher getRequestDispatcher(String path);

    public int getRemotePort(); // IP source port of client

    public String getLocalName(); // Host name of IP on which request was received

    public String getLocalAddr(); // IP of interface on which request was received

    public int getLocalPort(); // IP port number of Interface on which request was received

    // public ServletContext getServletContext();
    // public boolean isAsyncSupported();
    // public boolean isAsyncStarted();
    // public AsyncContext startAsync(ServletRequest servletRequest,ServletResponse
    // servletResponse) throws IllegalStateException;
    // public AsyncContext getAsyncContext();
    // public AsyncContext startAsync() throws IllegalStateException;

    // public DispathcerType getDispatcherType();

}
