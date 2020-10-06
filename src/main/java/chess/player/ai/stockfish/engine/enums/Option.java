package chess.player.ai.stockfish.engine.enums;

public enum Option {
    Contempt("Contempt"),
    Analysis_Contempt("Analysis Contempt"),
    Threads("Threads"),
    Hash("Hash"),
    Clear_Hash("Clear Hash"),
    Ponder("Ponder"),
    MultiPV("MultiPV"),
    Skill_Level("Skill Level"),
    Move_Overhead("Move Overhead"),
    Minimum_Thinking_Time("Minimum Thinking Time"),
    Slow_Mover("Slow Mover"),
    Nodestime("nodestime");

    private String optionString;
    private long value;

    Option(String option) {
        optionString = option;
    }

    public Option setValue(long value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "setoption name " + optionString + " value " + value;
    }
}
