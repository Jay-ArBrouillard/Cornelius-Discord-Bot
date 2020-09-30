import Utils.CommandHelper;
import Utils.MovieWatcher;
import commands.Holiday;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException {
        System.out.println("MAKE SURE TO CHANGE TOKEN AND OWNER ID BEFORE PUSHING TO HEROKU OR GITHUB");

        String token = "NjkzMjgyMDk5MTY3NDk0MjI1.Xn6zRQ.Of2gJBsvhR3pzo7cpMHjNLTPGEY";//System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = "693282099167494225";//System.getenv("OWNER_ID");
        String moviesApiKey = "x";//System.getenv("MOVIES_API_KEY");
        String calendarApiKey = "X";//System.getenv("HOLIDAY_API_KEY");
        String unsplashAccessKey = "X";//System.getenv("UNSPLASH_ACCESS_KEY");

        // start getting a bot account set up
        JDA jda = JDABuilder.createDefault(token)
                  .setStatus(OnlineStatus.ONLINE)
                  .setActivity(Activity.playing("!help or @Cornelius !help"))
                  .setChunkingFilter(ChunkingFilter.ALL)
                  .setMemberCachePolicy(MemberCachePolicy.ALL)
                  .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_MESSAGES)
                  .setAutoReconnect(true)
                  .build()
                  .awaitReady();
        jda.addEventListener(new CommandHelper(ownerId));
        jda.addEventListener(new MovieWatcher(moviesApiKey, jda));
        jda.addEventListener(new Holiday(calendarApiKey, unsplashAccessKey));
    }
}
