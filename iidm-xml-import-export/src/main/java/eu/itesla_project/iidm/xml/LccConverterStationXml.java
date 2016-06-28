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
public class LccConverterStationXml extends ConnectableXml<LccConverterStation, LccConverterStationAdder, VoltageLevel> {

    static final LccConverterStationXml INSTANCE = new LccConverterStationXml();

    static final String ROOT_ELEMENT_NAME = "lccConverterStation";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(LccConverterStation cs) {
        return cs.getFilterCount() > 0;
    }

    @Override
    protected void writeRootElementAttributes(LccConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("powerFactor", cs.getPowerFactor(), context.getWriter());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(LccConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        for (int i = 0; i < cs.getFilterCount(); i++) {
            LccFilter filter = cs.getFilterAt(i);
            context.getWriter().writeEmptyElement(IIDM_URI, "filter");
            XmlUtil.writeFloat("b", filter.getB(), context.getWriter());
            context.getWriter().writeAttribute("connected", Boolean.toString(filter.isConnected()));
        }
    }

    @Override
    protected LccConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newLccConverterStation();
    }

    @Override
    protected LccConverterStation readRootElementAttributes(LccConverterStationAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float powerFactor = XmlUtil.readOptionalFloatAttribute(reader, "powerFactor");
        readNodeOrBus(adder, reader);
        LccConverterStation cs = adder.setPowerFactor(powerFactor)
                .add();
        readPQ(null, cs.getTerminal(), reader);
        return cs;
    }

    @Override
    protected void readSubElements(LccConverterStation cs, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "filter":
                    float b = XmlUtil.readOptionalFloatAttribute(reader, "b");
                    boolean connected = XmlUtil.readBoolAttribute(reader, "connected");
                    cs.newFilter()
                            .setB(b)
                            .setConnected(connected)
                            .add();
                    break;

                default:
                    super.readSubElements(cs, reader, endTasks);
            }
        });
    }
}
