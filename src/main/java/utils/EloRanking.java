package utils;


import chess.ChessGameState;
import chess.tables.ChessPlayer;

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

    public static void calculateChessElo(ChessGameState state, ChessPlayer whiteSidePlayer, ChessPlayer blackSidePlayer) {
        String winnerId = state.getWinnerId();
        if (winnerId == null) { //Draw
            calculateChessEloSingle(true, false, whiteSidePlayer, state.getPrevElo().get(blackSidePlayer.discordId), blackSidePlayer);
            calculateChessEloSingle(true, false, blackSidePlayer, state.getPrevElo().get(whiteSidePlayer.discordId), whiteSidePlayer);
        }
        else if (winnerId.equals(whiteSidePlayer.discordId)) { //White side white
            calculateChessEloSingle(false, true, whiteSidePlayer, state.getPrevElo().get(blackSidePlayer.discordId), blackSidePlayer);
            calculateChessEloSingle(false, false, blackSidePlayer, state.getPrevElo().get(whiteSidePlayer.discordId), whiteSidePlayer);
        }
        else if (winnerId.equals(blackSidePlayer.discordId)) { //Black side white
            calculateChessEloSingle(false, false, whiteSidePlayer, state.getPrevElo().get(blackSidePlayer.discordId), blackSidePlayer);
            calculateChessEloSingle(false, true, blackSidePlayer, state.getPrevElo().get(whiteSidePlayer.discordId), whiteSidePlayer);
        }
        else {
            new RuntimeException("Error calculating chess elo");
        }
    }

    public static void calculateChessEloSingle(boolean isDraw, boolean isWin, ChessPlayer c, double oPrevElo, ChessPlayer o) {
        if (isDraw) {
            if (!c.provisional && !o.provisional) {
                c.elo = EloRanking.calculateEstablishedVsEstablished(c.elo, oPrevElo, 0.5);
            }
            else if (!c.provisional && o.provisional) {
                c.elo = EloRanking.calculateEstablishedVsProvisional(c.elo, oPrevElo, o.totalGames,0.5);
            }
            else if (c.provisional && !o.provisional) {
                c.elo = EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, 0.0);
            }
            else {
                c.elo = EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, 0.0);
            }
        }
        else {
            if (isWin) {
                // Never let their elo decrease after a win
                if (!c.provisional && !o.provisional) {
                    c.elo = Math.max(c.elo, EloRanking.calculateEstablishedVsEstablished(c.elo, oPrevElo, 1.0));
                }
                else if (!c.provisional && o.provisional) {
                    c.elo = Math.max(c.elo, EloRanking.calculateEstablishedVsProvisional(c.elo, oPrevElo, o.totalGames,1.0));
                }
                else if (c.provisional && !o.provisional) {
                    c.elo = Math.max(c.elo, EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, 1.0));
                }
                else {
                    c.elo = Math.max(c.elo, EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, 1.0));
                }
            }
            else { //Loss
                // Never let their elo decrease lower than 100
                if (!c.provisional && !o.provisional) {
                    c.elo = Math.max(100, EloRanking.calculateEstablishedVsEstablished(c.elo, oPrevElo, 0.0));
                }
                else if (!c.provisional && o.provisional) {
                    c.elo = Math.max(100, EloRanking.calculateEstablishedVsProvisional(c.elo, oPrevElo, o.totalGames,0.0));
                }
                else if (c.provisional && !o.provisional) {
                    c.elo = Math.max(100, EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, -1.0));
                }
                else {
                    c.elo = Math.max(100, EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, -1.0));
                }
            }
        }
        c.elo = Math.round(c.elo); // Round double to nearest integer
        if (c.provisional && c.totalGames > 20) c.provisional = false;
        if (c.totalGames <= 20) {
            c.highestElo = null;
        }
        else if (c.totalGames == 21) {
            c.highestElo = c.elo;
        }
        else if (c.elo > c.highestElo) {
            c.highestElo = c.elo;
        }
        c.determineTitle();
    }
}
