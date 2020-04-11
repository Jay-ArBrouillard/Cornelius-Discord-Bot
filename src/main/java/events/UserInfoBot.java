package events;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoBot extends Command {
    public UserInfoBot()
    {
        this.name = "userinfo";
        this.help = "Displays information about a user";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }


    @Override
    protected void execute(CommandEvent event) {
        String msg = event.getMessage().getContentRaw();
        System.out.println(msg);
        if (msg == null) {
            event.getChannel().sendMessage("Missing arguments, check `!userinfo username/@user/userId`").queue();
            return;
        }

        String joined = String.join("", msg);
        List<User> foundUsers = FinderUtil.findUsers(joined, event.getJDA());

        if (foundUsers.isEmpty()) {

            List<Member> foundMembers = FinderUtil.findMembers(joined, event.getGuild());

            if (foundMembers.isEmpty()) {
                event.getChannel().sendMessage("No users found for `" + joined + "`").queue();
                return;
            }

            foundUsers = foundMembers.stream().map(Member::getUser).collect(Collectors.toList());

        }

        User user = foundUsers.get(0);
        Member member = event.getGuild().getMember(user);

        MessageEmbed embed = new EmbedBuilder()
                .setColor(member.getColor())
                .setThumbnail(user.getEffectiveAvatarUrl().replaceFirst("gif", "png"))
                .addField("Username#Discriminator", String.format("%#s", user), false)
                .addField("Display Name", member.getEffectiveName(), false)
                .addField("User Id + Mention", String.format("%s (%s)", user.getId(), member.getAsMention()), false)
                .addField("Account Created", user.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Guild Joined", member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Online Status", member.getOnlineStatus().name().toLowerCase().replaceAll("_", " "), false)
                .addField("Bot Account", user.isBot() ? "Yes" : "No", false)
                .build();

        event.getChannel().sendMessage(embed).queue();
    }
}
