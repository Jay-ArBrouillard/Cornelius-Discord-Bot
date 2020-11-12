package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Fizbo;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FizboClient extends BaseAiClient{
    private Fizbo engine;

    public FizboClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Fizbo(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "FizboClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/fizbo-2-linux";

        public final FizboClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final FizboClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final FizboClient build() throws IOException {
            return new FizboClient(variant, path, options);
        }
    }
}
