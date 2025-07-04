package servlet.util.digester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import jerry.ExceptionUtils;
import logging.Log;
import logging.LogFactory;
import servlet.util.IntrospectionUtils;
import servlet.util.IntrospectionUtils.PropertySource;
import servlet.util.StringManager;

public class Digester extends DefaultHandler2 {
    protected static IntrospectionUtils.PropertySource[] propertySources;
    private static boolean propertySourcesSet = false;
    protected static final StringManager sm = StringManager.getManager(Digester.class);
    protected IntrospectionUtils.PropertySource[] source;

    static {
        String classNames = System.getProperty("org.apache.tomcat.util.digester.PROPERTY_SOURCE");
        ArrayList<IntrospectionUtils.PropertySource> sourcesList = new ArrayList<>();
        IntrospectionUtils.PropertySource[] sources = null;
        if (classNames != null) {
            StringTokenizer classNamesTokenizer = new StringTokenizer(classNames, ",");
            while (classNamesTokenizer.hasMoreTokens()) {
                String className = classNamesTokenizer.nextToken().trim();
                ClassLoader[] cls = new ClassLoader[] { Digester.class.getClassLoader(),
                        Thread.currentThread().getContextClassLoader() };
                for (ClassLoader cl : cls) {
                    try {
                        Class<?> clazz = Class.forName(className, true, cl);
                        sourcesList.add((PropertySource) clazz.getConstructor().newInstance());
                        break;
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        LogFactory.getLog(Digester.class)
                                .error(sm.getString("digester.propertySourceLoadError", className), t);
                    }
                }
            }
            sources = sourcesList.toArray(new IntrospectionUtils.PropertySource[0]);
        }
        if (sources != null) {
            propertySources = sources;
            propertySourcesSet = true;
        }
        if (Boolean.getBoolean("org.apache.tomcat.util.digester.REPLACE_SYSTEM_PROPERTIES")) {
            replaceSystemProperties();
        }
    }

