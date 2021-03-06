/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.auto.service.AutoService;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * XML export of an IIDM model.<p>
 * <table border="1">
 *     <tr>
 *         <td><b>property name</b></td>
 *         <td><b>comment</b></td>
 *         <td><b>possible values</b></td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.indent</td>
 *         <td>if true write indented xml (4 spaces)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.with-branch-state-variables</td>
 *         <td>if true export branches state (active and reactive flow)</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.force-bus-branch-topo</td>
 *         <td>if true remove switches and aggregate buses</td>
 *         <td>true or false</td>
 *     </tr>
 *     <tr>
 *         <td>iidm.export.xml.only-main-cc</td>
 *         <td>if true only export equipments of the main connected component</td>
 *         <td>true or false</td>
 *     </tr>
 * </table>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Exporter.class)
public class XMLExporter implements Exporter, XmlConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLExporter.class);

    public static final String INDENT_PROPERTY = "iidm.export.xml.indent";

    public static final String WITH_BRANCH_STATE_VARIABLES_PROPERTY = "iidm.export.xml.with-branch-state-variables";

    public static final String FORCE_BUS_BRANCH_TOPO_PROPERTY = "iidm.export.xml.force-bus-branch-topo";

    public static final String ONLY_MAIN_CC_PROPERTIES = "iidm.export.xml.only-main-cc";

    public static final String ANONYMISED_PROPERTIES = "iidm.export.xml.anonymised";

    public static final String SKIP_EXTENSIONS_PROPERTIES = "iidm.export.xml.skip-extensions";

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM XML v" + VERSION + " exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }

        XMLExportOptions options = new XMLExportOptions();
        if (parameters != null) {
            options.setIndent(!"false".equals(parameters.getProperty(INDENT_PROPERTY)))
                    .setWithBranchSV("true".equals(parameters.getProperty(WITH_BRANCH_STATE_VARIABLES_PROPERTY)))
                    .setForceBusBranchTopo("true".equals(parameters.getProperty(FORCE_BUS_BRANCH_TOPO_PROPERTY, "false")))
                    .setOnlyMainCc("true".equals(parameters.getProperty(ONLY_MAIN_CC_PROPERTIES)))
                    .setAnonymized("true".equals(parameters.getProperty(ANONYMISED_PROPERTIES)))
                    .setSkipExtensions("true".equals(parameters.getProperty(SKIP_EXTENSIONS_PROPERTIES)));
        }

        try {
            long startTime = System.currentTimeMillis();

            try (OutputStream os = dataSource.newOutputStream(null, "xiidm", false);
                 BufferedOutputStream bos = new BufferedOutputStream(os)) {
                Anonymizer anonymizer = NetworkXml.write(network, options, bos);
                if (anonymizer != null) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dataSource.newOutputStream("_mapping", "csv", false)))) {
                        anonymizer.write(writer);
                    }
                }
            }

            LOGGER.debug("XIIDM export done in {} ms", (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
