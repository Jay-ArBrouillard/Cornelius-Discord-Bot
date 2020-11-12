package chess.tables;

public class ChessPlayer {
    public final String discordId;
    public final String name;
    public double elo;
    public Double highestElo;
    public boolean provisional;
    public String title;
    public int wins;
    public int losses;
    public int draws;
    public double ratio;
    public int totalGames;
    public String totalGameTimeStr;
    public final String createdOn;
    public String updatedOn;

    public ChessPlayer(String discordId, String name, double elo, Double highestElo, boolean provisional, String title, int wins, int losses, int draws, double ratio, int totalGames, String totalGameTimeStr, String createdOn, String updatedOn) {
        this.discordId = discordId;
        this.name = name;
        this.elo = elo;
        this.highestElo = highestElo;
        this.provisional = provisional;
        this.title = title;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.ratio = ratio;
        this.totalGames = totalGames;
        this.totalGameTimeStr = totalGameTimeStr;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public void incrementWins() {
        this.wins++;
        this.totalGames++;
        updateRatio();
    }

    public void incrementLosses() {
        this.losses++;
        this.totalGames++;
        updateRatio();
    }

    public void incrementDraws() {
        this.draws++;
        this.totalGames++;
        updateRatio();
    }

    private void updateRatio() {
        this.ratio = this.losses != 0 ? (this.wins + this.draws) / (double) this.losses : (this.wins + this.draws) / 1.0 ;
    }

    public void determineTitle() {
        if ("Grandmaster (GM)".equals(this.title)) { //Grandmasters never lose this title regardless of elo
            return;
        }
        if (this.elo >= 2500) {
            this.title = "Grandmaster (GM)";
        }
        else if (this.elo >= 2400) {
            this.title = "International Master (IM)";
        }
        else if (this.elo >= 2300) {
            this.title = "FIDE Master (FM)";
        }
        else if (this.elo >= 2200) {
            this.title = "Senior Master (SM)";
        }
        else if (this.elo >= 2100) {
            this.title = "Master (M)";
        }
        else if (this.elo >= 2000) {
            this.title = "Candidate Master (CM)";
        }
        else if (this.elo >= 1800) {
            this.title = "Class A";
        }
        else if (this.elo >= 1600) {
            this.title = "Class B";
        }
        else if (this.elo >= 1400) {
            this.title = "Class C";
        }
        else if (this.elo >= 1200) {
            this.title = "Class D";
        }
        else if (this.elo >= 1000) {
            this.title = "Class E";
        }
        else if (this.elo >= 800) {
            this.title = "Class F";
        }
        else if (this.elo >= 600) {
            this.title = "Class G";
        }
        else if (this.elo >= 400) {
            this.title = "Class H";
        }
        else if (this.elo >= 200) {
            this.title = "Class I";
        }
        else {
            this.title = "Class J";
        }
    }
}
