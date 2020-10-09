package Utils;

public class EloStatus {
    public double prevElo;
    public double currElo;
    public double totalGamesPlayed;

    public EloStatus(double prevElo, double currElo, double totalGamesPlayed) {
        this.prevElo = prevElo;
        this.currElo = currElo;
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public EloStatus() {

    }
}
