package commands;

import kong.unirest.*;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Holiday extends ListenerAdapter {

    private String holidayApiKey;
    private String imageApiKey;
    private Timer timer = new Timer();
    private Calendar today;

    public Holiday(String moviesApiKey, String imageApiKey) {
        this.holidayApiKey = moviesApiKey;
        this.imageApiKey = imageApiKey;
        Date date = new Date();
        today = Calendar.getInstance();
        today.setTime(date);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        if (message.contains("!holiday loop")) {
            timer = new Timer();
            startTimer(event, null);
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage("Ok! I'll send out a daily holiday alert!").queue();
        }
        else if (message.contains("!holiday stop")) {
            timer.cancel();
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage("Ok! The holidays alerts are turned off...").queue();
        }
        else if (message.contains("!holiday")) {
            String [] args = message.substring(message.indexOf("!holiday")+8).trim().split(" ");
            callApi(event.getTextChannel(), args);
        }
    }

    public void callApi(TextChannel ch, String[] args) {
        StringBuilder url = new StringBuilder("https://calendarific.com/api/v2/holidays?api_key=").append(holidayApiKey);
        if (args != null) {
            for (String s : args) {
                String value = "";
                s = s.trim();
                boolean valid = true;
                if (s == null || s.isEmpty()) continue;
                if (s.contains("m")) {
                    value = s.substring(s.indexOf("m")+1);
                    valid = validate("m", value, ch);
                    url.append("&month=");
                }
                else if (s.contains("d")) {
                    value = s.substring(s.indexOf("d")+1);
                    valid = validate("d", value, ch);
                    url.append("&day=");
                }
                else if (s.contains("y")) {
                    value = s.substring(s.indexOf("y")+1);
                    valid = validate("y", value, ch);
                    url.append("&year=");
                }
                else if (s.contains("l")) {
                    value = s.substring(s.indexOf("l")+1);
                    url.append("&location=");
                }
                else if (s.contains("t")) {
                    value = s.substring(s.indexOf("t")+1);
                    valid = validate("t", value, ch);
                    url.append("&type=");
                }
                else if (s.contains("c")) {
                    value = s.substring(s.indexOf("c")+1);
                    url.append("&country=");
                }
                if (!valid) return;
                url.append(value);
            }

            //Check if the args is empty or if all the args are null or empty
            if (args.length == 0 || Stream.of(args).allMatch(string -> string == null || string.isEmpty())) {
                url.append("&month=")
                        .append(today.get(Calendar.MONTH)+1)
                        .append("&day=")
                        .append(today.get(Calendar.DAY_OF_MONTH))
                        .append("&year=")
                        .append(today.get(Calendar.YEAR));
            }
        }
        else {
            url.append("&month=")
                    .append(today.get(Calendar.MONTH)+1)
                    .append("&day=")
                    .append(today.get(Calendar.DAY_OF_MONTH))
                    .append("&year=")
                    .append(today.get(Calendar.YEAR));
        }

        Unirest.get(url.toString()).asJsonAsync(new Callback<JsonNode>() {
            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
                if (o == null || o.isEmpty()) return;
                boolean valid = handleHTTPResponse(o.getJSONObject("meta"), ch);
                if (!valid) return;

                JSONArray holidays = o.getJSONObject("response").getJSONArray("holidays");
                if (holidays.length() == 0) {
                    ch.sendTyping().queue();
                    ch.sendMessage("Oop. There are no holidays for that date and settings.").queue();
                }
                for (int i = 0; i < holidays.length(); i++) {
                    JSONObject holiday = (JSONObject) holidays.get(i);
                    getImage(ch, holiday);
                }
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                ch.sendTyping().queue();
                ch.sendMessage("Sorry dude the Holiday API must be busy").queue();
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                ch.sendTyping().queue();
                ch.sendMessage("Sorry dude some jerk interfered with the communication lines").queue();
            }
        });
    }

    private boolean validate(String parameter, String value, TextChannel ch) {
        switch (parameter) {
            case "m":
                int month = 0;
                try {
                    month = Integer.parseInt(value);
                    if (month < 1 || month > 12) {
                        ch.sendTyping().queue();
                        ch.sendMessage("Month must be a numeric value between [1-12].");
                        return false;
                    }
                }
                catch (Exception e) {
                    ch.sendTyping().queue();
                    ch.sendMessage("Month must be a numeric value between [1-12].");
                    return false;
                }
                break;
            case "d":
                int day = 0;
                try {
                    day = Integer.parseInt(value);
                    if (day < 1 || day > 31) {
                        ch.sendTyping().queue();
                        ch.sendMessage("Day must be a numeric value between [1-31].");
                        return false;
                    }
                }
                catch (Exception e) {
                    ch.sendTyping().queue();
                    ch.sendMessage("Day must be a numeric value between [1-31].");
                    return false;
                }
                break;
            case "y":
                int year = 0;
                try {
                    year = Integer.parseInt(value);
                    if (year < 0 || year > 2049) {
                        ch.sendTyping().queue();
                        ch.sendMessage("Year must be a numeric value between [0-2049].");
                        return false;
                    }
                }
                catch (Exception e) {
                    ch.sendTyping().queue();
                    ch.sendMessage("Year must be a numeric value between [0-2049].");
                    return false;
                }
                break;
            case "t":
                String type = value.toLowerCase();
                if (!type.equals("national") && !type.equals("local") && !type.equals("religious") && !type.equals("observance")) {
                    ch.sendTyping().queue();
                    ch.sendMessage("Type must be one of [national, local, religious, or observance].");
                    return false;
                }
                return true;
        }
        return true;
    }

    public void getImage (TextChannel ch, JSONObject holiday) {
        Unirest.get("https://api.unsplash.com/search/photos/?client_id="+imageApiKey+"&page=1&query="+holiday.getString("name")).asJsonAsync(new Callback<JsonNode>() {
            // The API call was successful
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject o = hr.getBody().getObject();
                JSONArray results = o.getJSONArray("results");
                int rand = getRandomNumber(0, results.length()/2); //Get a random image in the first half of results
                JSONObject urls = ((JSONObject) results.get(rand)).getJSONObject("urls");

                String name = holiday.getString("name");
                String date = holiday.getJSONObject("date").getString("iso");
                String description = holiday.getString("description");
                String url = urls.getString("raw");
                String locations = holiday.getString("locations");
                String typeString = "";
                if (holiday.getJSONArray("type") != null) {
                    for (Object type : holiday.getJSONArray("type")) {
                        typeString += type.toString() + ", ";
                    }
                }
                String statesString = "";
                try { //Using try catch here because we cant check if for json primitive or json array
                    JSONArray states = holiday.getJSONArray("states");
                    for (Object state : states) {
                        statesString += ((JSONObject)state).getString("name") + ", ";
                    }
                } catch (Throwable e) {
                    statesString = holiday.getString("states");
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Happy " + name + "! " + date);
                if (url != null) eb.setImage(url);
                eb.setDescription(description);
                eb.addField("Celebrated as a: ", typeString, false);
                eb.addField("Locations: ", locations, false);
                eb.addField("States Recognized: ", statesString, false);
                eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
                ch.sendMessage(eb.build()).queue();
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                sendWithoutImage(holiday, ch);
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                sendWithoutImage(holiday, ch);
            }
        });
    }

    public void startTimer(MessageReceivedEvent event, String [] args) {
        TextChannel ch = event.getTextChannel();
        timer.schedule(new TimerTask() {
            public void run() {
                StringBuilder url = new StringBuilder("https://calendarific.com/api/v2/holidays?api_key=").append(holidayApiKey).append("&country=US");
                url.append("&month=").append(today.get(Calendar.MONTH)+1).append("&day=");
                url.append(today.get(Calendar.DAY_OF_MONTH)).append("&year=").append(today.get(Calendar.YEAR));

                Unirest.get(url.toString()).asJsonAsync(new Callback<JsonNode>() {
                    // The API call was successful
                    @Override
                    public void completed(HttpResponse<JsonNode> hr) {
                        JSONObject o = hr.getBody().getObject();
                        if (o == null || o.isEmpty()) return;
                        boolean valid = handleHTTPResponse(o.getJSONObject("meta"), ch);
                        if (!valid) return;

                        JSONArray holidays = o.getJSONObject("response").getJSONArray("holidays");
                        if (holidays.length() == 0) {
                            ch.sendTyping().queue();
                            ch.sendMessage("Oop. No celebratin' today, there are no holidays today :(.").queue();
                        }
                        for (int i = 0; i < holidays.length(); i++) {
                            JSONObject holiday = (JSONObject) holidays.get(i);
                            getImage(ch, holiday);
                        }
                    }

                    // The API call failed
                    @Override
                    public void failed(UnirestException ue) {
                        ch.sendTyping().queue();
                        ch.sendMessage("Sorry dude the Holiday API must be busy").queue();
                    }

                    // The API call was cancelled (this should never happen)
                    @Override
                    public void cancelled() {
                        ch.sendTyping().queue();
                        ch.sendMessage("Sorry dude some jerk interfered with the communication lines").queue();
                    }
                });
            }
        }, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); //period: 1 day
    }

    public void sendWithoutImage (JSONObject holiday, TextChannel ch) {
        String name = holiday.getString("name");
        String date = holiday.getJSONObject("date").getString("iso");
        String description = holiday.getString("description");
        String locations = holiday.getString("locations");
        String typeString = "";
        if (holiday.getJSONArray("type") != null) {
            for (Object type : holiday.getJSONArray("type")) {
                typeString += type.toString() + ", ";
            }
        }
        String statesString = "";
        try { //Using try catch here because we cant check if for json primitive or json array
            JSONArray states = holiday.getJSONArray("states");
            for (Object state : states) {
                statesString += ((JSONObject)state).getString("name") + ", ";
            }
        } catch (Throwable e) {
            statesString = holiday.getString("states");
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Happy " + name + "! " + date);
        eb.setDescription(description);
        eb.addField("Celebrated as a: ", typeString, false);
        eb.addField("Locations: ", locations, false);
        eb.addField("States Recognized: ", statesString, false);
        eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
        ch.sendMessage(eb.build()).queue();
    }

    public boolean handleHTTPResponse(JSONObject metadata, TextChannel ch) {
        if (metadata.getInt("code") == 200) {
            return true;
        }
        else {
            String errorType = metadata.getString("error_type");
            String error_detail = metadata.getString("error_detail");

            if (errorType != null || !errorType.isEmpty()) {
                ch.sendMessage("Holiday Command Issue: " + errorType).queue();
            }
            if (error_detail != null || !error_detail.isEmpty()) {
                ch.sendMessage(error_detail).queue();
            }
            return false;
        }
    }

    public int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    public static class Help {
        static String name = "!holiday";
        static String description = "Get holiday information about any country at any time. See here for a list of supported countries: https://calendarific.com/supported-countries";
        static String arguments = "m<month> d<day> y<year> l<location> t<type> c<country> (In any order)";
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
