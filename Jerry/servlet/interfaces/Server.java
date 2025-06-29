package servlet.interfaces;

import java.io.File;

import startup.Mouselet;

public interface Server extends Lifecycle {
    
    void await();

    int getPort();

    // set port to listen for shutdown command
    void setPort();

    // Number that offsets the port for shutdown command
    // if port is 8005 , offset 1000 , server listens at 9005
    int getPortOffset();

    void setPortOffset();

    // Actual port on which server is listening for shutdown command
    int getPortWithOffset();

    // Address on which we listen for shutdown command
    String getAddress();

    void setAddress();

    // Shutdown command string
    String getShutdown();

    void setShutdown(String shutdown);

    /**
     * @return the parent class loader for this component. If not set, return
     *         {@link #getMouselet()}
     *         {@link Mouselet#getParentClassLoader()}. If catalina has not been
     *         set, return the system class
     *         loader.
     */
    ClassLoader getParentClassLoader();

    /**
     * Set the parent class loader for this server.
     *
     * @param parent The new parent class loader
     */
    void setParentClassLoader(ClassLoader parent);

    /**
     * @return the outer Mouselet startup/shutdown component if present.
     */
    Mouselet getMouselet();

    /**
     * Set the outer Mouselet startup/shutdown component if present.
     *
     * @param mouselet the outer mouselet component
     */
    void setMouselet(Mouselet mouselet);

    /**
     * @return the configured base (instance) directory. Note that home and base may
     *         be the same (and are by default).
     *         If this is not set the value returned by {@link #getMouseletHome()}
     *         will
     *         be used.
     */
    File getMouseletBase();

    /**
     * Set the configured base (instance) directory. Note that home and base may be
     * the same (and are by default).
     *
     * @param MouseletBase the configured base directory
     */
    void setMouseletBase(File jerryBase);

    /**
     * @return the configured home (binary) directory. Note that home and base may
     *         be the same (and are by default).
     */
    File getMouseletHome();

    /**
     * Set the configured home (binary) directory. Note that home and base may be
     * the same (and are by default).
     *
     * @param mouseletHome the configured home directory
     */
    void setMouseletHome(File mouseletHome);

    /**
     * Get the utility thread count.
     *
     * @return the thread count
     */
    int getUtilityThreads();

    /**
     * Set the utility thread count.
     *
     * @param utilityThreads the new thread count
     */
    void setUtilityThreads(int utilityThreads);

}
