package commands;

import kong.unirest.*;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class MemeCommand  {

    public static void execute(MessageReceivedEvent event) {
        // use Unirest to poll an API
        Unirest.get("https://meme-api.herokuapp.com/gimme").asJsonAsync(new Callback<JsonNode>() {

            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle(o.getString("title"), o.getString("postLink"))
                        .setFooter(o.getString("subreddit"))
                        .setImage(o.getString("url"))
                        .build()).queue();
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                event.getChannel().sendMessage("Sorry meme api is down").queue();
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                event.getChannel().sendMessage("Sorry meme api is down").queue();
            }
        });
    }

    public static class Help {
        static String name = "!meme";
        static String description = "shows a random meme";
        static String arguments = "";
        static boolean guildOnly = false;

        public static String getName() {
            return name;
        }

        public static String getDescription() {
            return description;
        }

        public static String getArguments() {
            return arguments;
        }

        public boolean isGuildOnly() {
            return guildOnly;
        }
    }
}
