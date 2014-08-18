package org.github.fujohnwang;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author FuqiangWang
 * @since 2014-08-18
 */
public class Shutdown implements ShutdownMBean {

    protected String domain;

    protected long shutdownCheckInterval = 10L;

    public Shutdown() {
        this("com.github.fujohnwang.shutdown");
    }

    public Shutdown(String domain) {
        this.domain = domain;
    }

    protected AtomicBoolean running = new AtomicBoolean(false);

    public void await() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        if (running.get()) return;

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(this, new ObjectName(domain, "name", "ShutdownHook"));
        running.compareAndSet(false, true);

        while (running.get()) {
            TimeUnit.SECONDS.sleep(shutdownCheckInterval);
        }
    }

    @Override
    public void shutdown() {
        if (!running.get()) return;
        running.compareAndSet(true, false);
    }

    public long getShutdownCheckInterval() {
        return shutdownCheckInterval;
    }

    public void setShutdownCheckInterval(long shutdownCheckInterval) {
        this.shutdownCheckInterval = shutdownCheckInterval;
    }

    public static void main(String[] args) throws Throwable {
        Shutdown shutdown = new Shutdown();
        shutdown.await(); // busy loop to prevent process to exit
    }
}


