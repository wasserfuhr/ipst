/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.LccFilterAdder;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LccFilterAdderImpl implements LccFilterAdder {

    private final LccConverterStationImpl converterStation;

    private float b = Float.NaN;

    private Boolean connected;

    public LccFilterAdderImpl(LccConverterStationImpl converterStation) {
        this.converterStation = Objects.requireNonNull(converterStation);
    }

    @Override
    public LccFilterAdder setB(float b) {
        this.b = b;
        return this;
    }

    @Override
    public LccFilterAdder setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }

    @Override
    public LccFilterImpl add() {
        LccFilterImpl filter = new LccFilterImpl(b, connected);
        converterStation.getFilters().add(filter);
        return filter;
    }
}
