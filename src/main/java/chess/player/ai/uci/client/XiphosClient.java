package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Xiphos;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class XiphosClient extends BaseAiClient {
    private Xiphos engine;

    public XiphosClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Xiphos(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "XiphosClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/xiphos_0.6_linux";

        public final XiphosClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final XiphosClient.Builder setOption(Option o, long value) {
            options.add(o.setValue(value));
            return this;
        }

        public final XiphosClient build() throws IOException {
            return new XiphosClient(variant, path, options);
        }
    }
}
