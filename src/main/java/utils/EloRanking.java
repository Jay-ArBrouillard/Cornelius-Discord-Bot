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
     *  https://boardgames.stackexchange.com/questions/4561/how-are-numerical-chess-rankings-calculated-for-different-ranking-systems
     *  Uses 200
     * @param rA
     * @param nA
     * @param rB
     * @param nB
     * @param isWin
     * @param isDraw
     * @return
     */
    public static double calculateProvisionalVsProvisional(double rA, double nA, double rB, double nB, boolean isWin, boolean isDraw) {
        double newRating = rA;
        if (nA == 1) { //First provisional game
            if (nB == 1) { // New rating is hard coded values if both players first game
                if (isDraw) {
                    newRating = 1500;
                }
                else {
                    if (isWin) {
                        newRating = 1700;
                    }
                    else {
                        newRating = 1300;
                    }
                }
            }
            else { //The elo differences don't apply
                if (isDraw) {
                    newRating = rB;
                }
                else {
                    if (isWin) {
                        newRating = 200 + rB;
                    }
                    else {
                        newRating = rB - 200;
                    }
                }
            }
        }
        else { //Every game after the first game
            if (isDraw) {
                newRating = rB;
            }
            else {
                if (isWin) {
                    //Only award elo if the opponent's rating is no less than 400 points of yours
                    if (rB < rA && (rA - rB) < 400) {
                        newRating = 200 + rB;
                    }
                }
                else { //Loss
                    //Only award elo if the opponent's rating is no more than 400 points of yours
                    if (rB > rA && (rB - rA) < 400) {
                        newRating = rB - 200;
                    }
                }
            }
        }

        return newRating;
    }

    /**
     * https://boardgames.stackexchange.com/questions/4561/how-are-numerical-chess-rankings-calculated-for-different-ranking-systems
     * Uses 400
     * @param rA
     * @param nA
     * @param rB
     * @param isWin
     * @param isDraw
     * @return
     */
    public static double calculateProvisionalVsEstablished(double rA, double nA, double rB, boolean isWin, boolean isDraw) {
        double newRating = rA;
        if (nA == 1) { //First provisional game
            //The elo differences don't apply
            if (isDraw) {
                newRating = rB;
            }
            else {
                if (isWin) {
                    newRating = 400 + rB;
                }
                else {
                    newRating = rB - 400;
                }
            }
        }
        else { //Every game after the first game
            if (isDraw) {
                newRating = rB;
            }
            else {
                if (isWin) {
                    //Only award elo if the opponent's rating is no less than 400 points of yours
                    if (rB < rA && (rA - rB) < 400) {
                        newRating = 400 + rB;
                    }
                }
                else { //Loss
                    //Only subtract elo if the opponent's rating is no more than 400 points of yours
                    if (rB > rA && (rB - rA) < 400) {
                        newRating = rB - 400;
                    }
                }
            }
        }

        return newRating;
    }

    /**
     * r'A = rA + K * (nB / 20) * (s2 - (1 / (1 + 10^((rB - rA) / 400) )))
     * @param rA
     * @param rB
     * @param nB
     * @param s2
     * @return
     */
    public static double calculateEstablishedVsProvisional(double rA, int nA, double rB, double nB, double s2) {
        double probability = s2 - calculateProbabilityOfWin(rA, rB);
        return rA + determineK(rA, nA) * (nB / 20.0) * probability;
    }

    /**
     * r'A = rA + K * (s2 - (1 / (1 + 10^((rB - rA) / 400) )))
     * @param rA
     * @param rB
     * @param s2
     * @return
     */
    public static double calculateEstablishedVsEstablished(double rA, int nA, double rB, double s2) {
        double probability = s2 - calculateProbabilityOfWin(rA, rB);
        return rA + determineK(rA, nA) * probability;
    }

    /**
     * Modified from USCF
     * @param rating
     * @return
     */
    public static double determineK(double rating, int numGames) {
        double K;
        if (numGames < 30 && rating < 2300) {
            //For 10 games after a new players provisional period they can still significantly influence their elo
            K = 40;
        }
        else if (rating < 2100) { // 0-2099
            K = 32;
        }
        else if (rating <= 2400) { //2100 - 2400
            K = 24;
        }
        else { //2401+
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
                c.elo = EloRanking.calculateEstablishedVsEstablished(c.elo, c.totalGames, oPrevElo, 0.5);
            }
            else if (!c.provisional && o.provisional) {
                c.elo = EloRanking.calculateEstablishedVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames,0.5);
            }
            else if (c.provisional && !o.provisional) {
                c.elo = EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, false, true);
            }
            else {
                c.elo = EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames, false, true);
            }
        }
        else {
            if (isWin) {
                if (!c.provisional && !o.provisional) {
                    c.elo = EloRanking.calculateEstablishedVsEstablished(c.elo, c.totalGames, oPrevElo, 1.0);
                }
                else if (!c.provisional && o.provisional) {
                    c.elo = EloRanking.calculateEstablishedVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames,1.0);
                }
                else if (c.provisional && !o.provisional) {
                    c.elo = EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, true, false);
                }
                else {
                    c.elo = EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames, true, false);
                }
            }
            else { //Loss
                if (!c.provisional && !o.provisional) {
                    c.elo = EloRanking.calculateEstablishedVsEstablished(c.elo, c.totalGames, oPrevElo, 0.0);
                }
                else if (!c.provisional && o.provisional) {
                    c.elo = EloRanking.calculateEstablishedVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames,0.0);
                }
                else if (c.provisional && !o.provisional) {
                    c.elo = EloRanking.calculateProvisionalVsEstablished(c.elo, c.totalGames, oPrevElo, false, false);
                }
                else {
                    c.elo = EloRanking.calculateProvisionalVsProvisional(c.elo, c.totalGames, oPrevElo, o.totalGames, false, false);
                }
            }
        }
        System.out.println("New elo no round:" + c.elo);
        c.elo = Math.round(c.elo); // Round double to nearest integer
        if (c.provisional && c.totalGames == 20) c.provisional = false;
        if (c.totalGames < 20) {
            c.highestElo = null;
        }
        else if (c.totalGames == 20) {
            c.highestElo = c.elo;
        }
        else if (c.elo > c.highestElo) {
            c.highestElo = c.elo;
        }
        c.determineTitle();
    }
}
