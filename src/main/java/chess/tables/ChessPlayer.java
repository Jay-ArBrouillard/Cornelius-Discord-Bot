package chess.tables;

import Utils.EloRanking;

public class ChessPlayer {
    public final String discordId;
    public final String name;
    public int elo;
    public boolean provisional;
    public String title;
    public int wins;
    public int losses;
    public int draws;
    public double ratio;
    public int totalGames;
    public String avgGameLength;
    public final String createdOn;
    public String updatedOn;

    public ChessPlayer(String discordId, String name, int elo, boolean provisional, String title, int wins, int losses, int draws, double ratio, int totalGames, String avgGameLength, String createdOn, String updatedOn) {
        this.discordId = discordId;
        this.name = name;
        this.elo = elo;
        this.provisional = provisional;
        this.title = title;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.ratio = ratio;
        this.totalGames = totalGames;
        this.avgGameLength = avgGameLength;
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

    public void calculateElo(boolean isDraw, boolean isWin, ChessPlayer o) {
        if (isDraw) {
            if (!this.provisional && !o.provisional) {
                this.elo = (int)EloRanking.calculateEstablishedVsEstablished(this.elo, o.elo, 0.5);
            }
            else if (!this.provisional && o.provisional) {
                this.elo = (int)EloRanking.calculateEstablishedVsProvisional(this.elo, this.totalGames, o.elo, 0.5);
            }
            else if (this.provisional && !o.provisional) {
                this.elo = (int)EloRanking.calculateProvisionalVsEstablished(this.elo, this.totalGames, o.elo, 0);
            }
            else {
                this.elo = (int)EloRanking.calculateProvisionalVsProvisional(this.elo, this.totalGames, o.elo, 0);
            }
        }
        else {
            if (isWin) {
                if (!this.provisional && !o.provisional) {
                    this.elo = (int)EloRanking.calculateEstablishedVsEstablished(this.elo, o.elo, 1);
                }
                else if (!this.provisional && o.provisional) {
                    this.elo = (int)EloRanking.calculateEstablishedVsProvisional(this.elo, this.totalGames, o.elo, 1);
                }
                else if (this.provisional && !o.provisional) {
                    this.elo = (int)EloRanking.calculateProvisionalVsEstablished(this.elo, this.totalGames, o.elo, 1);
                }
                else {
                    this.elo = (int)EloRanking.calculateProvisionalVsProvisional(this.elo, this.totalGames, o.elo, 1);
                }
            }
            else { //Loss
                if (!this.provisional && !o.provisional) {
                    this.elo = (int)EloRanking.calculateEstablishedVsEstablished(this.elo, o.elo, 0);
                }
                else if (!this.provisional && o.provisional) {
                    this.elo = (int)EloRanking.calculateEstablishedVsProvisional(this.elo, this.totalGames, o.elo, 0);
                }
                else if (this.provisional && !o.provisional) {
                    this.elo = (int)EloRanking.calculateProvisionalVsEstablished(this.elo, this.totalGames, o.elo, -1);
                }
                else {
                    this.elo = (int)EloRanking.calculateProvisionalVsProvisional(this.elo, this.totalGames, o.elo, -1);
                }
            }
        }
        if (this.provisional && this.totalGames >= 20) this.provisional = false;
        determineTitle();
    }

    public void determineTitle() {
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
