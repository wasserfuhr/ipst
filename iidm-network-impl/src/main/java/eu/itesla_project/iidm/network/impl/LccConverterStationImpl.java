/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.LccConverterStation;
import eu.itesla_project.iidm.network.LccFilter;
import eu.itesla_project.iidm.network.LccFilterAdder;
import eu.itesla_project.iidm.network.impl.util.Ref;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LccConverterStationImpl extends HvdcConverterStationImpl<LccConverterStation> implements LccConverterStation {

    static final String TYPE_DESCRIPTION = "lccConverterStation";

    private float powerFactor;

    private final List<LccFilterImpl> filters = new ArrayList<>();

    LccConverterStationImpl(String id, String name, Ref<? extends MultiStateObject> ref, float powerFactor) {
        super(id, name, ref);
        this.powerFactor = powerFactor;
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.LCC;
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public float getPowerFactor() {
        return powerFactor;
    }

    @Override
    public LccConverterStation setPowerFactor(float powerFactor) {
        ValidationUtil.checkPowerFactor(this, powerFactor);
        float oldValue = this.powerFactor;
        this.powerFactor = powerFactor;
        notifyUpdate("powerFactor", oldValue, powerFactor);
        return this;
    }

    List<LccFilterImpl> getFilters() {
        return filters;
    }

    private void checkFilterIndex(int index) {
        if (index < 0 || index >= filters.size()) {
            throw new RuntimeException("Bad filter index " + index);
        }
    }

    @Override
    public LccFilter getFilterAt(int index) {
        checkFilterIndex(index);
        return filters.get(index);
    }

    @Override
    public LccFilterAdder newFilter() {
        return new LccFilterAdderImpl(this);
    }

    @Override
    public int getFilterCount() {
        return filters.size();
    }

    @Override
    public void removeFilterAt(int index) {
        checkFilterIndex(index);
        filters.remove(index);
    }
}
