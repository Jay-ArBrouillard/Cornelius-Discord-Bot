package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Amoeba;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FridolinClient extends BaseAiClient{
    private Amoeba engine;

    public FridolinClient(Variant variant, String filePath, Set<Option> options) throws IOException {
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
        return "FridolinClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/Fridolin300_linux";

        public final FridolinClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final FridolinClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final FridolinClient build() throws IOException {
            return new FridolinClient(variant, path, options);
        }
    }
}
