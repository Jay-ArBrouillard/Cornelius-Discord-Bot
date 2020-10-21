package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Eschesc;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class EschescClient extends BaseAiClient{
    private Eschesc engine;

    public EschescClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Eschesc(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "EschescClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/eschecs_5.0.6_linux";

        public final EschescClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final EschescClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final EschescClient build() throws IOException {
            return new EschescClient(variant, path, options);
        }
    }
}
