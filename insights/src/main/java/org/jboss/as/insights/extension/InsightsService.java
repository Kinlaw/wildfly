/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.insights.extension;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.as.controller.services.path.PathManagerService;
import org.jboss.as.insights.api.InsightsScheduler;
import org.jboss.as.jdr.JdrReportCollector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.redhat.gss.redhat_support_lib.api.API;

import static org.jboss.as.insights.logger.InsightsLogger.ROOT_LOGGER;

/**
 * @author <a href="mailto:jkinlaw@redhat.com">Josh Kinlaw</a>
 */
public class InsightsService implements Service<InsightsScheduler> {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    public static final String PROXY_USER = "proxyUser";
    public static final String PROXY_PASSWORD = "proxyPassword";
    public static final String PROXY_URL = "proxyUrl";
    public static final String PROXY_PORT = "proxyPort";
    public static final String INSIGHTS_ENDPOINT = "insightsEndpoint";
    public static final String SYSTEM_ENDPOINT = "systemEndpoint";
    public static final String USER_AGENT = "userAgent";

    public static final String JBOSS_PROPERTY_DIR = "jboss.server.data.dir";

    public static final String INSIGHTS_PROPERTY_FILE_NAME = "insights.properties";

    public static final String INSIGHTS_DESCRIPTION = "Properties file consisting of RHN login information and insights/insights URL";

    public static final int DEFAULT_SCHEDULE_INTERVAL = 1;
    public static final String DEFAULT_BASE_URL = "https://api.access.redhat.com";
    public static final String DEFAULT_INSIGHTS_ENDPOINT = "/r/insights/v1/uploads/";
    public static final String DEFAULT_SYSTEM_ENDPOINT = "/r/insights/v1/systems/";
    public static final String DEFAULT_USER_AGENT = "redhat-support-lib-java";

    private InsightsJdrScheduler scheduler;

    /**
     * Endpoint of url which when added to the end of the url reveals the
     * location of where to send the JDR Should take the format of
     * /insights/endpoint/
     */
    private String insightsEndpoint;

    /**
     * Endponit of url which when added to the end of the url reveals the
     * location of where to query for the current system uuid
     */
    private String systemEndpoint;

    private boolean enabled = true;

    private long scheduleInterval = 1;

    private String rhnUid;

    private String rhnPw;

    private String proxyUrl;

    private String proxyPort;

    private String proxyUser;

    private String proxyPw;

    private String url;

    private String userAgent;

    private JdrReportCollector jdrCollector;

    private API api;

    private final ServiceTarget serviceTarget;

