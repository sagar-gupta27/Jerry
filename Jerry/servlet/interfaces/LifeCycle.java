package servlet.interfaces;

public interface Lifecycle {
    String BEFORE_INIT_EVENT = "before_init";
    String INIT_EVENT = "init_event";
    String AFTER_INIT_EVENT = "after_init";

    String START_EVENT = "start";
    String BEFORE_START_EVENT = "before_start";
    String AFTER_START_EVENT = "after_start";

    String STOP_EVENT = "stop";
    String AFTER_STOP_EVENT = "after_stop";
    String BEFORE_STOP_EVENT = "before_stop";

    String AFTER_DESTROY_EVENT = "after_destroy";
    String BEFORE_DESTROY_EVENT = "before_destroy";

    String PERIODIC_EVENT = "periodic";
    String CONFIGURE_START_EVENT = "configure_start";
    String CONFIGURE_STOP_EVENT = "configure_stop";

    void addLifeCycleListener(LifecycleListener listener);

    LifecycleListener[] findLifeCycleListeners();

    void removeLifeCycleListener();

    LifecycleState getState();

    void stop() throws LifecycleException;
    void destroy() throws LifecycleException;

    void init() throws LifecycleException;

    void start() throws LifecycleException;
}
