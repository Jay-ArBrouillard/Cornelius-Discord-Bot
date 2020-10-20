package chess.player.ai.uci;

import chess.player.ai.uci.engine.Wyld;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WyldClient extends BaseAiClient{
    private Wyld engine;

    public WyldClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Wyld(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "WyldClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/wyld_1_51_linux";

        public final WyldClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final WyldClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final WyldClient build() throws IOException {
            return new WyldClient(variant, path, options);
        }
    }
}
