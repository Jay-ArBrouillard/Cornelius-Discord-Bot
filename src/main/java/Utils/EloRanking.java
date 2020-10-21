package Utils;


/**
 * https://www.daysofwonder.com/online/en/play/ranking/
 * Let's call rA the starting score of player A, and nA his number of games. Same thing with rB and nB.
 * s1 = 1 if A wins, -1 if A loses, and 0 for a stalemate
 * s2 = 1 if A wins, 0 if A loses, and 0.5 for a stalemate
 */
public class EloRanking {
    public static double calculateProbabilityOfWin(double rA, double rB) {
        double exponent = (rB - rA) / 400.0;
        double probabilityDenominator = 1.0 + Math.pow(10.0, exponent);
        return 1.0 / probabilityDenominator;
    }

    /**
     * r'A = (rA * nA + ((rA + rB) / 2) + 100 * s1) / (nA + 1)
     * @param rA
     * @param nA
     * @param rB
     * @param s1
     * @return
     */
    public static double calculateProvisionalVsProvisional(double rA, double nA, double rB, double s1) {
        double subNumerator = (rA + rB) / 2.0;
        double numerator = (rA * nA) + subNumerator + (100.0 * s1);
        double denominator = nA + 1.0;
        return numerator / denominator;
    }

    /**
     * r'A = (rA * nA + rB + 200 * s1) / (nA + 1)
     * @param rA
     * @param nA
     * @param rB
     * @param s1
     * @return
     */
    public static double calculateProvisionalVsEstablished(double rA, double nA, double rB, double s1) {
        double numerator = (rA * nA) + rB + (200.0 * s1);
        double denominator = nA + 1.0;
        return numerator / denominator;
    }

    /**
     * r'A = rA + K * (nB / 20) * (s2 - (1 / (1 + 10^((rB - rA) / 400) )))
     * @param rA
     * @param rB
     * @param nB
     * @param s2
     * @return
     */
    public static double calculateEstablishedVsProvisional(double rA, double rB, double nB, double s2) {
        double probability = s2 - calculateProbabilityOfWin(rA, rB);
        return rA + determineK(rA) * (nB / 20.0) * probability;
    }

    /**
     * r'A = rA + K * (s2 - (1 / (1 + 10^((rB - rA) / 400) )))
     * @param rA
     * @param rB
     * @param s2
     * @return
     */
    public static double calculateEstablishedVsEstablished(double rA, double rB, double s2) {
        double probability = s2 - calculateProbabilityOfWin(rA, rB);
        return rA + determineK(rA) * probability;
    }

    /**
     * Modified K factor from similarly used by the USCF
     * @param rating
     * @return
     */
    public static double determineK(double rating) {
        int K;
        if (rating < 2100) { // 0-2099
            K = 40;
        }
        else if (rating < 2200) { //2100 - 2199
            K = 32;
        }
        else if (rating < 2400) { //2200 - 2399
            K = 24;
        }
        else { // >= 2400
            K = 16;
        }

        return K;
    }
}
