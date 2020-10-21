package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.CT800;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CT800Client extends BaseAiClient{
    private CT800 engine;

    public CT800Client(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new CT800(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "CT800Client";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/CT800_V1.34_x64_linux";

        public final CT800Client.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final CT800Client.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final CT800Client build() throws IOException {
            return new CT800Client(variant, path, options);
        }
    }
}
