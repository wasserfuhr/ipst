/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import org.apache.commons.cli.CommandLine;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Tool {

    /**
     * Get the command of the tool.
     */
    Command getCommand();

    /**
     * Run the tool.
     *
     * @param line the command line arguments
     * @throws Exception if the command fails
     */
    void run(CommandLine line) throws Exception;

}
