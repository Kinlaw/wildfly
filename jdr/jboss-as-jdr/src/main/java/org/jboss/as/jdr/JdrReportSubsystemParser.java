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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;
import java.util.UUID;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public final class JdrReportSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    static final JdrReportSubsystemParser INSTANCE = new JdrReportSubsystemParser();

    public static JdrReportSubsystemParser getInstance() {
        return INSTANCE;
    }

    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        System.out.println("Parser - readElement");
        ParseUtils.requireNoAttributes(reader);
//        ParseUtils.requireNoContent(reader);

        //Read the child
        System.out.println("Parser - reader.attrCount: " + reader.getAttributeCount());
        if (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            readProperties(reader, list);
            //Finishes reading end tags
            while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                readProperties(reader, list);
            }
        }
        else {
            System.out.println("Parser: else");
            final ModelNode subsystem = new ModelNode();
            subsystem.get(OP).set(ModelDescriptionConstants.ADD);
            JdrReportSubsystemDefinition.UUID.parseAndSetParameter(UUID.randomUUID().toString(), subsystem, reader);
            System.out.println("set UUID to SET_UUID");
            PathAddress addr = PathAddress.pathAddress(JdrReportExtension.SUBSYSTEM_PATH);
            subsystem.get(OP_ADDR).set(addr.toModelNode());
            System.out.println(subsystem);
            list.add(subsystem);
        }
    }

    /**
     * Gathers properties for the JDR
     *
     * @param reader
     * @param list subsystem list
     * @throws XMLStreamException
     */
    private void readProperties(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        System.out.println("Extension element: " + reader.getLocalName());
        if (!reader.getLocalName().equals(JdrReportExtension.JDR_PROPERTIES)) {
            throw ParseUtils.unexpectedElement(reader);
        }
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ModelDescriptionConstants.ADD);

        System.out.println("Extension - attributeCount: " + reader.getAttributeCount());
        if(reader.getAttributeCount() > 0) {
            String attr = reader.getAttributeLocalName(0);
            String value = reader.getAttributeValue(0);
            System.out.println("Extension - attr, value: " + attr + ", " + value);
            if (attr.equals(JdrReportExtension.UUID)) {
                JdrReportSubsystemDefinition.UUID.parseAndSetParameter(value, subsystem, reader);
                System.out.println("parseAndSet");
            }
            else {
                throw ParseUtils.unexpectedAttribute(reader, 0);
            }
        }
        else {
            JdrReportSubsystemDefinition.UUID.parseAndSetParameter(UUID.randomUUID().toString(), subsystem, reader);
        }
        ParseUtils.requireNoContent(reader);

        //Add the 'add' operation
        PathAddress addr = PathAddress.pathAddress(JdrReportExtension.SUBSYSTEM_PATH);
        subsystem.get(OP_ADDR).set(addr.toModelNode());
        list.add(subsystem);
        System.out.println("adding subsystem");
    }

    /**
     * {@inheritDoc}
     */
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context)
            throws XMLStreamException {
        System.out.println("JdrParser - writeContent - context: " + context);
        context.startSubsystemElement(org.jboss.as.jdr.Namespace.CURRENT.getUriString(), false);
        ModelNode node = context.getModelNode();
        writer.writeStartElement(JdrReportExtension.JDR_PROPERTIES);
        JdrReportSubsystemDefinition.UUID.marshallAsAttribute(node, true, writer);
        writer.writeEndElement();
        writer.writeEndElement();
        System.out.println("TeleExtension - wrote by " + writer);
    }
}