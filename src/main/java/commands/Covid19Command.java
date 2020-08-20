package commands;

import kong.unirest.*;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Covid19Command {

    public static void execute(MessageReceivedEvent event, String message) {
        Unirest.get("https://api.covid19api.com/summary").asJsonAsync(new Callback<JsonNode>() {
            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
                if (o == null || o.isEmpty()) return;
                try {
                    createGraph(event, o);
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendMessage("Uh oh! Something went wrong with the Covid19 API");
                }
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage("Sorry dude the Covid19 API must be busy").queue();
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage("Sorry dude some jerk interfered with the communication lines").queue();
            }
        });
    }

    public static void createGraph(MessageReceivedEvent event, JSONObject pData) throws MalformedURLException, UnsupportedEncodingException {
        //'{"chart": {"type": "bar", "data": {"labels": ["Hello", "World"], "datasets": [{"label": "Foo", "data": [1, 2]}]}}}'
        JSONObject global = pData.getJSONObject("Global");
        String newConfirmed = global.getString("NewConfirmed");
        String totalConfirmed = global.getString("TotalConfirmed");
        String newDeaths = global.getString("NewDeaths");
        String totalDeaths = global.getString("TotalDeaths");
        String newRecovered = global.getString("NewRecovered");
        String totalRecovered = global.getString("TotalRecovered");
        JSONObject USA = pData.getJSONArray("Countries").getJSONObject(235);
        String country = USA.getString("CountryCode");

        String baseurl = "https://quickchart.io/chart?width=500&height=300&backgroundColor=white&c=";
        StringBuilder query = new StringBuilder("{type:'bar',data:{labels:[\"newConfirmed\",\"totalConfirmed\",\"newDeaths\",\"totalDeaths\",\"newRecovered\",\"totalRecovered\"],datasets:[{label:'Global',data:[");
        query.append(newConfirmed).append(",");
        query.append(totalConfirmed).append(",");
        query.append(newDeaths).append(",");
        query.append(totalDeaths).append(",");
        query.append(newRecovered).append(",");
        query.append(totalRecovered).append("]},{label:'").append(country).append("',data:[");
        query.append(USA.getString("NewConfirmed")).append(",");
        query.append(USA.getString("TotalConfirmed")).append(",");
        query.append(USA.getString("NewDeaths")).append(",");
        query.append(USA.getString("TotalDeaths")).append(",");
        query.append(USA.getString("NewRecovered")).append(",");
        query.append(USA.getString("TotalRecovered")).append("]}]}}");
        System.out.println(query.toString());
        URL url = new URL(baseurl + URLEncoder.encode(query.toString(), "UTF-8"));

        System.out.println(url.toString());
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Global Covid19 Summary");
        eb.setImage(url.toString());
        eb.setFooter("A summary of new and total cases updated daily.");
        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage(eb.build()).queue();
    }

    public static class Help {
        static String name = "!covid";
        static String description = "returns a graph of the most recent stats on cornavirus";
        static String arguments = "<country> (optional)";
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
