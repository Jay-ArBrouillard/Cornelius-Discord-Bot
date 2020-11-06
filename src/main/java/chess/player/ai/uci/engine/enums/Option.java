package chess.player.ai.uci.engine.enums;

public enum Option {
    Contempt("Contempt"),
    Analysis_Contempt("Analysis Contempt"),
    Threads("Threads"),
    Hash("Hash"),
    Clear_Hash("Clear Hash"),
    Ponder("Ponder"),
    MultiPV("MultiPV"),
    Skill_Level("Skill Level"), //Used only for Stockfihs
    Move_Overhead("Move Overhead"),
    Minimum_Thinking_Time("Minimum Thinking Time"),
    Slow_Mover("Slow Mover"),
    Nodestime("nodestime"),
    Limit_Strength("UCI_LimitStrength"),
    Elo("UCI_Elo"),
    Table_Memory("Table Memory"), //Used only for Komodo
    Book_File("Book File"),
    Use_NNUE("Use NNUE"),
    Personality_File("PersonalityFile"); //Used only for Rodent

    private String optionString;
    private Object value;

    Option(String option) {
        optionString = option;
    }

    public String getOptionString() {
        return optionString;
    }

    public Option setValue(Object value) {
        this.value = value;
        return this;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "setoption name " + optionString + " value " + value;
    }
}
