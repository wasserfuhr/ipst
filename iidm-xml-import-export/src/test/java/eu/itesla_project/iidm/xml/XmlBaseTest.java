/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.common.io.ByteStreams;
import eu.itesla_project.iidm.network.Network;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class XmlBaseTest {

    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        tmpDir = fileSystem.getPath("tmp");
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    protected void roundTripTest(Network network, String ref) throws XMLStreamException, IOException, SAXException {
        Path xmlFile = tmpDir.resolve("n.xml");
        XMLExportOptions exportOptions = new XMLExportOptions();
        NetworkXml.write(network, exportOptions, xmlFile);
        NetworkXml.validate(xmlFile);
        assertEquals(new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8),
                     new String(ByteStreams.toByteArray(getClass().getResourceAsStream(ref)), StandardCharsets.UTF_8));
        Network network2 = NetworkXml.read(xmlFile);
        Path xmlFile2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, exportOptions, xmlFile2);
        NetworkXml.validate(xmlFile);
        assertEquals(new String(Files.readAllBytes(xmlFile2), StandardCharsets.UTF_8),
                     new String(ByteStreams.toByteArray(getClass().getResourceAsStream(ref)), StandardCharsets.UTF_8));
    }
}
