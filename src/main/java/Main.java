import Chess.Board;
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
        // config.txt contains two lines
        // the first is the bot token

        String token = "NjkzMjgyMDk5MTY3NDk0MjI1.XooYBw.uQtlw6XOB98G9aqmAfPsssgFSvA";//System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = "693282099167494225";//System.getenv("OWNER_ID");
        String moviesApiKey = "c67c13c5f724cedd093c9d4a28c1e094";//System.getenv("MOVIES_API_KEY");
        String witServerAccessToken = "MGI2MIZXINQZXIFIJL3JHNTY5KSJUAVB";//System.getenv("WIT_AI_TOKEN");
        String calendarApiKey = "bffaa8a8f90dc8a12b4b816f02befc90c0911e35";//System.getenv("HOLIDAY_API_KEY");
        String unsplashAccessKey = "YESomCUGS2m__9dzigU0AdDRMcrWTV-FlUzDkUxJI3c";//System.getenv("UNSPLASH_ACCESS_KEY");

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
