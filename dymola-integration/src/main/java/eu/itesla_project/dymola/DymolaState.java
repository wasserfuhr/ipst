/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.modules.simulation.SimulationState;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class DymolaState implements SimulationState {

    private final String name;

    
    DymolaState(String name) {
	this.name=name;
    }

    @Override
    public String getName() {
        return name;
    }

}