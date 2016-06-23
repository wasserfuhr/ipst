/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcConverterStation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class HvdcConverterStationAdderImpl<T extends HvdcConverterStationAdderImpl<T>> extends SingleTerminalConnectableAdderImpl<T> {

    protected HvdcConverterStation.ConverterMode converterMode;

    public T setConverterMode(HvdcConverterStation.ConverterMode converterMode) {
        this.converterMode = converterMode;
        return (T) this;
    }

}
