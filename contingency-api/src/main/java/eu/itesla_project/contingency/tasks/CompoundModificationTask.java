/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.iidm.network.Network;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CompoundModificationTask implements ModificationTask {

    private final List<ModificationTask> subTasks;

    public CompoundModificationTask(List<ModificationTask> subTasks) {
        this.subTasks = subTasks;
    }

    public CompoundModificationTask(ModificationTask... subTasks) {
        this(Arrays.asList(subTasks));
    }

    @Override
    public void modify(Network network) {
        for (ModificationTask subTask : subTasks) {
            subTask.modify(network);
        }
    }

}
