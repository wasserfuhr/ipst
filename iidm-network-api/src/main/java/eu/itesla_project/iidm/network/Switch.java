/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * A switch to connect equipments in a substation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Switch extends Identifiable<Switch> {

    /**
     * Get the kind of switch.
     */
    SwitchKind getKind();

    /**
     * Get the open status of the switch.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    boolean isOpen();

    /**
     * Get the retain status of the switch. A retained switch is a switch that
     * will be part of the bus/breaker topology.
     */
    boolean isRetained();

}
