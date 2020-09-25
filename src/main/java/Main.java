import Utils.CommandHelper;
import Utils.MovieWatcher;
import commands.Holiday;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
//        System.out.println("MAKE SURE TO CHANGE TOKEN AND OWNER ID BEFORE PUSHING TO HEROKU OR GITHUB");

        String token = System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = System.getenv("OWNER_ID");
        String moviesApiKey = System.getenv("MOVIES_API_KEY");
        String calendarApiKey = System.getenv("HOLIDAY_API_KEY");
        String unsplashAccessKey = System.getenv("UNSPLASH_ACCESS_KEY");

        // start getting a bot account set up
        JDA jda = JDABuilder.createDefault(token)
                  .addEventListeners(new CommandHelper(ownerId))
                  .setStatus(OnlineStatus.ONLINE)
                  .setActivity(Activity.playing("!help or @Cornelius !help"))
                  .setAutoReconnect(true)
                  .build()
                  .awaitReady();
        jda.addEventListener(new MovieWatcher(moviesApiKey, jda));
        jda.addEventListener(new Holiday(calendarApiKey, unsplashAccessKey));
    }
}
