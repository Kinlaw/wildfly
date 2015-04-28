/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.jdr;


//import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
//import org.jboss.msc.service.ServiceName;
//import org.jboss.msc.service.ServiceRegistry;
//import org.jboss.msc.service.ServiceController.Mode;

/**
 * Adds the JDR subsystem.
 *
 * @author Brian Stansberry
 */
//public class JdrReportSubsystemAdd extends AbstractAddStepHandler {
//    static final JdrReportSubsystemAdd INSTANCE = new JdrReportSubsystemAdd();
//
//    private JdrReportSubsystemAdd() {
//    }
//
//    @Override
//    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
//        JdrReportSubsystemDefinition.UUID.validateAndSet(operation, model);
//        JdrReportService.addService(context.getServiceTarget());
//    }
//}
class JdrReportSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final JdrReportSubsystemAdd INSTANCE = new JdrReportSubsystemAdd();

    private JdrReportSubsystemAdd() {
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        System.out.println("JdrSubsystemAdd - populateModel");
        JdrReportSubsystemDefinition.UUID.validateAndSet(operation, model);
        System.out.println("JdrSubsystemAdd - populateModel finish");
    }

    /** {@inheritDoc} */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model)
            throws OperationFailedException {
        System.out.println("JdrSubAdd - performBoottime");
        JdrReportService.addService(context.getServiceTarget());
        System.out.println("JdrSubAdd - performBoottime finish");
    }
}