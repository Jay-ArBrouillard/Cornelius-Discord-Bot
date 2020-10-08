package chess.player.ai.stockfish;

import chess.player.ai.stockfish.engine.Stockfish;
import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Query;
import chess.player.ai.stockfish.engine.enums.Variant;
import chess.player.ai.stockfish.exception.StockfishInitException;

import java.util.HashSet;
import java.util.Set;

public class StockFishClient {
    private Stockfish engine;

    public StockFishClient(Variant variant, Set<Option> options) throws StockfishInitException {
        engine = new Stockfish(variant, options.toArray(new Option[options.size()]));
    }

    public String submit(Query query) {
        String output;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query);
                break;
            case Make_Move:
                output = engine.makeMove(query);
                break;
            case Legal_Moves:
                output = engine.getLegalMoves(query);
                break;
            default:
                output = null;
                break;
        }

        return output;
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;

        public final Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final StockFishClient build() throws StockfishInitException {
            return new StockFishClient(variant, options);
        }
    }
}
