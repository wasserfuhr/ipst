/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class HvdcLineXml extends IdentifiableXml<HvdcLine, HvdcLineAdder, Network> {

    static final HvdcLineXml INSTANCE = new HvdcLineXml();

    static final String ROOT_ELEMENT_NAME = "hvdcLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(HvdcLine identifiable) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(HvdcLine l, Network parent, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("r", l.getR(), context.getWriter());
        XmlUtil.writeFloat("nominalV", l.getNominalV(), context.getWriter());
        context.getWriter().writeAttribute("convertersMode", l.getConvertersMode().name());
        XmlUtil.writeFloat("targetP", l.getTargetP(), context.getWriter());
        XmlUtil.writeFloat("maxP", l.getMaxP(), context.getWriter());
        context.getWriter().writeAttribute("inService", Boolean.toString(l.isInService()));
        context.getWriter().writeAttribute("converterStation1", l.getConverterStation1().getId());
        context.getWriter().writeAttribute("converterStation2", l.getConverterStation2().getId());
    }

    @Override
    protected void writeSubElements(HvdcLine l, Network parent, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected HvdcLineAdder createAdder(Network n) {
        return n.newHvdcLine();
    }

    @Override
    protected HvdcLine readRootElementAttributes(HvdcLineAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float r = XmlUtil.readFloatAttribute(reader, "r");
        float nominalV = XmlUtil.readFloatAttribute(reader, "nominalV");
        HvdcLine.ConvertersMode convertersMode = HvdcLine.ConvertersMode.valueOf(reader.getAttributeValue(null, "convertersMode"));
        float targetP = XmlUtil.readFloatAttribute(reader, "targetP");
        float maxP = XmlUtil.readFloatAttribute(reader, "maxP");
        boolean inService = Boolean.parseBoolean(reader.getAttributeValue(null, "inService"));
        String converterStation1 = reader.getAttributeValue(null, "converterStation1");
        String converterStation2 = reader.getAttributeValue(null, "converterStation2");
        return adder.setR(r)
                .setNominalV(nominalV)
                .setConvertersMode(convertersMode)
                .setTargetP(targetP)
                .setMaxP(maxP)
                .setInService(inService)
                .setConverterStationId1(converterStation1)
                .setConverterStationId2(converterStation2)
                .add();
    }

    @Override
    protected void readSubElements(HvdcLine l, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            super.readSubElements(l, reader, endTasks);
        });
    }
}
