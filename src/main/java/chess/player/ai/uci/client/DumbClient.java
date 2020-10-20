package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Amoeba;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DumbClient extends BaseAiClient{
    private Amoeba engine;

    public DumbClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Amoeba(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "DumbClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/dumb-1.4-linux";

        public final DumbClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final DumbClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final DumbClient build() throws IOException {
            return new DumbClient(variant, path, options);
        }
    }
}
