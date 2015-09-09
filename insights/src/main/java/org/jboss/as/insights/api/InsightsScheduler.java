/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.as.insights.api;

import org.jboss.msc.service.ServiceName;

/**
 * Scanner for Insights
 *
 * Derived from org.jboss.as.server.deployment.scanner.api.DeploymentScanner
 *
 * @author <a href="mailto:jkinlaw@redhat.com">Josh Kinlaw</a>
 */
public interface InsightsScheduler {

    ServiceName BASE_SERVICE_NAME = ServiceName.JBOSS.append("server",
            "deployment", "scanner");

    /**
     * Check whether the scheduler is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Get the current schedule interval
     *
     * @return the scan interval in ms
     */
    long getScheduleInterval();

    /**
     * Set the schedule interval.
     *
     * @param scanInterval
     *            the scan interval in ms
     */
    void setScheduleInterval(long scanInterval);

    /**
     * Start the scanner, if not already started, using a default
     * {@link DeploymentOperations}.
     *
     * @see #startScanner(DeploymentOperations)
     */
    void startScheduler();

    /**
     * Stop the scheduler, if not already stopped.
     */
    void stopScheduler();

    /**
     * set the rhnUid
     */
    void setRhnUid(String rhnUid);

    /**
     * set the rhnPw
     */
    void setRhnPw(String rhnPw);

    /**
     * set the proxy URL
     */
    void setProxyUrl(String proxyUrl);

    /**
     * set the proxy port
     */
    void setProxyPort(String proxyPort);

    /**
     * set the proxy user
     */
    void setProxyUser(String proxyUser);

    /**
     * set the proxy password
     */
    void setProxyPw(String proxyPw);

    /**
     * set the url
     */
    void setUrl(String url);

    /**
     * set the insights endpoint
     * @param insightsEndpoint
     */
    void setInsightsEndpoint(String insightsEndpoint);

    /**
     * set the system endpoint
     * @param systemEndpoint
     */
    void setSystemEndpoint(String systemEndpoint);

    /**
     * set the user agent
     * @param userAgent
     */
    void setUserAgent(String userAgent);
}