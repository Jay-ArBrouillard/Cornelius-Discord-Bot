package events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.regex.Pattern;

public class NumberGeneratorBot extends ListenerAdapter {

    private static final String CHANNEL = "commands";

    public void onGuildMessageReceived (GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String channel = event.getChannel().getName();
        if (!channel.equals(CHANNEL)) return;
        Pattern p1 = Pattern.compile("!generate\\(\\)");
        Pattern p2 = Pattern.compile("!generate\\(\\d*-\\d*\\)");
        String[] responses = new String[] {
                "Here's your number ",
                "Here you go ",
                "Am I just a calculator to you? ",
                "Easy peasy. ",
                "You coulda just used Random.org "
        };

        String message = event.getMessage().getContentRaw();
        if (p1.matcher(message).matches()) {
            int rand = getRandomNumber();
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage(responses[getRandomNumber(0, responses.length-1)] + rand).queue();
        } else if (p2.matcher(message).matches()) {
            int first = Integer.parseInt(message.substring(message.indexOf("(")+1, message.indexOf("-")));
            int second = Integer.parseInt(message.substring(message.indexOf("-")+1, message.length()-1));

            event.getChannel().sendTyping().queue();

            if (first > second) {
                event.getChannel().sendMessage("You dumbass, that doesn't make any sense").queue();
            } else {
                int rand = getRandomNumber(first, second);
                event.getChannel().sendMessage(responses[getRandomNumber(0, responses.length-1)] + rand).queue();
            }
        }

    }

    public int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }
}
