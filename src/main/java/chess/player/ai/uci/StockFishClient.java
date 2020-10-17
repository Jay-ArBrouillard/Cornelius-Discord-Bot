package chess.player.ai.uci;

import chess.player.ai.uci.engine.Stockfish;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StockFishClient extends BaseAiClient {
    private Stockfish engine;

    public StockFishClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Stockfish(variant, filePath, options.toArray(new Option[options.size()]));
    }

    public String submit(Query query) throws IOException {
        String output;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query);
                break;
            case EVAL:
                output = engine.getEvaluation(query);
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

    public void close() throws IOException {
        engine.close();
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/stockfish_20090216_x64";

        public final Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final StockFishClient build() throws IOException {
            return new StockFishClient(variant, path, options);
        }
    }
}
