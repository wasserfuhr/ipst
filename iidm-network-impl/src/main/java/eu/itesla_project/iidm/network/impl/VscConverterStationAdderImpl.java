/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.VscConverterStationAdder;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VscConverterStationAdderImpl extends HvdcConverterStationAdderImpl<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private final VoltageLevelExt voltageLevel;

    private Boolean voltageRegulatorOn;

    private float targetQ = Float.NaN;

    private float targetV = Float.NaN;

    VscConverterStationAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "vscConverterStation";
    }

    @Override
    public VscConverterStationAdderImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setTargetV(float targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setTargetQ(float targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public VscConverterStationImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal(id);
        ValidationUtil.checkConverterMode(this, converterMode);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, targetQ);
        VscConverterStationImpl converterStation
                = new VscConverterStationImpl(id, getName(), getNetwork().getRef(), converterMode, voltageRegulatorOn, targetQ, targetV);
        converterStation.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(converterStation);
        getNetwork().getListeners().notifyCreation(converterStation);
        return converterStation;
    }

}
