/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.LccFilter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LccFilterImpl implements LccFilter {

    private float b;

    private boolean connected;

    LccFilterImpl(float b, boolean connected) {
        this.b = b;
        this.connected = connected;
    }

    @Override
    public float getB() {
        return b;
    }

    @Override
    public LccFilterImpl setB(float b) {
        this.b = b;
        return this;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public LccFilterImpl setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }
}
