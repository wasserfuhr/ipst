/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.LccConverterStation;
import eu.itesla_project.iidm.network.impl.util.Ref;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LccConverterStationImpl extends HvdcConverterStationImpl<LccConverterStation> implements LccConverterStation {

    static class FilterImpl implements Filter {

        private float b;

        private boolean connected;

        @Override
        public float getB() {
            return b;
        }

        @Override
        public Filter setB(float b) {
            this.b = b;
            return this;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public Filter setConnected(boolean connected) {
            this.connected = connected;
            return this;
        }
    }

    private float powerFactor;

    private final List<Filter> filters = new ArrayList<>();

    LccConverterStationImpl(String id, String name, Ref<? extends MultiStateObject> ref, ConverterMode converterMode, float powerFactor) {
        super(id, name, ref, converterMode);
        this.powerFactor = powerFactor;
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.LCC;
    }

    @Override
    protected String getTypeDescription() {
        return "lccConverterStation";
    }

    @Override
    public float getPowerFactor() {
        return powerFactor;
    }

    @Override
    public LccConverterStation setPowerFactor(float powerFactor) {
        float oldValue = this.powerFactor;
        this.powerFactor = powerFactor;
        notifyUpdate("powerFactor", oldValue, powerFactor);
        return this;
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }
}
