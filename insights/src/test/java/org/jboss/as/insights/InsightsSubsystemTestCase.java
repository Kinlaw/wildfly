package org.jboss.as.insights;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.insights.extension.InsightsExtension;
import org.jboss.as.model.test.ModelTestControllerVersion;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.as.subsystem.test.KernelServicesBuilder;
import org.junit.Assert;
import org.junit.Test;

public class InsightsSubsystemTestCase extends AbstractSubsystemBaseTest {

    public InsightsSubsystemTestCase() {
        super(InsightsExtension.SUBSYSTEM_NAME, new InsightsExtension());
    }
    
    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("insights-1.0.xml");
    }
    
    @Override
    protected String getSubsystemXsdPath() throws Exception {
        return "schema/jboss-eap-insights_1_0.xsd";
    }
    
    @Test
    public void testRuntime(ModelTestControllerVersion controllerVersion, ModelVersion modelVersion) throws Exception {
        String subsystemXml = "insights-1.0.xml";
        //Use the non-runtime version of the extension which will happen on the HC
        KernelServicesBuilder builder = createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT)
                .setSubsystemXmlResource(subsystemXml);

        // Add legacy subsystems
        builder.createLegacyKernelServicesBuilder(null, controllerVersion, modelVersion)
                .addMavenResourceURL("org.jboss.as:jboss-as-jdr:" + controllerVersion.getMavenGavVersion());

        KernelServices mainServices = builder.build();
        KernelServices legacyServices = mainServices.getLegacyServices(modelVersion);
        Assert.assertNotNull(mainServices);
        Assert.assertNotNull(legacyServices);
        checkSubsystemModelTransformation(mainServices, modelVersion);
    }

    @Override
    protected AdditionalInitialization createAdditionalInitialization() {
        return new AdditionalInitialization() {
            @Override
            protected RunningMode getRunningMode() {
                return RunningMode.NORMAL;
            }
        };
    }

    protected static final OptionAttributeDefinition ENABLED_PROTOCOLS = OptionAttributeDefinition.builder("enabled-protocols", Options.SSL_ENABLED_PROTOCOLS)
            .setAllowNull(true)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setAllowExpression(true)
            .build();

    @Test
    public void testSequence() throws Exception {
        OptionMap.Builder builder = OptionMap.builder();
        ModelNode model = new ModelNode();
        ModelNode operation = new ModelNode();
        operation.get(ENABLED_PROTOCOLS.getName()).set("TLSv1, TLSv1.1, TLSv1.2");
        ENABLED_PROTOCOLS.validateAndSet(operation, model);
        ENABLED_PROTOCOLS.resolveOption(ExpressionResolver.SIMPLE, model, builder);
        Sequence<String> protocols = builder.getMap().get(Options.SSL_ENABLED_PROTOCOLS);
        Assert.assertEquals(3, protocols.size());
        Assert.assertEquals("TLSv1", protocols.get(0));
        Assert.assertEquals("TLSv1.1", protocols.get(1));
        Assert.assertEquals("TLSv1.2", protocols.get(2));
    }
}