    public ServiceController<InsightsScheduler> addScheduler() {
        System.out.println("addScheduler");
        final ServiceName serviceName = InsightsScheduler.BASE_SERVICE_NAME;
        ServiceBuilder<InsightsScheduler> builder = serviceTarget.addService(
                serviceName, this).addDependency(
                PathManagerService.SERVICE_NAME);
        ServiceController<InsightsScheduler> svc = builder.setInitialMode(
                Mode.NEVER).install();
        System.out.println("addScheduler finished");
        return svc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void start(StartContext context) throws StartException {
        System.out.println("start");
        try {
            // if this is the first start we want to use the same scheduler that
            // was used at boot time
            if (scheduler == null) {
                final InsightsJdrScheduler scheduler = new InsightsJdrScheduler(
                        scheduleInterval, rhnUid, rhnPw, proxyUrl, proxyPort,
                        proxyUser, proxyPw, url, userAgent, systemEndpoint,
                        insightsEndpoint, jdrCollector);
                this.scheduler = scheduler;
            }
            if (enabled) {
                scheduler.startScheduler();
            }
        } catch (Exception e) {
            throw new StartException(e);
        }
        System.out.println("start finished");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stop(StopContext context) {
        final InsightsJdrScheduler scheduler = this.scheduler;
        this.scheduler = null;
        scheduler.stopScheduler();
    }

    private InsightsService(final ServiceTarget serviceTarget,
            long scheduleInterval, boolean enabled, String insightsEndpoint,
            String systemEndpoint, String url, String userAgent) {
        this.serviceTarget = serviceTarget;
        this.scheduleInterval = scheduleInterval;
        this.enabled = enabled;
        this.insightsEndpoint = insightsEndpoint;
        this.systemEndpoint = systemEndpoint;
        this.url = url;
        this.userAgent = userAgent;
        addScheduler();
    }

    public static InsightsService getInstance(ServiceTarget serviceTarget,
            long scheduleInterval, boolean enabled, String insightsEndpoint,
            String systemEndpoint, String url, String userAgent) {
        InsightsService instance = new InsightsService(serviceTarget,
                scheduleInterval, enabled, insightsEndpoint, systemEndpoint,
                url, userAgent);
        return instance;
    }

    @Override
    public InsightsScheduler getValue() throws IllegalStateException,
            IllegalArgumentException {
        return scheduler;
    }

    void setFrequency(long scheduleInterval) {
        ROOT_LOGGER.scheduleIntervalUpdated("" + scheduleInterval);
        this.scheduleInterval = scheduleInterval;
    }

    public long getFrequency() {
        return this.scheduleInterval;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            ROOT_LOGGER.insightsEnabled();
            try {
                start(null);
            } catch (StartException e) {
                ROOT_LOGGER.couldNotStartThread(e);
            }
        } else if (!enabled) {
            ROOT_LOGGER.insightsDisabled();
        }
    }

    public void setJdrReportCollector(JdrReportCollector jdrCollector) {
        this.jdrCollector = jdrCollector;
    }

    public void setRhnUid(String rhnUid) {
        this.rhnUid = rhnUid;
        scheduler.setRhnUid(rhnUid);
    }

    public void setRhnPw(String rhnPw) {
        this.rhnPw = rhnPw;
        scheduler.setRhnPw(rhnPw);
    }

    public String getRhnUid() {
        return rhnUid;
    }

    public String getRhnPw() {
        return rhnPw;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        scheduler.setUrl(url);
    }

    public void setInsightsEndpoint(String insightsEndpoint) {
        this.insightsEndpoint = insightsEndpoint;
        scheduler.setInsightsEndpoint(insightsEndpoint);
    }

    public void setSystemEndpoint(String systemEndpoint) {
        this.systemEndpoint = systemEndpoint;
        scheduler.setSystemEndpoint(systemEndpoint);
    }

    public String getInsightsEndpoint() {
        return insightsEndpoint;
    }

    public String getSystemEndpoint() {
        return systemEndpoint;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        scheduler.setUserAgent(userAgent);
    }

    public String getUserAgent() {
        return userAgent;
    }

    private void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
        scheduler.setProxyPort(proxyPort);
    }

    private void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
        scheduler.setProxyUrl(proxyUrl);
    }

    private String getProxyPort() {
        return proxyPort;
    }

    private String getProxyUrl() {
        return proxyUrl;
    }

    private void setProxyPw(String proxyPw) {
        this.proxyPw = proxyPw;
        scheduler.setProxyPw(proxyPw);
    }

    private void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
        scheduler.setProxyUser(proxyUser);
    }

    private String getProxyPw() {
        return proxyPw;
    }

    private String getProxyUser() {
        return proxyUser;
    }

    public static ServiceName createServiceName() {
        return ServiceName.JBOSS
                .append(org.jboss.as.insights.extension.InsightsExtension.SUBSYSTEM_NAME);
    }

    /**
     * Set RHN login credentials and write to insights config file
     *
     * @param rhnUid
     * @param rhnPw
     */
    public void setRhnLoginCredentials(String rhnUid, String rhnPw) {
        setRhnUid(rhnUid);
        setRhnPw(rhnPw);
        setConnectionManager();
    }

    public void setRhnLoginCredentials(String rhnUid, String rhnPw,
            String proxyUrl, String proxyPort) {
        setProxyUrl(proxyUrl);
        setProxyPort(proxyPort);
        setRhnLoginCredentials(rhnUid, rhnPw);
    }

    public void setRhnLoginCredentials(String rhnUid, String rhnPw,
            String proxyUrl, String proxyPort, String proxyUser, String proxyPw) {
        setProxyUser(proxyUser);
        setProxyPw(proxyPw);
        setRhnLoginCredentials(rhnUid, rhnPw, proxyUrl, proxyPort);
    }

    public void setConnectionManager() {
        int proxyPortInt = -1;
        URL proxyUrlUrl = null;
        try {
            proxyPortInt = Integer.parseInt(proxyPort);
        } catch (NumberFormatException e) {
            proxyPortInt = -1;
        }
        try {
            proxyUrlUrl = new URL(proxyUrl);
        } catch (MalformedURLException e) {
            proxyUrlUrl = null;
        }
        if (rhnUid != null && rhnPw != null) {
            api = new API(rhnUid, rhnPw, url, proxyUser, proxyPw, proxyUrlUrl,
                    proxyPortInt, userAgent);
        }
    }
}