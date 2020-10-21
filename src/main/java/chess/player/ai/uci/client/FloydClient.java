package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Floyd;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FloydClient extends BaseAiClient{
    private Floyd engine;

    public FloydClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Floyd(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "FloydClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/floyd0.9.linux.x86-64";

        public final FloydClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final FloydClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final FloydClient build() throws IOException {
            return new FloydClient(variant, path, options);
        }
    }
}
