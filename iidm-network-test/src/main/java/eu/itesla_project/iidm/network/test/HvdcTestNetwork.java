/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.test;

import eu.itesla_project.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HvdcTestNetwork {

    private HvdcTestNetwork() {
    }

    private static Network createBase() {
        Network network = NetworkFactory.create("vsctest", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T16:34:55.930+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        return network;
    }

    private static void createLine(Network network) {
        network.newHvdcLine()
                .setId("L")
                .setConverterStationId1("C1")
                .setConverterStationId2("C2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300)
                .setTargetP(280)
                .setInService(true)
                .add();
    }

    public static Network createVsc() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.newVscConverterStation()
                .setId("C1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setTargetV(405)
                .setVoltageRegulatorOn(true)
                .add();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.newVscConverterStation()
                .setId("C2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setTargetQ(123)
                .setVoltageRegulatorOn(false)
                .add();
        createLine(network);
        return network;
    }

    public static Network createLcc() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        LccConverterStation cs1 = vl1.newLccConverterStation()
                .setId("C1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setPowerFactor(0.5f)
                .add();
        cs1.newFilter()
                .setB(0.00001f)
                .setConnected(true)
                .add();
        cs1.newFilter()
                .setB(0.00002f)
                .setConnected(false)
                .add();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        LccConverterStation cs2 = vl2.newLccConverterStation()
                .setId("C2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setPowerFactor(0.6f)
                .add();
        cs2.newFilter()
                .setB(0.00003f)
                .setConnected(true)
                .add();
        cs2.newFilter()
                .setB(0.00004f)
                .setConnected(true)
                .add();
        createLine(network);
        return network;
    }
}
