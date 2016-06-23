/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * A HVDC line connected to two HVDC converters on DC side.
 * It has to be connected to the same <code>{@link HvdcConverterStation}</code> subclass.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface HvdcLine extends Identifiable {

    /**
     * Get the network this HVDC line belongs.
     * @return the network this HVDC line belongs
     */
    Network getNetwork();

    /**
     * Get resistance (in &#937;) of the line.
     * @return the resistance of the line
     */
    float getR();

    /**
     * Set the resistance (in &#937;) of the line.
     * @param r the resistance of the line
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setR(float r);

    /**
     * Get the nominal voltage (in Kv).
     * @return the nominal voltage.
     */
    float getNominalV();

    /**
     * Set the nominal voltage.
     * @param nominalV the nominal voltage.
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setNominalV(float nominalV);

    /**
     * Get the active power setpoint target (in MW).
     * @return the active power setpoint target
     */
    float getTargetP();

    /**
     * Set the active power setpoint target (in MW).
     * @param targetP the active power setpoint target
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setTargetP(float targetP);

    /**
     * Get the maximum active power (in MW).
     * @return the maximum active power
     */
    float getMaxP();

    /**
     * Set the maximum active power (in MW).
     * @param maxP the maximum active power
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setMaxP(float maxP);

    /**
     * Get the number of pole in service.
     * @return the number of pole in service
     */
    int getNumberOfPoleInService();

    /**
     * Set the number of pole in service.
     * @param numberOfPoleInService the number of pole in service
     * @return
     */
    HvdcLine setNumberOfPoleInService(int numberOfPoleInService);

    /**
     * Get the HVDC converter station connected on side 1.
     * @return the HVDC converter station connected on side 1
     */
    HvdcConverterStation<?> getConverterStation1();

    /**
     * Get the HVDC converter station connected on side 2.
     * @return the HVDC converter station connected on side 2
     */
    HvdcConverterStation<?> getConverterStation2();

    /**
     * Remove the HVDC line
     */
    void remove();
}
