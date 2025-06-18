import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


public class maks {
    @Bind
    private int LL; // Количество лет

    @Bind
    private double[] twKI; // Темп роста частного потребления
    @Bind
    private double[] twKS; // Темп роста государственного потребления
    @Bind
    private double[] twINW; // Темп роста инвестиций
    @Bind
    private double[] twEKS; // Темп роста экспорта
    @Bind
    private double[] twIMP; // Темп роста импорта

    @Bind
    private double[] KI; // Частное потребление
    @Bind
    private double[] KS; // Государственное потребление
    @Bind
    private double[] INW; // Инвестиции
    @Bind
    private double[] EKS; // Экспорт
    @Bind
    private double[] IMP; // Импорт
    @Bind
    private double[] PKB; // ВВП



    public maks() {}

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
