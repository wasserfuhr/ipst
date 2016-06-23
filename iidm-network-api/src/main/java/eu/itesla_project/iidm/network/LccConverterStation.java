/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

import java.util.List;

/**
 * LCC converter station.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LccConverterStation extends HvdcConverterStation<LccConverterStation> {

    /**
     * Harmonic filter.
     * q = b * v^2
     */
    interface Filter {

        /**
         * Get filter susceptance (&#937;).
         * @return
         */
        float getB();

        /**
         * Set filter susceptance (&#937;).
         * @param b filter susceptance;
         * @return the filter itself to allow method chaining
         */
        Filter setB(float b);

        /**
         * Check the filter is connected.
         * @return true if the filter is connected, false otherwise.
         */
        boolean isConnected();

        /**
         * Set the connection status of the filter.
         * @param connected the new connection status of the filter
         * @return the filter itself to allow method chaining
         */
        Filter setConnected(boolean connected);
    }

    /**
     * Get power factor (ratio of the active power and the apparent power)
     * @return the power factor.
     */
    float getPowerFactor();

    /**
     * Set the power factor. Has to be greater that zero.
     * @param powerFactor the new power factor
     * @return the converter itself to allow method chaining
     */
    LccConverterStation setPowerFactor(float powerFactor);

    /**
     * Get harmonic filters associated to this converter.
     * @return harmonic filters associated to this converter
     */
    List<Filter> getFilters();
}
