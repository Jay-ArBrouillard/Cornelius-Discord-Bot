import Utils.CommandHelper;
import Utils.MovieWatcher;
import chess.pgn.FenUtils;
import chess.player.ai.stockfish.StockFishClient;
import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Query;
import chess.player.ai.stockfish.engine.enums.QueryType;
import chess.player.ai.stockfish.engine.enums.Variant;
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

    public static void main(String[] args) throws LoginException, InterruptedException {//
        String token = System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId =System.getenv("OWNER_ID");
        String moviesApiKey = System.getenv("MOVIES_API_KEY");
        String calendarApiKey = System.getenv("HOLIDAY_API_KEY");
        String unsplashAccessKey = System.getenv("UNSPLASH_ACCESS_KEY");

//        StockFishClient stockFishClient = null;
//        try {
//             stockFishClient = new StockFishClient.Builder()
//                    .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
//                    .setOption(Option.Skill_Level, 20) // Stockfish skill level 0-20
//                    .setVariant(Variant.BMI2) // Stockfish Variant
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String bestMoveString = stockFishClient.submit(new Query.Builder(QueryType.Best_Move)
//                .setMovetime(1000)
//                .setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
//                .build());
//
//        System.out.println(bestMoveString);

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
