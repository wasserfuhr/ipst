/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * LCC converter station builder and adder.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public interface LccConverterStationAdder extends SingleTerminalConnectableAdder<LccConverterStationAdder> {

    interface FilterAdder {

        FilterAdder setB(float b);

        FilterAdder setConnected(boolean connected);

        LccConverterStationAdder endFilter();
    }

    LccConverterStationAdder setPowerFactor(float powerFactor);

    FilterAdder beginFilter();

    LccConverterStation add();
}