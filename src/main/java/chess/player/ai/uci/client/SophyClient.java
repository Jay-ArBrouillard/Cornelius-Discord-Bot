package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Sophy;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SophyClient extends BaseAiClient{
    private Sophy engine;

    public SophyClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Sophy(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "SophyClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/sophy-v0.1-linux";

        public final SophyClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final SophyClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final SophyClient build() throws IOException {
            return new SophyClient(variant, path, options);
        }
    }
}
