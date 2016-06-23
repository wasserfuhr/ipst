/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ConnectableType;
import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.Terminal;
import eu.itesla_project.iidm.network.impl.util.Ref;

import java.util.BitSet;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class HvdcConverterStationImpl<T extends HvdcConverterStation<T>> extends ConnectableImpl implements HvdcConverterStation<T> {

    private final BitSet rectifierMode;

    HvdcConverterStationImpl(String id, String name, Ref<? extends MultiStateObject> ref, ConverterMode converterMode) {
        super(id, name);
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.rectifierMode = new BitSet(stateArraySize);
        this.rectifierMode.set(0, stateArraySize, converterMode == ConverterMode.RECTIFIER);
    }

    @Override
    public ConverterMode getConverterMode() {
        return toEnum(rectifierMode.get(getNetwork().getStateIndex()));
    }

    private static ConverterMode toEnum(boolean rectifierMode) {
        return rectifierMode ? ConverterMode.RECTIFIER : ConverterMode.INVERTER;
    }

    private static boolean fromEnum(ConverterMode converterMode) {
        return converterMode == ConverterMode.RECTIFIER;
    }

    @Override
    public T setConverterMode(ConverterMode converterMode) {
        int stateIndex = getNetwork().getStateIndex();
        boolean oldValue = this.rectifierMode.get(stateIndex);
        this.rectifierMode.set(stateIndex, fromEnum(Objects.requireNonNull(converterMode)));
        notifyUpdate("converterMode", toEnum(oldValue), converterMode);
        return (T) this;
    }

    @Override
    public Terminal getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.HVDC_CONVERTER_STATION;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        for (int i = 0; i < number; i++) {
            rectifierMode.set(initStateArraySize + i, rectifierMode.get(sourceIndex));
        }
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            rectifierMode.set(index, rectifierMode.get(sourceIndex));
        }
    }

}
