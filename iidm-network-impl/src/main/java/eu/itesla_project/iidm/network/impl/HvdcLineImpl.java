package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
class HvdcLineImpl extends IdentifiableImpl implements HvdcLine, Stateful {

    private float r;

    private float nominalV;

    private float maxP;

    // attributes depending on the state

    private final TFloatArrayList targetP;

    private final TIntArrayList numberOfPoleInService;

    //

    private final Ref<NetworkImpl> networkRef;

    HvdcLineImpl(String id, String name, float r, float nominalV, float maxP, float targetP, int numberOfPoleInService,
                 Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int stateArraySize = networkRef.get().getStateManager().getStateArraySize();
        this.targetP = new TFloatArrayList(stateArraySize);
        this.numberOfPoleInService = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.targetP.add(targetP);
            this.numberOfPoleInService.add(numberOfPoleInService);
        }
        this.networkRef = networkRef;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    public float getR() {
        return r;
    }

    @Override
    public HvdcLine setR(float r) {
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
    public HvdcLine setNominalV(float nominalV) {
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
    public HvdcLine setMaxP(float maxP) {
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
    public HvdcLine setTargetP(float targetP) {
        ValidationUtil.checkTargetP(this, targetP);
        float oldValue = this.targetP.set(getNetwork().getStateIndex(), targetP);
        notifyUpdate("targetP", oldValue, targetP);
        return this;
    }

    @Override
    public int getNumberOfPoleInService() {
        return numberOfPoleInService.get(getNetwork().getStateIndex());
    }

    @Override
    public HvdcLine setNumberOfPoleInService(int numberOfPoleInService) {
        ValidationUtil.checkNumberOfPoleInService(this, numberOfPoleInService);
        float oldValue = this.numberOfPoleInService.set(getNetwork().getStateIndex(), numberOfPoleInService);
        notifyUpdate("numberOfPoleInService", oldValue, numberOfPoleInService);
        return this;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return null;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return null;
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {

    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {

    }

    @Override
    public void reduceStateArraySize(int number) {

    }

    @Override
    public void deleteStateArrayElement(int index) {

    }

    @Override
    public void remove() {

    }

    @Override
    protected String getTypeDescription() {
        return "hvdcLine";
    }
}
