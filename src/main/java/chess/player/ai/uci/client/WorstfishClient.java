package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Worstfish;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Tries to find the worst move using stockfish
 */
public class WorstfishClient extends BaseAiClient{
    private Worstfish engine;

    public WorstfishClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Worstfish(variant, filePath, options.toArray(new Option[options.size()]));
    }

    public String submit(Query query) throws IOException {
        String output;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query);
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

    @Override
    public String toString() {
        return "WorstfishClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/stockfish_20090216_x64_modern";

        public final WorstfishClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final WorstfishClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final WorstfishClient build() throws IOException {
            return new WorstfishClient(variant, path, options);
        }
    }
}