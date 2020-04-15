package commands;

import kong.unirest.*;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WitCommand extends ListenerAdapter {
    private final String token;
    public WitCommand(String token) {
        this.token = token;
    }

    public void onMessageReceived (MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getMessage().getContentRaw().equals("!wit")) return;

        //todo fix
        Unirest.get("https://api.wit.ai/message?v=20200411&q="+event.getMessage().getContentRaw().substring(1)).asJsonAsync(new Callback<JsonNode>() {

            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                System.out.println("failed");
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                System.out.println("cancelled");
            }
        });

    }
}
