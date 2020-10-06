package chess.player.ai.stockfish.engine.enums;

public class Query {
    private QueryType type;
    private String fen, move;
    private int difficulty, depth;
    private long movetime;

    public Query(QueryType type, String fen, String move, int difficulty, int depth, long movetime) {
        this.type = type;
        this.fen = fen;
        this.move = move;
        this.difficulty = difficulty;
        this.depth = depth;
        this.movetime = movetime;
    }

    public QueryType getType() {
        return type;
    }

    public String getFen() {
        return fen;
    }

    public String getMove() {
        return move;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getDepth() {
        return depth;
    }

    public long getMovetime() {
        return movetime;
    }

    public static class Builder {
        private QueryType type;
        private String fen, move;
        private int difficulty = -1, depth = -1;
        private long movetime = -1;

        public Builder(QueryType type) {
            this.type = type;
        }

        public Builder setFen(String fen) {
            this.fen = fen;
            return this;
        }

        public Builder setMove(String move) {
            this.move = move;
            return this;
        }

        public Builder setDifficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setMovetime(long movetime) {
            this.movetime = movetime;
            return this;
        }

        public Query build() {
            if (type == null)
                throw new IllegalStateException("Query type can not be null.");

            if (fen == null)
                throw new IllegalStateException("Query is missing FEN.");

            return new Query(type, fen, move, difficulty, depth, movetime);
        }
    }
}
