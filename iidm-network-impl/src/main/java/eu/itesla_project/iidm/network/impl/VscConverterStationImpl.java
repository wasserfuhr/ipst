/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.VscConverterStation;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;

import java.util.BitSet;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VscConverterStationImpl extends HvdcConverterStationImpl<VscConverterStation> implements VscConverterStation {

    static final String TYPE_DESCRIPTION = "vscConverterStation";

    private final BitSet voltageRegulatorOn;

    private final TFloatArrayList targetQ;

    private final TFloatArrayList targetV;

    VscConverterStationImpl(String id, String name, Ref<? extends MultiStateObject> ref, boolean voltageRegulatorOn,
                            float targetQ, float targetV) {
        super(id, name, ref);
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageRegulatorOn = new BitSet(stateArraySize);
        this.targetQ = new TFloatArrayList(stateArraySize);
        this.targetV = new TFloatArrayList(stateArraySize);
        this.voltageRegulatorOn.set(0, stateArraySize, voltageRegulatorOn);
        for (int i = 0; i < stateArraySize; i++) {
            this.targetQ.add(targetQ);
            this.targetV.add(targetV);
        }
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.VSC;
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(stateIndex), targetQ.get(stateIndex));
        boolean oldValue = this.voltageRegulatorOn.get(stateIndex);
        this.voltageRegulatorOn.set(stateIndex, voltageRegulatorOn);
        notifyUpdate("voltageRegulatorOn", oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public float getTargetV() {
        return this.targetV.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setTargetV(float targetV) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV, targetQ.get(stateIndex));
        float oldValue = this.targetV.set(stateIndex, targetV);
        notifyUpdate("targetV", oldValue, targetV);
        return this;
    }

    @Override
    public float getTargetQ() {
        return targetQ.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setTargetQ(float targetQ) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV.get(stateIndex), targetQ);
        float oldValue = this.targetQ.set(stateIndex, targetQ);
        notifyUpdate("targetQ", oldValue, targetQ);
        return this;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        targetQ.ensureCapacity(targetQ.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; i++) {
            voltageRegulatorOn.set(initStateArraySize + i, voltageRegulatorOn.get(sourceIndex));
            targetQ.add(targetQ.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            voltageRegulatorOn.set(index, voltageRegulatorOn.get(sourceIndex));
            targetQ.set(index, targetQ.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
    }
}
