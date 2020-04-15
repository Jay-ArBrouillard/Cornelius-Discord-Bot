package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoCommand {

    public static void execute(MessageReceivedEvent event, String message) {
        if (message == null) {
            event.getChannel().sendMessage("Missing arguments, check `!userinfo username/@user/userId`").queue();
            return;
        }

        String joined = String.join("", message);
        List<User> foundUsers = event.getJDA().getUsers();

        if (foundUsers.isEmpty()) {

            List<Member> foundMembers = event.getGuild().getMembers();

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

    public static class Help {
        static String name = "!user";
        static String description = "returns information about the specified user";
        static String arguments = "<@User> or <username> or <userId>";
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
