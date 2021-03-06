/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.simulation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import eu.itesla_project.commons.config.PlatformConfig;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimulationDetailedParameters {

    private final static Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    public static class Contingency {

        private final String id;
        private final Map<String, Branch> branches = new HashMap<>();
        private final Map<String, Generator> generators = new HashMap<>();

        public Contingency(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public String getId() {
            return id;
        }

        public Map<String, Branch> getBranches() {
            return branches;
        }

        public Branch getBranch(String id) {
            return branches.get(id);
        }

        public Map<String, Generator> getGenerators() {
            return generators;
        }

        public Generator getGenerator(String id) {
            return generators.get(id);
        }
    }

    public static class Branch {

        private final String id;
        private final Double shortCircuitDuration;
        private final Double shortCircuitDistance;
        private final String shortCircuitSide;

        public Branch(String id, Double shortCircuitDuration, Double shortCircuitDistance, String shortCircuitSide) {
            this.id = Objects.requireNonNull(id);
            this.shortCircuitDuration = shortCircuitDuration;
            this.shortCircuitDistance = shortCircuitDistance;
            this.shortCircuitSide = shortCircuitSide;
        }

        public String getId() {
            return id;
        }

        public Double getShortCircuitDistance() {
            return shortCircuitDistance;
        }

        public Double getShortCircuitDuration() {
            return shortCircuitDuration;
        }

        public String getShortCircuitSide() {
            return shortCircuitSide;
        }
    }

    public static class Generator {
        private final String id;
        private final Double shortCircuitDuration;

        public Generator(String id, Double shortCircuitDuration) {
            this.id = Objects.requireNonNull(id);
            this.shortCircuitDuration = Objects.requireNonNull(shortCircuitDuration);
        }

        public String getId() {
            return id;
        }

        public Double getShortCircuitDuration() {
            return shortCircuitDuration;
        }
    }

    private final String fileName;

    private final Map<String, Contingency> contingencies = new HashMap<>();

    public SimulationDetailedParameters(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public Map<String, Contingency> getContingencies() {
        return contingencies;
    }

    public Contingency getContingency(String id) {
        return contingencies.get(id);
    }

    private static Double parseDoubleIfNotNull(String str) {
        return str == null ? null : Double.parseDouble(str);
    }

    private static void parse(BufferedReader reader, SimulationDetailedParameters parameters) {
        try {
            XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(reader);
            Contingency contingency = null;
            while (xmlsr.hasNext()) {
                int eventType = xmlsr.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        switch (xmlsr.getLocalName()) {
                            case "contingency":
                                contingency = new Contingency(xmlsr.getAttributeValue(null, "id"));
                                parameters.getContingencies().put(contingency.getId(), contingency);
                                break;
                            case "branch":
                                Branch branch = new Branch(xmlsr.getAttributeValue(null, "id"),
                                                           parseDoubleIfNotNull(xmlsr.getAttributeValue(null, "shortCircuitDuration")),
                                                           parseDoubleIfNotNull(xmlsr.getAttributeValue(null, "shortCircuitDistance")),
                                                           xmlsr.getAttributeValue(null, "shortCircuitSide"));
                                contingency.getBranches().put(branch.getId(), branch);
                                break;
                            case "generator":
                                Generator generator = new Generator(xmlsr.getAttributeValue(null, "id"),
                                                                    parseDoubleIfNotNull(xmlsr.getAttributeValue(null, "shortCircuitDuration")));
                                contingency.getGenerators().put(generator.getId(), generator);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        switch (xmlsr.getLocalName()) {
                            case "contingency":
                                contingency = null;
                                break;
                        }
                        break;
                }
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static SimulationDetailedParameters load(String fileName) {
        SimulationDetailedParameters parameters = null;
        Path file = PlatformConfig.CONFIG_DIR.resolve(fileName);
        if (Files.exists(file)) {
            parameters = new SimulationDetailedParameters(fileName);
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                parse(reader, parameters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parameters;
    }

}