    public static void replaceSystemProperties() {
        Log log = LogFactory.getLog(Digester.class);
        if (propertySources != null) {
            Properties properties = System.getProperties();
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                String value = System.getProperty(name);
                if (value != null) {
                    try {
                        String newValue = IntrospectionUtils.replaceProperties(value, null, propertySources, null);
                        if (!value.equals(newValue)) {
                            System.setProperty(name, newValue);
                        }
                    } catch (Exception e) {
                        log.warn(sm.getString("digester.failedToUpdateSystemProperty", name, value), e);
                    }
                }
            }
        }
    }

    public static void setPropertySource(IntrospectionUtils.PropertySource propertySource) {
        if (!propertySourcesSet) {
            propertySources = new IntrospectionUtils.PropertySource[1];
            propertySources[0] = propertySource;
            propertySourcesSet = true;
        }
    }

    public static void setPropertySource(IntrospectionUtils.PropertySource[] propertySources) {
        if (!propertySourcesSet) {
            Digester.propertySources = propertySources;
            propertySourcesSet = true;
        }
    }

    private static final HashSet<String> generatedClasses = new HashSet<>();

    public static void addGeneratedClass(String className) {
        generatedClasses.add(className);
    }

    private static GeneratedCodeLoader generatedCodeLoader;

    public static boolean isGeneratedCodeLoaderSet() {
        return generatedCodeLoader != null;
    }

    public static void setGeneratedCodeLoader(GeneratedCodeLoader generatedCodeLoader) {
        if (Digester.generatedCodeLoader == null) {
            Digester.generatedCodeLoader = generatedCodeLoader;
        }
    }
    public static String[] getGeneratedClasses() {
        return generatedClasses.toArray(new String[0]);
    }

    public interface GeneratedCodeLoader {
        Object loadGeneratedCode(String className);
    }

    public static Object loadGeneratedClass(String className) {
        if (generatedCodeLoader != null) {
            return generatedCodeLoader.loadGeneratedCode(className);
        }
        return null;
    }


     /**
     * The body text of the current element.
     */
    protected StringBuilder bodyText = new StringBuilder();


    /**
     * The stack of body text string buffers for surrounding elements.
     */
    protected ArrayStack<StringBuilder> bodyTexts = new ArrayStack<>();


    /**
     * Stack whose elements are List objects, each containing a list of Rule objects as returned from Rules.getMatch().
     * As each xml element in the input is entered, the matching rules are pushed onto this stack. After the end tag is
     * reached, the matches are popped again. The depth of is stack is therefore exactly the same as the current
     * "nesting" level of the input xml.
     *
     * @since 1.6
     */
    protected ArrayStack<List<Rule>> matches = new ArrayStack<>(10);

    /**
     * The class loader to use for instantiating application objects. If not specified, the context class loader, or the
     * class loader used to load Digester itself, is used, based on the value of the <code>useContextClassLoader</code>
     * variable.
     */
    protected ClassLoader classLoader = null;


    /**
     * Has this Digester been configured yet.
     */
    protected boolean configured = false;


    /**
     * The EntityResolver used by the SAX parser. By default, it uses this class
     */
    protected EntityResolver entityResolver;

    /**
     * The URLs of entityValidator that have been registered, keyed by the public identifier that corresponds.
     */
    protected HashMap<String,String> entityValidator = new HashMap<>();


    /**
     * The application-supplied error handler that is notified when parsing warnings, errors, or fatal errors occur.
     */
    protected ErrorHandler errorHandler = null;


    /**
     * The SAXParserFactory that is created the first time we need it.
     */
    protected SAXParserFactory factory = null;

    /**
     * The Locator associated with our parser.
     */
    protected Locator locator = null;


    /**
     * The current match pattern for nested element processing.
     */
    protected String match = "";


    /**
     * Do we want a "namespace aware" parser.
     */
    protected boolean namespaceAware = false;


    /**
     * Registered namespaces we are currently processing. The key is the namespace prefix that was declared in the
     * document. The value is an ArrayStack of the namespace URIs this prefix has been mapped to -- the top Stack
     * element is the most current one. (This architecture is required because documents can declare nested uses of the
     * same prefix for different Namespace URIs).
     */
    protected HashMap<String,ArrayStack<String>> namespaces = new HashMap<>();


    /**
     * The parameters stack being utilized by CallMethodRule and CallParamRule rules.
     */
    protected ArrayStack<Object> params = new ArrayStack<>();


    /**
     * The SAXParser we will use to parse the input stream.
     */
    protected SAXParser parser = null;


    /**
     * The public identifier of the DTD we are currently parsing under (if any).
     */
    protected String publicId = null;


    /**
     * The XMLReader used to parse digester rules.
     */
    protected XMLReader reader = null;


    /**
     * The "root" element of the stack (in other words, the last object that was popped).
     */
    protected Object root = null;


    /**
     * The <code>Rules</code> implementation containing our collection of <code>Rule</code> instances and associated
     * matching policy. If not established before the first rule is added, a default implementation will be provided.
     */
    protected Rules rules = null;

    /**
     * The object stack being constructed.
     */
    protected ArrayStack<Object> stack = new ArrayStack<>();


    /**
     * Do we want to use the Context ClassLoader when loading classes for instantiating new objects. Default is
     * <code>false</code>.
     */
    protected boolean useContextClassLoader = false;


    /**
     * Do we want to use a validating parser.
     */
    protected boolean validating = false;


    /**
     * Warn on missing attributes and elements.
     */
    protected boolean rulesValidation = false;


    /**
     * Fake attributes map (attributes are often used for object creation).
     */
    protected Map<Class<?>,List<String>> fakeAttributes = null;


    /**
     * The Log to which most logging calls will be made.
     */
    protected Log log = LogFactory.getLog(Digester.class);

    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    protected Log saxLog = LogFactory.getLog("org.apache.tomcat.util.digester.Digester.sax");

    /**
     * Generated code.
     */
    protected StringBuilder code = null;

}
