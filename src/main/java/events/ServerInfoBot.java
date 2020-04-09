package events;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;

public class ServerInfoBot extends Command {
    public ServerInfoBot()
    {
        this.name = "serverInfo";
        this.aliases = new String[]{"server", "serverinfo"};
        this.help = "shows a information about the server such as members and when they joined";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!event.getChannelType().isGuild()) {
            event.reactError();
            event.getChannel().sendMessage("`"+event.getMessage().getContentRaw()+"`" + " won't work in a direct messsage").queue();
            return;
        }
        String[] members = new String[event.getGuild().getMembers().size()];
        for (int i = 0; i < event.getGuild().getMembers().size(); i++) {
            members[i] = event.getGuild().getMembers().get(i).getEffectiveName() + "- joined: " +
                         event.getGuild().getMembers().get(i).getTimeJoined().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setAuthor(event.getGuild().getName());
        eb.setThumbnail("https://static.thenounproject.com/png/9347475-200.png");
        eb.addField("Server Owner: ", event.getGuild().getName(), true);
        eb.addField("Member Count:", Integer.toString(members.length), true);
        eb.setDescription("**Members:** \n" + Arrays.toString(members) + "\n **Invite Link** \n" + "https://discord.gg/ZwVbJA");

        event.getChannel().sendMessage(eb.build()).queue();
    }
}
