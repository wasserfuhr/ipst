/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.cases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.ucte.util.EntsoeGeographicalCode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepositoryTest {

    private FileSystem fileSystem;
    private Path rootDir;
    private EntsoeCaseRepository caseRepository;
    private Network cimNetwork;
    private Network uctNetwork;

    private class DataSourceMock implements DataSource {
        private final Path directory;
        private final String baseName;

        private DataSourceMock(Path directory, String baseName) {
            this.directory = directory;
            this.baseName = baseName;
        }

        private Path getDirectory() {
            return directory;
        }

        @Override
        public boolean exists(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBaseName() {
            return baseName;
        }

        @Override
        public boolean exists(String suffix, String ext) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static void createFile(Path dir, String fileName) throws IOException {
        try (Writer writer = Files.newBufferedWriter(dir.resolve(fileName))) {
            writer.write("test");
        }
    }

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        rootDir = fileSystem.getPath("/");

        Importer cimImporter = Mockito.mock(Importer.class);
        Mockito.when(cimImporter.exists(Matchers.isA(DataSource.class)))
                .thenAnswer(invocation -> {
                    DataSourceMock dataSource = invocation.getArgumentAt(0, DataSourceMock.class);
                    Path file = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".zip");
                    return Files.isRegularFile(file) && Files.exists(file);
                });
        Mockito.when(cimImporter.getFormat())
                .thenReturn("CIM1");
        cimNetwork = Mockito.mock(Network.class);
        Mockito.when(cimImporter.import_(Matchers.isA(DataSource.class), Matchers.any()))
                .thenReturn(cimNetwork);

        Importer uctImporter = Mockito.mock(Importer.class);
        Mockito.when(uctImporter.exists(Matchers.isA(DataSource.class)))
                .thenAnswer(invocation -> {
                    DataSourceMock dataSource = invocation.getArgumentAt(0, DataSourceMock.class);
                    Path file = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".uct");
                    return Files.isRegularFile(file) && Files.exists(file);
                });
        Mockito.when(uctImporter.getFormat())
                .thenReturn("UCTE");
        uctNetwork = Mockito.mock(Network.class);
        Mockito.when(uctImporter.import_(Matchers.isA(DataSource.class), Matchers.any()))
                .thenReturn(uctNetwork);

        caseRepository = new EntsoeCaseRepository(new EntsoeCaseRepositoryConfig(rootDir, HashMultimap.create()),
                Arrays.asList(new EntsoeCaseRepository.EntsoeFormat(cimImporter, "CIM"),
                        new EntsoeCaseRepository.EntsoeFormat(uctImporter, "UCT")),
                (directory, baseName) -> new DataSourceMock(directory, baseName));
        Path dir1 = fileSystem.getPath("/CIM/SN/2013/01/13");
        Files.createDirectories(dir1);
        createFile(dir1, "20130113_0015_SN7_FR0.zip");
        createFile(dir1, "20130113_0045_SN7_FR0.zip");
        Path dir2 = fileSystem.getPath("/CIM/SN/2013/01/14");
        Files.createDirectories(dir2);
        createFile(dir2, "20130114_0015_SN1_FR0.zip");
        Path dir3 = fileSystem.getPath("/UCT/SN/2013/01/14");
        Files.createDirectories(dir3);
        createFile(dir3, "20130114_0015_SN1_FR0.uct");
        createFile(dir3, "20130114_0030_SN1_FR0.uct");
        Path dir4 = fileSystem.getPath("/UCT/SN/2013/01/15");
        Files.createDirectories(dir4);
        createFile(dir4, "20130115_0015_SN2_D20.uct");
        createFile(dir4, "20130115_0015_SN2_D40.uct");
        createFile(dir4, "20130115_0015_SN2_D70.uct");
        createFile(dir4, "20130115_0015_SN2_D80.uct");

        // D2
        Path dir5 = fileSystem.getPath("/UCT/FO/2013/01/15");
        Files.createDirectories(dir5);
        createFile(dir5, "20130115_0030_2D2_FR0.uct");
        createFile(dir5, "20130115_0130_2D2_FR0.uct");

        // LR
        Path dir6 = fileSystem.getPath("/UCT/FO/2013/01/15");
        Files.createDirectories(dir6);
        createFile(dir6, "20130115_0030_LR2_FR0.uct");
        createFile(dir6, "20130115_0130_LR2_FR0.uct");

        // RE
        Path dir7 = fileSystem.getPath("/UCT/FO/2013/01/15");
        Files.createDirectories(dir7);
        createFile(dir7, "20130115_0030_RE2_FR0.uct");
        createFile(dir7, "20130115_0130_RE2_FR0.uct");

        // INTRADAY
        Path dir8 = fileSystem.getPath("/UCT/FO/2013/01/15");
        Files.createDirectories(dir8);
        createFile(dir8, "20130115_0330_012_FR0.uct");
        createFile(dir8, "20130115_0330_022_FR0.uct");
        createFile(dir8, "20130115_0330_032_FR0.uct");

        // daylight saving FO
        Path dir9 = fileSystem.getPath("/UCT/FO/2016/10/30");
        Files.createDirectories(dir9);
        createFile(dir9, "20161030_0230_FO7_FR0.uct");
        createFile(dir9, "20161030_B230_FO7_FR0.uct");

        Files.createDirectories(fileSystem.getPath("/CIM/FO"));

        // 1 day FOs (intraday, dayahead, 2daysahead)
        Path dirFO = fileSystem.getPath("/UCT/FO/2016/09/30");
        Files.createDirectories(dirFO);
        List<String> fileNamesList= new ArrayList<>();
        fileNamesList.add("20160930_0030_FO5_FR0.uct");  //20160929    18, 24 DACF
        fileNamesList.add("20160930_0130_FO5_FR0.uct");
        fileNamesList.add("20160930_0230_FO5_FR0.uct");
        fileNamesList.add("20160930_0330_FO5_FR0.uct");
        fileNamesList.add("20160930_0430_FO5_FR0.uct");
        fileNamesList.add("20160930_0530_FO5_FR0.uct");
        fileNamesList.add("20160930_0630_FO5_FR0.uct");
        fileNamesList.add("20160930_0730_FO5_FR0.uct");
        fileNamesList.add("20160930_0830_FO5_FR0.uct");
        fileNamesList.add("20160930_0930_FO5_FR0.uct");
        fileNamesList.add("20160930_1030_FO5_FR0.uct");
        fileNamesList.add("20160930_1130_FO5_FR0.uct");
        fileNamesList.add("20160930_1230_FO5_FR0.uct");
        fileNamesList.add("20160930_1330_FO5_FR0.uct");
        fileNamesList.add("20160930_1430_FO5_FR0.uct");
        fileNamesList.add("20160930_1530_FO5_FR0.uct");
        fileNamesList.add("20160930_1630_FO5_FR0.uct");
        fileNamesList.add("20160930_1730_FO5_FR0.uct");
        fileNamesList.add("20160930_1830_FO5_FR0.uct");
        fileNamesList.add("20160930_1930_FO5_FR0.uct");
        fileNamesList.add("20160930_2030_FO5_FR0.uct");
        fileNamesList.add("20160930_2130_FO5_FR0.uct");
        fileNamesList.add("20160930_2230_FO5_FR0.uct");
        fileNamesList.add("20160930_2330_FO5_FR0.uct");

        fileNamesList.add("20160930_0030_2D5_FR0.uct");//20160930;    2 days ahead
        fileNamesList.add("20160930_0130_2D5_FR0.uct");
        fileNamesList.add("20160930_0230_2D5_FR0.uct");
        fileNamesList.add("20160930_0330_2D5_FR0.uct");
        fileNamesList.add("20160930_0430_2D5_FR0.uct");
        fileNamesList.add("20160930_0530_2D5_FR0.uct");
        fileNamesList.add("20160930_0630_2D5_FR0.uct");
        fileNamesList.add("20160930_0730_2D5_FR0.uct");
        fileNamesList.add("20160930_0830_2D5_FR0.uct");
        fileNamesList.add("20160930_0930_2D5_FR0.uct");
        fileNamesList.add("20160930_1030_2D5_FR0.uct");
        fileNamesList.add("20160930_1130_2D5_FR0.uct");
        fileNamesList.add("20160930_1230_2D5_FR0.uct");
        fileNamesList.add("20160930_1330_2D5_FR0.uct");
        fileNamesList.add("20160930_1430_2D5_FR0.uct");
        fileNamesList.add("20160930_1530_2D5_FR0.uct");
        fileNamesList.add("20160930_1630_2D5_FR0.uct");
        fileNamesList.add("20160930_1730_2D5_FR0.uct");
        fileNamesList.add("20160930_1830_2D5_FR0.uct");
        fileNamesList.add("20160930_1930_2D5_FR0.uct");
        fileNamesList.add("20160930_2030_2D5_FR0.uct");
        fileNamesList.add("20160930_2130_2D5_FR0.uct");
        fileNamesList.add("20160930_2230_2D5_FR0.uct");
        fileNamesList.add("20160930_2330_2D5_FR0.uct");

        fileNamesList.add("20160930_0130_015_FR0.uct"); //20160930;    00.30 , 23 intraday
        fileNamesList.add("20160930_0230_025_FR0.uct");
        fileNamesList.add("20160930_0330_035_FR0.uct");
        fileNamesList.add("20160930_0430_045_FR0.uct");
        fileNamesList.add("20160930_0530_055_FR0.uct");
        fileNamesList.add("20160930_0630_065_FR0.uct");
        fileNamesList.add("20160930_0730_075_FR0.uct");
        fileNamesList.add("20160930_0830_085_FR0.uct");
        fileNamesList.add("20160930_0930_095_FR0.uct");
        fileNamesList.add("20160930_1030_105_FR0.uct");
        fileNamesList.add("20160930_1130_115_FR0.uct");
        fileNamesList.add("20160930_1230_125_FR0.uct");
        fileNamesList.add("20160930_1330_135_FR0.uct");
        fileNamesList.add("20160930_1430_145_FR0.uct");
        fileNamesList.add("20160930_1530_155_FR0.uct");
        fileNamesList.add("20160930_1630_165_FR0.uct");
        fileNamesList.add("20160930_1730_175_FR0.uct");
        fileNamesList.add("20160930_1830_185_FR0.uct");
        fileNamesList.add("20160930_1930_195_FR0.uct");
        fileNamesList.add("20160930_2030_205_FR0.uct");
        fileNamesList.add("20160930_2130_215_FR0.uct");
        fileNamesList.add("20160930_2230_225_FR0.uct");
        fileNamesList.add("20160930_2330_235_FR0.uct");

        fileNamesList.add("20160930_0230_015_FR0.uct"); //20160930;    01.30 , 22 intraday
        fileNamesList.add("20160930_0330_025_FR0.uct");
        fileNamesList.add("20160930_0430_035_FR0.uct");
        fileNamesList.add("20160930_0530_045_FR0.uct");
        fileNamesList.add("20160930_0630_055_FR0.uct");
        fileNamesList.add("20160930_0730_065_FR0.uct");
        fileNamesList.add("20160930_0830_075_FR0.uct");
        fileNamesList.add("20160930_0930_085_FR0.uct");
        fileNamesList.add("20160930_1030_195_FR0.uct");
        fileNamesList.add("20160930_1130_105_FR0.uct");
        fileNamesList.add("20160930_1230_115_FR0.uct");
        fileNamesList.add("20160930_1330_125_FR0.uct");
        fileNamesList.add("20160930_1430_135_FR0.uct");
        fileNamesList.add("20160930_1530_145_FR0.uct");
        fileNamesList.add("20160930_1630_155_FR0.uct");
        fileNamesList.add("20160930_1730_165_FR0.uct");
        fileNamesList.add("20160930_1830_175_FR0.uct");
        fileNamesList.add("20160930_1930_185_FR0.uct");
        fileNamesList.add("20160930_2030_195_FR0.uct");
        fileNamesList.add("20160930_2130_205_FR0.uct");
        fileNamesList.add("20160930_2230_215_FR0.uct");
        fileNamesList.add("20160930_2330_225_FR0.uct");

        fileNamesList.add("20160930_0330_015_FR0.uct");//20160930;    02.30 , 21 intraday
        fileNamesList.add("20160930_0430_025_FR0.uct");
        fileNamesList.add("20160930_0530_035_FR0.uct");
        fileNamesList.add("20160930_0630_045_FR0.uct");
        fileNamesList.add("20160930_0730_055_FR0.uct");
        fileNamesList.add("20160930_0830_065_FR0.uct");
        fileNamesList.add("20160930_0930_075_FR0.uct");
        fileNamesList.add("20160930_1030_085_FR0.uct");
        fileNamesList.add("20160930_1130_095_FR0.uct");
        fileNamesList.add("20160930_1230_105_FR0.uct");
        fileNamesList.add("20160930_1330_115_FR0.uct");
        fileNamesList.add("20160930_1430_125_FR0.uct");
        fileNamesList.add("20160930_1530_135_FR0.uct");
        fileNamesList.add("20160930_1630_145_FR0.uct");
        fileNamesList.add("20160930_1730_155_FR0.uct");
        fileNamesList.add("20160930_1830_165_FR0.uct");
        fileNamesList.add("20160930_1930_175_FR0.uct");
        fileNamesList.add("20160930_2030_185_FR0.uct");
        fileNamesList.add("20160930_2130_195_FR0.uct");
        fileNamesList.add("20160930_2230_205_FR0.uct");
        fileNamesList.add("20160930_2330_215_FR0.uct");

        fileNamesList.add("20160930_0430_015_FR0.uct");//20160930;    03.30 , 20 intraday
        fileNamesList.add("20160930_0530_025_FR0.uct");
        fileNamesList.add("20160930_0630_035_FR0.uct");
        fileNamesList.add("20160930_0730_045_FR0.uct");
        fileNamesList.add("20160930_0830_055_FR0.uct");
        fileNamesList.add("20160930_0930_065_FR0.uct");
        fileNamesList.add("20160930_1030_075_FR0.uct");
        fileNamesList.add("20160930_1130_085_FR0.uct");
        fileNamesList.add("20160930_1230_095_FR0.uct");
        fileNamesList.add("20160930_1330_105_FR0.uct");
        fileNamesList.add("20160930_1430_115_FR0.uct");
        fileNamesList.add("20160930_1530_125_FR0.uct");
        fileNamesList.add("20160930_1630_135_FR0.uct");
        fileNamesList.add("20160930_1730_145_FR0.uct");
        fileNamesList.add("20160930_1830_155_FR0.uct");
        fileNamesList.add("20160930_1930_165_FR0.uct");
        fileNamesList.add("20160930_2030_175_FR0.uct");
        fileNamesList.add("20160930_2130_185_FR0.uct");
        fileNamesList.add("20160930_2230_195_FR0.uct");
        fileNamesList.add("20160930_2330_205_FR0.uct");

        fileNamesList.add("20160930_0530_015_FR0.uct");//20160930;    04.30 , 19 intraday
        fileNamesList.add("20160930_0630_025_FR0.uct");
        fileNamesList.add("20160930_0730_035_FR0.uct");
        fileNamesList.add("20160930_0830_045_FR0.uct");
        fileNamesList.add("20160930_0930_055_FR0.uct");
        fileNamesList.add("20160930_1030_065_FR0.uct");
        fileNamesList.add("20160930_1130_075_FR0.uct");
        fileNamesList.add("20160930_1230_085_FR0.uct");
        fileNamesList.add("20160930_1330_095_FR0.uct");
        fileNamesList.add("20160930_1430_105_FR0.uct");
        fileNamesList.add("20160930_1530_115_FR0.uct");
        fileNamesList.add("20160930_1630_125_FR0.uct");
        fileNamesList.add("20160930_1730_135_FR0.uct");
        fileNamesList.add("20160930_1830_145_FR0.uct");
        fileNamesList.add("20160930_1930_155_FR0.uct");
        fileNamesList.add("20160930_2030_165_FR0.uct");
        fileNamesList.add("20160930_2130_175_FR0.uct");
        fileNamesList.add("20160930_2230_185_FR0.uct");
        fileNamesList.add("20160930_2330_195_FR0.uct");

        fileNamesList.add("20160930_0630_015_FR0.uct");//20160930;    05.30 , 18 intraday
        fileNamesList.add("20160930_0730_025_FR0.uct");
        fileNamesList.add("20160930_0830_035_FR0.uct");
        fileNamesList.add("20160930_0930_045_FR0.uct");
        fileNamesList.add("20160930_1030_055_FR0.uct");
        fileNamesList.add("20160930_1130_065_FR0.uct");
        fileNamesList.add("20160930_1230_075_FR0.uct");
        fileNamesList.add("20160930_1330_085_FR0.uct");
        fileNamesList.add("20160930_1430_095_FR0.uct");
        fileNamesList.add("20160930_1530_105_FR0.uct");
        fileNamesList.add("20160930_1630_115_FR0.uct");
        fileNamesList.add("20160930_1730_125_FR0.uct");
        fileNamesList.add("20160930_1830_135_FR0.uct");
        fileNamesList.add("20160930_1930_145_FR0.uct");
        fileNamesList.add("20160930_2030_155_FR0.uct");
        fileNamesList.add("20160930_2130_165_FR0.uct");
        fileNamesList.add("20160930_2230_175_FR0.uct");
        fileNamesList.add("20160930_2330_185_FR0.uct");

        fileNamesList.add("20160930_0730_015_FR0.uct");//20160930;    06.30 , 17 intraday
        fileNamesList.add("20160930_0830_025_FR0.uct");
        fileNamesList.add("20160930_0930_035_FR0.uct");
        fileNamesList.add("20160930_1030_045_FR0.uct");
        fileNamesList.add("20160930_1130_055_FR0.uct");
        fileNamesList.add("20160930_1230_065_FR0.uct");
        fileNamesList.add("20160930_1330_075_FR0.uct");
        fileNamesList.add("20160930_1430_085_FR0.uct");
        fileNamesList.add("20160930_1530_095_FR0.uct");
        fileNamesList.add("20160930_1630_105_FR0.uct");
        fileNamesList.add("20160930_1730_115_FR0.uct");
        fileNamesList.add("20160930_1830_125_FR0.uct");
        fileNamesList.add("20160930_1930_135_FR0.uct");
        fileNamesList.add("20160930_2030_145_FR0.uct");
        fileNamesList.add("20160930_2130_155_FR0.uct");
        fileNamesList.add("20160930_2230_165_FR0.uct");
        fileNamesList.add("20160930_2330_175_FR0.uct");

        fileNamesList.add("20160930_0830_015_FR0.uct");//20160930;    07.30 , 16 intraday
        fileNamesList.add("20160930_0930_025_FR0.uct");
        fileNamesList.add("20160930_1030_035_FR0.uct");
        fileNamesList.add("20160930_1130_045_FR0.uct");
        fileNamesList.add("20160930_1230_055_FR0.uct");
        fileNamesList.add("20160930_1330_065_FR0.uct");
        fileNamesList.add("20160930_1430_075_FR0.uct");
        fileNamesList.add("20160930_1530_085_FR0.uct");
        fileNamesList.add("20160930_1630_095_FR0.uct");
        fileNamesList.add("20160930_1730_105_FR0.uct");
        fileNamesList.add("20160930_1830_115_FR0.uct");
        fileNamesList.add("20160930_1930_125_FR0.uct");
        fileNamesList.add("20160930_2030_135_FR0.uct");
        fileNamesList.add("20160930_2130_145_FR0.uct");
        fileNamesList.add("20160930_2230_155_FR0.uct");
        fileNamesList.add("20160930_2330_165_FR0.uct");

        fileNamesList.add("20160930_0930_015_FR0.uct");//20160930;    08.30 , 15 intraday
        fileNamesList.add("20160930_1030_025_FR0.uct");
        fileNamesList.add("20160930_1130_035_FR0.uct");
        fileNamesList.add("20160930_1230_045_FR0.uct");
        fileNamesList.add("20160930_1330_055_FR0.uct");
        fileNamesList.add("20160930_1430_065_FR0.uct");
        fileNamesList.add("20160930_1530_075_FR0.uct");
        fileNamesList.add("20160930_1630_085_FR0.uct");
        fileNamesList.add("20160930_1730_095_FR0.uct");
        fileNamesList.add("20160930_1830_105_FR0.uct");
        fileNamesList.add("20160930_1930_115_FR0.uct");
        fileNamesList.add("20160930_2030_125_FR0.uct");
        fileNamesList.add("20160930_2130_135_FR0.uct");
        fileNamesList.add("20160930_2230_145_FR0.uct");
        fileNamesList.add("20160930_2330_155_FR0.uct");

        fileNamesList.add("20160930_1030_015_FR0.uct");//20160930;    09.30 , 14 intraday
        fileNamesList.add("20160930_1130_025_FR0.uct");
        fileNamesList.add("20160930_1230_035_FR0.uct");
        fileNamesList.add("20160930_1330_045_FR0.uct");
        fileNamesList.add("20160930_1430_055_FR0.uct");
        fileNamesList.add("20160930_1530_065_FR0.uct");
        fileNamesList.add("20160930_1630_075_FR0.uct");
        fileNamesList.add("20160930_1730_085_FR0.uct");
        fileNamesList.add("20160930_1830_095_FR0.uct");
        fileNamesList.add("20160930_1930_105_FR0.uct");
        fileNamesList.add("20160930_2030_115_FR0.uct");
        fileNamesList.add("20160930_2130_125_FR0.uct");
        fileNamesList.add("20160930_2230_135_FR0.uct");
        fileNamesList.add("20160930_2330_145_FR0.uct");

        fileNamesList.add("20160930_1130_015_FR0.uct");//20160930;    10.30 , 13 intraday
        fileNamesList.add("20160930_1230_025_FR0.uct");
        fileNamesList.add("20160930_1330_035_FR0.uct");
        fileNamesList.add("20160930_1430_045_FR0.uct");
        fileNamesList.add("20160930_1530_055_FR0.uct");
        fileNamesList.add("20160930_1630_065_FR0.uct");
        fileNamesList.add("20160930_1730_075_FR0.uct");
        fileNamesList.add("20160930_1830_085_FR0.uct");
        fileNamesList.add("20160930_1930_095_FR0.uct");
        fileNamesList.add("20160930_2030_105_FR0.uct");
        fileNamesList.add("20160930_2130_115_FR0.uct");
        fileNamesList.add("20160930_2230_125_FR0.uct");
        fileNamesList.add("20160930_2330_135_FR0.uct");

        fileNamesList.add("20160930_1230_015_FR0.uct");//20160930;    11.30 , 12 intraday
        fileNamesList.add("20160930_1330_025_FR0.uct");
        fileNamesList.add("20160930_1430_035_FR0.uct");
        fileNamesList.add("20160930_1530_045_FR0.uct");
        fileNamesList.add("20160930_1630_055_FR0.uct");
        fileNamesList.add("20160930_1730_065_FR0.uct");
        fileNamesList.add("20160930_1830_075_FR0.uct");
        fileNamesList.add("20160930_1930_085_FR0.uct");
        fileNamesList.add("20160930_2030_095_FR0.uct");
        fileNamesList.add("20160930_2130_105_FR0.uct");
        fileNamesList.add("20160930_2230_115_FR0.uct");
        fileNamesList.add("20160930_2330_125_FR0.uct");

        fileNamesList.add("20160930_1330_015_FR0.uct");//20160930;    12.30 , 11 intraday
        fileNamesList.add("20160930_1430_025_FR0.uct");
        fileNamesList.add("20160930_1530_035_FR0.uct");
        fileNamesList.add("20160930_1630_045_FR0.uct");
        fileNamesList.add("20160930_1730_055_FR0.uct");
        fileNamesList.add("20160930_1830_065_FR0.uct");
        fileNamesList.add("20160930_1930_075_FR0.uct");
        fileNamesList.add("20160930_2030_085_FR0.uct");
        fileNamesList.add("20160930_2130_095_FR0.uct");
        fileNamesList.add("20160930_2230_105_FR0.uct");
        fileNamesList.add("20160930_2330_115_FR0.uct");

        fileNamesList.add("20160930_1430_015_FR0.uct");//20160930;    13.30 , 10 intraday
        fileNamesList.add("20160930_1530_025_FR0.uct");
        fileNamesList.add("20160930_1630_035_FR0.uct");
        fileNamesList.add("20160930_1730_045_FR0.uct");
        fileNamesList.add("20160930_1830_055_FR0.uct");
        fileNamesList.add("20160930_1930_065_FR0.uct");
        fileNamesList.add("20160930_2030_075_FR0.uct");
        fileNamesList.add("20160930_2130_085_FR0.uct");
        fileNamesList.add("20160930_2230_095_FR0.uct");
        fileNamesList.add("20160930_2330_105_FR0.uct");

        fileNamesList.add("20160930_1530_015_FR0.uct");//20160930;    14.30 , 9 intraday
        fileNamesList.add("20160930_1630_025_FR0.uct");
        fileNamesList.add("20160930_1730_035_FR0.uct");
        fileNamesList.add("20160930_1830_045_FR0.uct");
        fileNamesList.add("20160930_1930_055_FR0.uct");
        fileNamesList.add("20160930_2030_065_FR0.uct");
        fileNamesList.add("20160930_2130_075_FR0.uct");
        fileNamesList.add("20160930_2230_085_FR0.uct");
        fileNamesList.add("20160930_2330_095_FR0.uct");

        fileNamesList.add("20160930_1630_015_FR0.uct");//20160930;    15.30 , 8 intraday
        fileNamesList.add("20160930_1730_025_FR0.uct");
        fileNamesList.add("20160930_1830_035_FR0.uct");
        fileNamesList.add("20160930_1930_045_FR0.uct");
        fileNamesList.add("20160930_2030_055_FR0.uct");
        fileNamesList.add("20160930_2130_065_FR0.uct");
        fileNamesList.add("20160930_2230_075_FR0.uct");
        fileNamesList.add("20160930_2330_085_FR0.uct");

        fileNamesList.add("20160930_1730_015_FR0.uct");//20160930;    16.30 , 7 intraday
        fileNamesList.add("20160930_1830_025_FR0.uct");
        fileNamesList.add("20160930_1930_035_FR0.uct");
        fileNamesList.add("20160930_2030_045_FR0.uct");
        fileNamesList.add("20160930_2130_055_FR0.uct");
        fileNamesList.add("20160930_2230_065_FR0.uct");
        fileNamesList.add("20160930_2330_075_FR0.uct");

        fileNamesList.add("20160930_1830_015_FR0.uct");//20160930;    17.30 , 6 intraday
        fileNamesList.add("20160930_1930_025_FR0.uct");
        fileNamesList.add("20160930_2030_035_FR0.uct");
        fileNamesList.add("20160930_2130_045_FR0.uct");
        fileNamesList.add("20160930_2230_055_FR0.uct");
        fileNamesList.add("20160930_2330_065_FR0.uct");

        fileNamesList.add("20160930_1930_015_FR0.uct");//20160930;    18.30 , 5 intraday
        fileNamesList.add("20160930_2030_025_FR0.uct");
        fileNamesList.add("20160930_2130_035_FR0.uct");
        fileNamesList.add("20160930_2230_045_FR0.uct");
        fileNamesList.add("20160930_2330_055_FR0.uct");

        fileNamesList.add("20160930_2030_015_FR0.uct");//20160930;    19.30 , 4 intraday
        fileNamesList.add("20160930_2130_025_FR0.uct");
        fileNamesList.add("20160930_2230_035_FR0.uct");
        fileNamesList.add("20160930_2330_045_FR0.uct");

        fileNamesList.add("20160930_2130_015_FR0.uct");//20160930;    20.30 , 3 intraday
        fileNamesList.add("20160930_2230_025_FR0.uct");
        fileNamesList.add("20160930_2330_035_FR0.uct");

        fileNamesList.add("20160930_2230_015_FR0.uct");//20160930;    21.30 , 2 intraday
        fileNamesList.add("20160930_2330_025_FR0.uct");

        fileNamesList.add("20160930_2330_015_FR0.uct");//20160930;    22.30 , 1 intraday

        for (String filename: fileNamesList) {
            createFile(dirFO, filename);
        }
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testLoad() throws Exception {
        assertTrue(caseRepository.load(DateTime.parse("2013-01-13T00:15:00+01:00"), CaseType.SN, Country.FR).size() == 1);
        assertTrue(caseRepository.load(DateTime.parse("2013-01-13T00:30:00+01:00"), CaseType.SN, Country.FR).isEmpty());
        assertTrue(caseRepository.load(DateTime.parse("2013-01-13T00:15:00+01:00"), CaseType.FO, Country.FR).isEmpty());
        assertTrue(caseRepository.load(DateTime.parse("2013-01-13T00:15:00+01:00"), CaseType.SN, Country.BE).isEmpty());

        // check that cim network is loaded instead of uct network
        assertTrue(caseRepository.load(DateTime.parse("2013-01-14T00:15:00+01:00"), CaseType.SN, Country.FR).equals(Collections.singletonList(cimNetwork)));

        // check that if cim is forbidden for france, uct is loaded
        caseRepository.getConfig().getForbiddenFormatsByGeographicalCode().put(EntsoeGeographicalCode.FR, "CIM1");
        assertTrue(caseRepository.load(DateTime.parse("2013-01-14T00:15:00+01:00"), CaseType.SN, Country.FR).equals(Collections.singletonList(uctNetwork)));

        assertTrue(caseRepository.load(DateTime.parse("2013-01-15T00:15:00+01:00"), CaseType.SN, Country.DE).size() == 4);
    }

    @Test
    public void testIsDataAvailable() throws Exception {
        assertTrue(caseRepository.isDataAvailable(DateTime.parse("2013-01-13T00:15:00+01:00"), CaseType.SN, Country.FR));
        assertFalse(caseRepository.isDataAvailable(DateTime.parse("2013-01-13T00:30:00+01:00"), CaseType.SN, Country.FR));
    }

    @Test
    public void testDataAvailable() throws Exception {
        assertTrue(caseRepository.dataAvailable(CaseType.SN, EnumSet.of(Country.FR), Interval.parse("2013-01-13T00:00:00+01:00/2013-01-13T00:30:00+01:00"))
                .equals(Sets.newHashSet(DateTime.parse("2013-01-13T00:15:00+01:00"))));
        assertTrue(caseRepository.dataAvailable(CaseType.SN, EnumSet.of(Country.FR), Interval.parse("2013-01-13T00:00:00+01:00/2013-01-13T01:00:00+01:00"))
                .equals(Sets.newHashSet(DateTime.parse("2013-01-13T00:15:00+01:00"), DateTime.parse("2013-01-13T00:45:00+01:00"))));
        assertTrue(caseRepository.dataAvailable(CaseType.SN, EnumSet.of(Country.BE, Country.DE), Interval.parse("2013-01-13T00:00:00+01:00/2013-01-13T01:00:00+01:00"))
                .isEmpty());
        assertTrue(caseRepository.dataAvailable(CaseType.SN, EnumSet.of(Country.FR), Interval.parse("2013-01-14T00:00:00+01:00/2013-01-14T01:00:00+01:00"))
                .equals(Sets.newHashSet(DateTime.parse("2013-01-14T00:15:00+01:00"), DateTime.parse("2013-01-14T00:30:00+01:00"))));
    }

    @Test
    public void testLoadDayLightSaving() throws Exception {
        // forecastDistance 510
        List<Network> networksCEST=caseRepository.load(DateTime.parse("2016-10-30T02:30:00+02:00"), CaseType.FO, Country.FR);
        assertTrue(networksCEST.size() == 1);
        // forecastDistance 570
        List<Network> networksCET=caseRepository.load(DateTime.parse("2016-10-30T02:30:00+01:00"), CaseType.FO, Country.FR);
        assertTrue(networksCET.size() == 1);
    }



    @Test
    public void testDataAvailableDayLightSaving() throws Exception {

        // double date CEST + CET
        Set<DateTime> dset=caseRepository.dataAvailable(CaseType.FO, EnumSet.of(Country.FR), Interval.parse("2016-10-30T00:00:00+02:00/2016-10-30T03:30:00+01:00"));
        System.out.println(dset);
        assertTrue(dset.equals(Sets.newHashSet(DateTime.parse("2016-10-30T02:30:00+02:00"),DateTime.parse("2016-10-30T02:30:00+01:00"))));

        //just the CET one
        dset=caseRepository.dataAvailable(CaseType.FO, EnumSet.of(Country.FR), Interval.parse("2016-10-30T02:30:00+01:00/2016-10-30T03:30:00+01:00"));
        System.out.println(dset);
        assertTrue(dset.equals(Sets.newHashSet(DateTime.parse("2016-10-30T02:30:00+01:00"))));
    }

    @Test
    public void testLoadDayAhead() throws Exception {
        //20160930_0130_FO5_FR0
        assertTrue(caseRepository.load(DateTime.parse("2016-09-30T00:30:00+01:00"), CaseType.FO, Country.FR).size() == 1);
    }

    @Test
    public void testLoadIntraday() throws Exception {
        //20160930_0430_045_FR0 has forecastDistance of 240
        assertTrue(caseRepository.load(DateTime.parse("2016-09-30T03:30:00+01:00"), CaseType.FO, 240, Country.FR).size() == 1);
    }


    @Test
    public void testIsDataAvailable2DaysAhead() throws Exception {
        //20160930_0130_2D5_FR0 has forecastDistance 1830
        DateTime adate=DateTime.parse("2016-09-30T00:30:00+01:00");
        assertTrue(caseRepository.isDataAvailable(adate, CaseType.FO, 1830, Country.FR));
        assertTrue(caseRepository.load(adate, CaseType.FO, 1830, Country.FR).size() == 1);
    }

    @Test
    public void testDataAvailableDayAhead() throws Exception {
        //20160930_0130_FO5_FR0 , forecast distance 450
        assertTrue(caseRepository.dataAvailable(CaseType.FO, EnumSet.of(Country.FR), Interval.parse("2016-09-30T00:30:00+01:00/2016-09-30T01:30:00+01:00"))
                .equals(Sets.newHashSet(DateTime.parse("2016-09-30T01:30:00.000+02:00"))));
        assertTrue(caseRepository.load(DateTime.parse("2016-09-30T01:30:00.000+02:00"), CaseType.FO, 450, Country.FR).size() == 1);
    }

}