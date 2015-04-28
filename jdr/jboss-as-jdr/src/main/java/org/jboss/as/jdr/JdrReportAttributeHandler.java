package org.jboss.as.jdr;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

public class JdrReportAttributeHandler extends AbstractWriteAttributeHandler<Void> {

    public static final JdrReportAttributeHandler INSTANCE = new JdrReportAttributeHandler();

    private JdrReportAttributeHandler() {
        super(JdrReportSubsystemDefinition.UUID);
    }

    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
            ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder) throws OperationFailedException {
        System.out.println("AttrHandler - attributeName, resolvedValue: " + attributeName + ", " + resolvedValue);
        if (attributeName.equals(JdrReportExtension.UUID)) {
            JdrReportService service = (JdrReportService) context.getServiceRegistry(true).getRequiredService(JdrReportService.createServiceName()).getValue();
            service.setUuid(resolvedValue.asString());
            resolvedValue.set(service.getUuid());
            context.completeStep(OperationContext.ResultHandler.NOOP_RESULT_HANDLER);
        }
        return false;
    }

    /**
     * Hook to allow subclasses to revert runtime changes made in
     * {@link #applyUpdateToRuntime(OperationContext, ModelNode, String, ModelNode, ModelNode, HandbackHolder)}.
     *
     * @param context        the context of the operation
     * @param operation      the operation
     * @param attributeName  the name of the attribute being modified
     * @param valueToRestore the previous value for the attribute, before this operation was executed
     * @param valueToRevert  the new value for the attribute that should be reverted
     * @param handback       an object, if any, passed in to the {@code handbackHolder} by the {@code applyUpdateToRuntime}
     *                       implementation
     */
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
          ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        // no-op
    }
}