/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface RatioTapChangerAdder {

    public interface StepAdder {

        StepAdder setRho(float rho);

        StepAdder setR(float r);

        StepAdder setX(float x);

        StepAdder setG(float g);

        StepAdder setB(float b);

        RatioTapChangerAdder endStep();
    }

    RatioTapChangerAdder setLowTapPosition(int lowTapPosition);

    RatioTapChangerAdder setTapPosition(int tapPosition);

    RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities);

    RatioTapChangerAdder setRegulating(boolean regulating);

    RatioTapChangerAdder setTargetV(float targetV);

    RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal);

    StepAdder beginStep();

    RatioTapChanger add();

}
