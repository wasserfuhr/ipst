/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.VscConverterStation;
import eu.itesla_project.iidm.network.VscConverterStationAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VscConverterStationXml extends ConnectableXml<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

    static final VscConverterStationXml INSTANCE = new VscConverterStationXml();

    static final String ROOT_ELEMENT_NAME = "vscConverterStation";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(VscConverterStation cs) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(cs.isVoltageRegulatorOn()));
        XmlUtil.writeFloat("targetV", cs.getTargetV(), context.getWriter());
        XmlUtil.writeFloat("targetQ", cs.getTargetQ(), context.getWriter());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(reader, "voltageRegulatorOn");
        float targetV = XmlUtil.readOptionalFloatAttribute(reader, "targetV");
        float targetQ = XmlUtil.readOptionalFloatAttribute(reader, "targetQ");
        readNodeOrBus(adder, reader);
        VscConverterStation cs = adder
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetV(targetV)
                .setTargetQ(targetQ)
                .add();
        readPQ(null, cs.getTerminal(), reader);
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            super.readSubElements(cs, reader, endTasks);
        });
    }
}
