package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.BitSet;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
class HvdcLineImpl extends IdentifiableImpl<HvdcLine> implements HvdcLine, Stateful {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private float r;

    private float nominalV;

    private float maxP;

    // attributes depending on the state

    private final BitSet convertersMode;

    private final TFloatArrayList targetP;

    private final BitSet inService;

    //

    private final HvdcConverterStationImpl<?> converterStation1;

    private final HvdcConverterStationImpl<?> converterStation2;

    private final Ref<NetworkImpl> networkRef;

    HvdcLineImpl(String id, String name, float r, float nominalV, float maxP, ConvertersMode convertersMode, float targetP,
                 boolean inService, HvdcConverterStationImpl<?> converterStation1, HvdcConverterStationImpl<?> converterStation2,
                 Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int stateArraySize = networkRef.get().getStateManager().getStateArraySize();
        this.convertersMode = new BitSet(stateArraySize);
        this.convertersMode.set(0, stateArraySize, convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        this.inService = new BitSet(stateArraySize);
        this.inService.set(0, stateArraySize, inService);
        this.targetP = new TFloatArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.targetP.add(targetP);
        }
        this.converterStation1 = converterStation1;
        this.converterStation2 = converterStation2;
        this.networkRef = networkRef;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    private static ConvertersMode toEnum(boolean convertersMode) {
        return convertersMode ? ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER : ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
    }

    private static boolean fromEnum(ConvertersMode convertersMode) {
        return convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return toEnum(convertersMode.get(getNetwork().getStateIndex()));
    }

    @Override
    public HvdcLineImpl setConvertersMode(ConvertersMode convertersMode) {
        ValidationUtil.checkConvertersMode(this, convertersMode);
        int stateIndex = getNetwork().getStateIndex();
        boolean oldValue = this.convertersMode.get(stateIndex);
        this.convertersMode.set(stateIndex, fromEnum(Objects.requireNonNull(convertersMode)));
        notifyUpdate("convertersMode", toEnum(oldValue), convertersMode);
        return this;
    }

    @Override
    public float getR() {
        return r;
    }

    @Override
    public HvdcLineImpl setR(float r) {
        ValidationUtil.checkR(this, r);
        float oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public float getNominalV() {
        return nominalV;
    }

    @Override
    public HvdcLineImpl setNominalV(float nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        float oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public float getMaxP() {
        return maxP;
    }

    @Override
    public HvdcLineImpl setMaxP(float maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        float oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public float getTargetP() {
        return targetP.get(getNetwork().getStateIndex());
    }

    @Override
    public HvdcLineImpl setTargetP(float targetP) {
        ValidationUtil.checkTargetP(this, targetP);
        float oldValue = this.targetP.set(getNetwork().getStateIndex(), targetP);
        notifyUpdate("targetP", oldValue, targetP);
        return this;
    }

    @Override
    public boolean isInService() {
        return inService.get(getNetwork().getStateIndex());
    }

    @Override
    public HvdcLineImpl setInService(boolean inService) {
        int stateIndex = getNetwork().getStateIndex();
        boolean oldValue = this.inService.get(stateIndex);
        this.inService.set(stateIndex, inService);
        notifyUpdate("inService", oldValue, inService);
        return this;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return converterStation1;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return converterStation2;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        targetP.ensureCapacity(targetP.size() + number);
        for (int i = 0; i < number; i++) {
            convertersMode.set(initStateArraySize + i, convertersMode.get(sourceIndex));
            targetP.add(targetP.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        targetP.remove(targetP.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            targetP.set(index, targetP.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getObjectStore().remove(this);
        network.getListeners().notifyRemoval(this);
    }

    @Override
    protected String getTypeDescription() {
        return "hvdcLine";
    }
}
