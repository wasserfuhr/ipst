/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.impl.util.Ref;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationImpl extends IdentifiableImpl<Substation> implements Substation {

    private static final Function<VoltageLevelExt, Iterable<TwoWindingsTransformer>> toTwoWindingsTransformers
            = new Function<VoltageLevelExt, Iterable<TwoWindingsTransformer>>() {
        @Override
        public Iterable<TwoWindingsTransformer> apply(VoltageLevelExt vl) {
            return vl.getConnectables(TwoWindingsTransformer.class);
        }
    };

    private static final Function<VoltageLevelExt, Iterable<ThreeWindingsTransformer>> toThreeWindingsTransformers
            = new Function<VoltageLevelExt, Iterable<ThreeWindingsTransformer>>() {
        @Override
        public Iterable<ThreeWindingsTransformer> apply(VoltageLevelExt vl) {
            return vl.getConnectables(ThreeWindingsTransformer.class);
        }
    };

    private Country country;

    private String tso;

    private final Ref<NetworkImpl> networkRef;

    private final Set<String> geographicalTags = new LinkedHashSet<>();

    private final Set<VoltageLevelExt> voltageLevels = new LinkedHashSet<>();

    SubstationImpl(String id, String name, Country country, String tso, Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.country = country;
        this.tso = tso;
        this.networkRef = networkRef;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SUBSTATION;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    @Override
    public SubstationImpl setCountry(Country country) {
        ValidationUtil.checkCountry(this, country);
        Country oldValue = this.country;
        this.country = country;
        getNetwork().getListeners().notifyUpdate(this, "country", oldValue.toString(), country.toString());
        return this;
    }

    @Override
    public String getTso() {
        return tso;
    }

    @Override
    public SubstationImpl setTso(String tso) {
        String oldValue = this.tso;
        this.tso = tso;
        getNetwork().getListeners().notifyUpdate(this, "tso", oldValue, tso);
        return this;
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    void addVoltageLevel(VoltageLevelExt voltageLevel) {
        voltageLevels.add(voltageLevel);
    }

    @Override
    public VoltageLevelAdderImpl newVoltageLevel() {
        return new VoltageLevelAdderImpl(this);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Collections.<VoltageLevel>unmodifiableSet(voltageLevels);
    }

    @Override
    public TwoWindingsTransformerAdderImpl newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderImpl(this);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return FluentIterable.from(voltageLevels)
                             .transformAndConcat(toTwoWindingsTransformers)
                             .toSet();
    }

    @Override
    public ThreeWindingsTransformerAdderImpl newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderImpl(this);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return FluentIterable.from(voltageLevels)
                             .transformAndConcat(toThreeWindingsTransformers)
                             .toSet();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return Collections.unmodifiableSet(geographicalTags);
    }

    @Override
    public Substation addGeographicalTag(String tag) {
        if (tag == null) {
            throw new ValidationException(this, "geographical tag is null");
        }
        geographicalTags.add(tag);
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }

}
