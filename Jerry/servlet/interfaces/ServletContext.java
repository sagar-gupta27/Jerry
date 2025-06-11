package servlet.interfaces;

import java.util.Enumeration;
import java.util.EventListener;
import java.util.Set;

/* 
 * 
 * Defines a set of methods that a servlet uses to communicate with its
 * servlet container, for example, to get the MIME type of a file,
 * dispatch requests, or write to a log file.
 *
 * <p>There is one context per "web application" per Java Virtual Machine.  (A
 * "web application" is a collection of servlets and content installed under a
 * specific subset of the server's URL namespace such as <code>/catalog</code>
 * and possibly installed via a <code>.war</code> file.)
 *
 * <p>In the case of a web
 * application marked "distributed" in its deployment descriptor, there will
 * be one context instance for each virtual machine.  In this situation, the
 * context cannot be used as a location to share global information (because
 * the information won't be truly global).  Use an external resource like
 * a database instead.
 *
 * <p>The <code>ServletContext</code> object is contained within
 * the {@link ServletConfig} object, which the Web server provides the
 * servlet when the servlet is initialized.
 * 
 * 
 */
public interface ServletContext {
    // public static final String TEMPDIR = "servlet.server.context.tempdir";

    // public static final String ORDERED_LIBS ="javax.servlet.context.orderedLibs";

    public String getContextPath();

    public ServletContext getContext();

    public int getMajorVersion();

    public int getMinorVersion();

    public int getEffectiveMajorVersion();

    public int getEffectiveMinorVersion();

    public String getMimeType(String file);

    public Set<String> getResourcePath(String path);

    // public getResource(String path) throws MalformedURLException;
    // public getResourceAsStream(String path);
    public RequestDispatcher getRequestDispatcher(String path);

    public RequestDispatcher getNamedDispatcher(String name);

    public String getRealPath(String path);

    public String getServerInfo(); // Name and version of servlet container

    public String getInitParameter(String name);

    public Enumeration<String> getInitParameterNames();

    public boolean setInitParameter(String name, String value);

    public Object getAtrribute(String name);

    public Enumeration<Object> getAtrributeNames();

    public void setAttribute(String name, Object obj);

    public void removeAttribute(String name);

    public String getServletContextName();

    /*
     * public ServletRegistration.Dynamic addServlet(
     * String servletName, String className);
     * 
     * public ServletRegistration.Dynamic addServlet(
     * String servletName, Servlet servlet);
     * 
     * public ServletRegistration.Dynamic addServlet(String servletName,
     * Class <? extends Servlet> servletClass);
     * 
     * public ServletRegistration.Dynamic addJspFile(
     * String servletName, String jspFile);
     * 
     * public ServletRegistration getServletRegistration(String servletName);
     * 
     * public Map<String, ? extends ServletRegistration> getServletRegistrations();
     * 
     * 
     * public FilterRegistration.Dynamic addFilter(
     * String filterName, String className);
     * 
     * public FilterRegistration.Dynamic addFilter(
     * String filterName, Filter filter);
     * 
     * 
     * public FilterRegistration.Dynamic addFilter(String filterName,
     * Class <? extends Filter> filterClass);
     * 
     * public <T extends Filter> T createFilter(Class<T> clazz)
     * throws ServletException;
     * 
     * public FilterRegistration getFilterRegistration(String filterName);
     * 
     * public Map<String, ? extends FilterRegistration> getFilterRegistrations();
     * 
     * public SessionCookieConfig getSessionCookieConfig();
     * 
     * public void setSessionTrackingModes(Set<SessionTrackingMode>
     * sessionTrackingModes);
     * public Set<SessionTrackingMode> getDefaultSessionTrackingModes();
     * public Set<SessionTrackingMode> getEffectiveSessionTrackingModes();
     */

    public <T extends Servlet> T createServlet(Class<T> clazz); // throws ServletException;

    public void addListener(String className);

    public <T extends EventListener> void addListener(T t);

    public void addListener(Class<? extends EventListener> listenerClass);

    public <T extends EventListener> T createListener(Class<T> clazz);// throws ServletException;
    // public JspConfigDescriptor getJspConfigDescriptor();
    // public ClassLoader getClassLoader();

    public void declareRoles(String... roleNames);

    public String getVirtualServerName();

    public int getSessionTimeout();

    public void setSessionTimeout(int sessionTimeout);

    public String getRequestCharacterEncoding();

    public void setRequestCharacterEncoding(String encoding);

    public String getResponseCharacterEncoding();

    public void setResponseCharacterEncoding(String encoding);

}
