package chess.player.ai.uci.engine.enums;

import chess.board.Board;

public class Query {
    private QueryType type;
    private String fen, move;
    private int depth;
    private long movetime;
    private Board board;

    public Query(QueryType type, String fen, String move, int depth, long movetime) {
        this.type = type;
        this.fen = fen;
        this.move = move;
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

    public int getDepth() {
        return depth;
    }

    public long getMovetime() {
        return movetime;
    }

    public Board getBoard() { return board; }

    public static class Builder {
        private QueryType type;
        private String fen, move;
        private int depth = -1;
        private long movetime = -1;
        private Board board;

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

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setMovetime(long movetime) {
            this.movetime = movetime;
            return this;
        }

        public Builder setBoard(Board board) {
            this.board = board;
            return this;
        }

        public Query build() {
            if (type == null)
                throw new IllegalStateException("Query type can not be null.");

            if (fen == null && board == null)
                throw new IllegalStateException("Query is missing FEN or Board");

            return new Query(type, fen, move, depth, movetime);
        }
    }
}
