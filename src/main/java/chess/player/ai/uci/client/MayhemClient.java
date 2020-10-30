package chess.player.ai.uci.client;

import chess.player.ai.uci.engine.Mayhem;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MayhemClient extends BaseAiClient{
    private Mayhem engine;

    public MayhemClient(Variant variant, String filePath, Set<Option> options) throws IOException {
        engine = new Mayhem(variant, filePath, options.toArray(new Option[options.size()]));
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
        return "MayhemClient";
    }

    public static class Builder {
        private Set<Option> options = new HashSet<>();
        private Variant variant = Variant.DEFAULT;
        private String path = "bin/mayhem-0.48-x86-unix-modern-64bit";

        public final MayhemClient.Builder setVariant(Variant v) {
            variant = v;
            return this;
        }

        public final MayhemClient.Builder setOption(Option o, Object value) {
            options.add(o.setValue(value));
            return this;
        }

        public final MayhemClient build() throws IOException {
            return new MayhemClient(variant, path, options);
        }
    }
}
