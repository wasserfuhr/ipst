/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.cases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSourceFactory;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.ucte.util.EntsoeFileName;
import eu.itesla_project.ucte.util.EntsoeGeographicalCode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common ENTSOE case repository layout:
 * <pre>
 * CIM/SN/2013/01/15/20130115_0620_SN2_FR0.zip
 *    /FO/...
 * UCT/SN/...
 *    /FO/...
 * </pre>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepository implements CaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntsoeCaseRepository.class);

    static class EntsoeFormat {

        private final Importer importer;

        private final String dirName;

        EntsoeFormat(Importer importer, String dirName) {
            this.importer = Objects.requireNonNull(importer);
            this.dirName = Objects.requireNonNull(dirName);
        }

        Importer getImporter() {
            return importer;
        }

        String getDirName() {
            return dirName;
        }
    }

    private final EntsoeCaseRepositoryConfig config;

    private final List<EntsoeFormat> formats;

    private final ReadOnlyDataSourceFactory dataSourceFactory;

    public static CaseRepository create(ComputationManager computationManager) {
        return new EntsoeCaseRepository(EntsoeCaseRepositoryConfig.load(), computationManager);
    }

    EntsoeCaseRepository(EntsoeCaseRepositoryConfig config, List<EntsoeFormat> formats, ReadOnlyDataSourceFactory dataSourceFactory) {
        this.config = Objects.requireNonNull(config);
        this.formats = Objects.requireNonNull(formats);
        this.dataSourceFactory = Objects.requireNonNull(dataSourceFactory);
        LOGGER.info(config.toString());
    }

    EntsoeCaseRepository(EntsoeCaseRepositoryConfig config, ComputationManager computationManager) {
        this(config,
                Arrays.asList(new EntsoeFormat(Importers.getImporter("CIM1", computationManager), "CIM"),
                              new EntsoeFormat(Importers.getImporter("UCTE", computationManager), "UCT")), // official ENTSOE formats)
                (directory, baseName) -> new GenericReadOnlyDataSource(directory, baseName));
    }

    public EntsoeCaseRepositoryConfig getConfig() {
        return config;
    }

    private static class ImportContext {
        private final Importer importer;
        private final ReadOnlyDataSource ds;

        private ImportContext(Importer importer, ReadOnlyDataSource ds) {
            this.importer = importer;
            this.ds = ds;
        }
    }

    // because D1 snapshot does not exist and forecast replacement is not yet implemented
    private static Collection<EntsoeGeographicalCode> forCountryHacked(Country country) {
        return EntsoeGeographicalCode.forCountry(country).stream()
                .filter(ucteGeographicalCode -> ucteGeographicalCode != EntsoeGeographicalCode.D1)
                .collect(Collectors.toList());
    }

    public static String intraForecastDistanceInHoursSx(CaseType ct) {
        return ct.name().substring(4,6);
    }

    private final static List<String> entsoeFoFiletypes = Arrays.asList("FO", "2D", "LR", "RE",
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
    private final static List<String> entsoeSnFiletypes = Arrays.asList("SN");


    // if FO and forecastDistance is not specified, return dayahead
    private <R> R scanRepository(DateTime date, CaseType type, Country country, Function<List<ImportContext>, R> handler) {
        return scanRepository(date, type, null, country, handler);
    }

    private <R> R scanRepository(DateTime date, CaseType type, Integer forecastDistance, Country country, Function<List<ImportContext>, R> handler) {
        if ((type == CaseType.SN) && (forecastDistance !=null) && (forecastDistance > 0)) {
            throw new IllegalArgumentException("forecastDistance must be 0, for a CaseType.SN");
        }
        Collection<EntsoeGeographicalCode> geographicalCodes = country != null ? forCountryHacked(country)
                : Arrays.asList(EntsoeGeographicalCode.UX, EntsoeGeographicalCode.UC);
        DateTime testDate1=date.minusHours(1);
        String typeID=type.name();
        List<String> entsoeFileTypes=(type == CaseType.FO) ? entsoeFoFiletypes : entsoeSnFiletypes;

        for (EntsoeFormat format : formats) {
            Path formatDir = config.getRootDir().resolve(format.getDirName());
            if (Files.exists(formatDir)) {
                Path typeDir = formatDir.resolve(type.name());
                if (Files.exists(typeDir)) {
                    Path dayDir = typeDir.resolve(String.format("%04d", date.getYear()))
                            .resolve(String.format("%02d", date.getMonthOfYear()))
                            .resolve(String.format("%02d", date.getDayOfMonth()));
                    if (Files.exists(dayDir)) {
                        List<ImportContext> importContexts = null;
                        for (EntsoeGeographicalCode geographicalCode : geographicalCodes) {
                            Collection<String> forbiddenFormats = config.getForbiddenFormatsByGeographicalCode().get(geographicalCode);
                            if (!forbiddenFormats.contains(format.getImporter().getFormat())) {
                                for (int i = 9; i >= 0; i--) {
                                    for (String entsoeFiletype:  entsoeFileTypes ){
                                        String baseName = String.format("%04d%02d%02d_%02d%02d_" + entsoeFiletype + "%01d_" + geographicalCode.name() + "%01d",
                                                date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(),
                                                date.getDayOfWeek(), i);
                                        if (testDate1.getHourOfDay() == date.getHourOfDay()) {
                                            baseName = baseName.substring(0, 9) + 'B' + baseName.substring(10);
                                        }
                                        ReadOnlyDataSource ds = dataSourceFactory.create(dayDir, baseName);
                                        if (importContexts == null) {
                                            importContexts = new ArrayList<>();
                                        }
                                        EntsoeFileName efileName=EntsoeFileName.parse(baseName);
                                        if (format.getImporter().exists(ds)) {
                                            boolean importFile=false;
                                            switch (type) {
                                                case SN:
                                                    if (efileName.isSnapshot()) {
                                                        importFile=true;
                                                    }
                                                    break;
                                                case FO:
                                                    // default is day ahead forecasts, when forecastDistance is null
                                                    if ((forecastDistance == null) && (efileName.isDayAhead())) {
                                                        importFile=true;
                                                    } else if ((forecastDistance != null) && (efileName.getForecastDistance() == forecastDistance)) {
                                                        importFile=true;
                                                    }
                                                    break;
                                            }
                                            if (importFile) {
                                                importContexts.add(new ImportContext(format.getImporter(), ds));
                                            }
                                        }
                                    }
                                }
                                if (importContexts.size()==0 ) {  // for info purposes, only
                                    String baseName1 = String.format("%04d%02d%02d_%02d%02d_" + "["+String.join("|", entsoeFileTypes)+ "]" + "%01d_" + geographicalCode.name(),
                                            date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(),
                                            date.getDayOfWeek());
                                    if (testDate1.getHourOfDay() == date.getHourOfDay()) {
                                        baseName1 = baseName1.substring(0,9)+'B'+baseName1.substring(10);
                                    }
                                    LOGGER.warn("could not find any file {}[0-9] in directory {}, with forecastDistance {}", baseName1, dayDir, forecastDistance);
                                }
                            }
                        }
                        if (importContexts != null) {
                            R result = handler.apply(importContexts);
                            if (result != null) {
                                return result;
                            }
                        }
                    } else {
                        LOGGER.debug("could not find any (daydir) directory {}", dayDir);
                    }
                } else {
                    LOGGER.warn("could not find any (typedir) directory {}", typeDir);
                }
            } else {
                LOGGER.warn("could not find any (formatdir) directory {}", formatDir);
            }
        }
        return null;
    }

    private static DateTime toCetDate(DateTime date) {
        DateTimeZone CET = DateTimeZone.forID("CET");
        if (!date.getZone().equals(CET)) {
            return date.toDateTime(CET);
        }
        return date;
    }

    private List<Network> loadNetworks(DateTime date, CaseType type, Integer forecastDistance, Country country) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        List<Network> networks2 = scanRepository(toCetDate(date), type, forecastDistance, country, importContexts -> {
            List<Network> networks = null;
            if (importContexts.size() > 0) {
                networks = new ArrayList<>();
                for (ImportContext importContext : importContexts) {
                    LOGGER.info("Loading {} in {} format", importContext.ds.getBaseName(), importContext.importer.getFormat());
                    networks.add(importContext.importer.import_(importContext.ds, null));
                }
            }
            return networks;
        });
        return networks2 == null ? Collections.emptyList() : networks2;
    }


    @Override
    public List<Network> load(DateTime date, CaseType type, Country country) {
        return loadNetworks(date, type, null, country);
    }


    @Override
    public List<Network> load(DateTime date, CaseType type, int forecastDistance, Country country) {
        return loadNetworks(date, type, forecastDistance, country);
    }


	@Override
	public boolean isDataAvailable(DateTime date, CaseType type, Country country) {
		return isNetworkDataAvailable(date, type, country);
	}

    @Override
    public boolean isDataAvailable(DateTime date, CaseType type, int forecastDistance, Country country) {
        return isNetworkDataAvailable(date, type, forecastDistance, country);
    }

    private boolean isNetworkDataAvailable(DateTime date, CaseType type,Country country) {
        return isNetworkDataAvailable(date,type,null,country);
    }

    private boolean isNetworkDataAvailable(DateTime date, CaseType type, Integer forecastDistance, Country country) {
		Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        return scanRepository(toCetDate(date), type, forecastDistance, country, importContexts -> {
            if (importContexts.size() > 0) {
                for (ImportContext importContext : importContexts) {
                    if (importContext.importer.exists(importContext.ds)) {
                        return true;
                    }
                }
                return null;
            }
            return null;
        }) != null;
	}

    private void browse(Path dir, Consumer<Path> handler) {
        try (Stream<Path> stream = Files.list(dir)) {
            stream.sorted().forEach(child -> {
                if (Files.isDirectory(child)) {
                    browse(child, handler);
                } else {
                    try {
                        if (Files.size(child) > 0) {
                            handler.accept(child);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //if casetype is FO, check only dayaheads
    @Override
    public Set<DateTime> dataAvailable(CaseType type, Set<Country> countries, Interval interval) {
        Set<EntsoeGeographicalCode> geographicalCodes = new HashSet<>();
        if (countries == null) {
            geographicalCodes.add(EntsoeGeographicalCode.UX);
            geographicalCodes.add(EntsoeGeographicalCode.UC);
        } else {
            for (Country country : countries) {
                geographicalCodes.addAll(forCountryHacked(country));
            }
        }
        Multimap<DateTime, EntsoeGeographicalCode> dates = HashMultimap.create();

        String typeDirS=type.name();

        for (EntsoeFormat format : formats) {
            Path formatDir = config.getRootDir().resolve(format.getDirName());
            if (Files.exists(formatDir)) {
                Path typeDir = formatDir.resolve(type.name());
                if (Files.exists(typeDir)) {
                    browse(typeDir, path -> {
                        EntsoeFileName entsoeFileName = EntsoeFileName.parse(path.getFileName().toString());
                        EntsoeGeographicalCode geographicalCode = entsoeFileName.getGeographicalCode();
                        if (geographicalCode != null
                                && !config.getForbiddenFormatsByGeographicalCode().get(geographicalCode).contains(format.getImporter().getFormat())
                                && interval.contains(entsoeFileName.getDate())) {
                            //LOGGER.debug(entsoeFileName.getDate() +", " + entsoeFileName.getForecastDistance() +", "+ path.getFileName().toString());
                            boolean addFile=false;
                            if ((type == CaseType.SN) && (entsoeFileName.isSnapshot())) { //snapshot case
                                addFile=true;
                            } else if ((type == CaseType.FO) && (entsoeFileName.isDayAhead())) { // FO dayahead(dacf) case
                                addFile=true;
                            }
                            if (addFile == true) {
                                //LOGGER.debug(" + "+ entsoeFileName.getDate() +", " + entsoeFileName.getForecastDistance() +", "+ path.getFileName().toString());
                                dates.put(entsoeFileName.getDate(), geographicalCode);
                            }
                        }
                    });
                }
            }
        }
        return dates.asMap().entrySet().stream()
                .filter(e -> new HashSet<>(e.getValue()).containsAll(geographicalCodes))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
