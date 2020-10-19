package chess.player.ai.uci;

import chess.player.ai.uci.engine.SmarThink;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SmarThinkClient extends BaseAiClient{
    private SmarThink engine;

    public SmarThinkClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new SmarThink(variant, filePath, options.toArray(new Option[options.size()]));
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
        private String path = "bin/SmarThink_v198_nix";

        public final SmarThinkClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final SmarThinkClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final SmarThinkClient build() throws IOException {
            return new SmarThinkClient(variant, path, options);
        }
    }
}
