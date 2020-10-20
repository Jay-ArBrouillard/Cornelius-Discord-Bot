package chess.player.ai.uci;

import chess.player.ai.uci.engine.Chenglite;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ChengliteClient extends BaseAiClient{
    private Chenglite engine;

    public ChengliteClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Chenglite(variant, filePath, options.toArray(new Option[options.size()]));
    }

    public String submit(Query query) throws IOException {
        String output;

        switch (query.getType()) {
            case Best_Move:
                output = engine.getBestMove(query).trim();
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
        return "ChengliteClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/Chenglite_v1.0_x64_linux";

        public final ChengliteClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final ChengliteClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final ChengliteClient build() throws IOException {
            return new ChengliteClient(variant, path, options);
        }
    }
}
