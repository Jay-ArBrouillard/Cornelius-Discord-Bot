package Utils;


/**
 * https://www.daysofwonder.com/online/en/play/ranking/
 * Let's call rA the starting score of player A, and nA his number of games. Same thing with rB and nB.
 * s1 = 1 if A wins, -1 if A loses, and 0 for a stalemate
 * s2 = 1 if A wins, 0 if A loses, and 0.5 for a stalemate
 */
public class EloRanking {
    public static double calculateProbabilityOfWin(int rA, int rB) {
        return 1.0 / (1.0 + Math.pow(10.0, ((rB - rA) / 400.0) ));
    }

    public static int calculateProvisionalVsProvisional(int rA, int nA, int rB, int s1) {
        return (int)( (rA * nA + (rA + rB)/2.0 + 100 * s1) / (nA + 1.0));
    }

    public static int calculateProvisionalVsEstablished(int rA, int nA, int rB, int s1) {
        return (int)( (rA * nA + rB + 200.0 * s1) / (nA + 1.0));
    }

    public static int calculateEstablishedVsProvisional(int rA, int rB, int nB, double s2) {
        return (int)(rA + determineK(rA) * (nB / 20.0) * (s2 - (1.0 / (1.0 + Math.pow(10.0,((rB - rA) / 400.0)) ) )));
    }

    public static int calculateEstablishedVsEstablished(int rA, int rB, double s2) {
        return (int) (rA + determineK(rA) * (s2 - (1.0 / (1.0 + Math.pow(10.0, ((rB - rA) / 400.0))))));
    }

    /**
     * Modified K factor from similarly used by the USCF
     * @param rating
     * @return
     */
    public static int determineK(int rating) {
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
