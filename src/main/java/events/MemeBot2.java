package events;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import kong.unirest.*;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;

public class MemeBot2 extends Command {
    public MemeBot2()
    {
        this.name = "meme";
        this.help = "shows a random meme";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {


        // use Unirest to poll an API
        Unirest.get("https://meme-api.herokuapp.com/gimme").asJsonAsync(new Callback<JsonNode>() {

            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
                event.reply(new EmbedBuilder()
                        .setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.GREEN)
                        .setTitle(o.getString("title"), o.getString("postLink"))
                        .setFooter(o.getString("subreddit"))
                        .setImage(o.getString("url"))
                        .build());
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                event.reactError();
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                event.reactError();
            }
        });
    }
}
