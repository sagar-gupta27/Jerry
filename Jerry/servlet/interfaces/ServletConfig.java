package servlet.interfaces;
import java.util.Enumeration;

public interface ServletConfig {
 public String getServletName();
 public String getInitParameter(String name);
 public Enumeration<String> getInitParameter();
}
