package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Cheese;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CheeseClient extends BaseAiClient{
    private Cheese engine;

    public CheeseClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Cheese(variant, filePath, options.toArray(new Option[options.size()]));
    }

    public String submit(Query query) throws IOException {
        String output = null;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query);
                break;
            default:
                break;
        }

        return output;
    }

    public void close() throws IOException {
        engine.close();
    }

    @Override
    public String toString() {
        return "CheeseClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/cheese-21-linux-64";

        public final CheeseClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final CheeseClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final CheeseClient build() throws IOException {
            return new CheeseClient(variant, path, options);
        }
    }
}
