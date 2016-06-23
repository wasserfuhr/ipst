/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * HVDC converter station. This is the base class for VSC and LCC.
 * AC side of the converter is connected inside a substation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface HvdcConverterStation<T extends HvdcConverterStation<T>> extends SingleTerminalConnectable {

    /**
     * HDVC mode: VSC or LCC
     */
    enum HvdcType {
        VSC,
        LCC
    }

    /**
     * Converter mode used to known the sign of the active power of the HVDC line.
     */
    enum ConverterMode {
        RECTIFIER,
        INVERTER
    }

    /**
     * Get HVDC type.
     * @return HVDC type
     */
    HvdcType getHvdcType();

    /**
     * Get converter mode. If the converter is in rectifier mode active power of the connected HVDC line is positive
     * otherwise negative.
     * @return the converter mode
     */
    ConverterMode getConverterMode();

    /**
     * Change the converter mode.
     * @param mode the converter mode
     * @return the station itself to allow method chaining.
     */
    T setConverterMode(ConverterMode mode);

}
