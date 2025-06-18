public class Model1 {
    @Bind
    private int LL; // Number of years

    @Bind
    private double[] twKI; // Growth rate of private consumption
    @Bind
    private double[] twKS; // Growth rate of public consumption
    @Bind
    private double[] twINW; // Investment growth
    @Bind
    private double[] twEKS; // Export growth
    @Bind
    private double[] twIMP; // Import growth

    @Bind
    private double[] KI; // Private consumption
    @Bind
    private double[] KS; // Public consumption
    @Bind
    private double[] INW; // Investments
    @Bind
    private double[] EKS; // Exports
    @Bind
    private double[] IMP; // Imports
    @Bind
    private double[] PKB; // GDP


    public Model1() {}

    public void run() {
        PKB = new double[LL];
        PKB[0] = KI[0] + KS[0] + INW[0] + EKS[0] - IMP[0];
        for (int t = 1; t < LL; t++) {
            KI[t] = twKI[t] * KI[t - 1];
            KS[t] = twKS[t] * KS[t - 1];
            INW[t] = twINW[t] * INW[t - 1];
            EKS[t] = twEKS[t] * EKS[t - 1];
            IMP[t] = twIMP[t] * IMP[t - 1];
            PKB[t] = KI[t] + KS[t] + INW[t] + EKS[t] - IMP[t];
        }
    }


}
