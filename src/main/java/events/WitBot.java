package events;

import kong.unirest.*;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class WitBot extends ListenerAdapter {
    public WitBot() {

    }

    public void onMessageReceived (MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getMessage().getContentRaw().equals("!wit")) return;

        Unirest.config().addDefaultHeader("Authorization", "Bearer MGI2MIZXINQZXIFIJL3JHNTY5KSJUAVB");
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
