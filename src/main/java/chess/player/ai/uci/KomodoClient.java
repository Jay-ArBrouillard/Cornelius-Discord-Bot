package chess.player.ai.uci;

import chess.player.ai.uci.engine.Komodo;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class KomodoClient extends BaseAiClient{
    private Komodo engine;

    public KomodoClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Komodo(variant, filePath, options.toArray(new Option[options.size()]));
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

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/komodo_11.01_linux";

        public final KomodoClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final KomodoClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final KomodoClient build() throws IOException {
            return new KomodoClient(variant, path, options);
        }
    }
}
