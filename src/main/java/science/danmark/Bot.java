package science.danmark;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    public static JDA builder;

    public static void main(String[] args) throws ClassNotFoundException {
        builder = JDABuilder.createLight("YOURTOKENHERE")
                .setActivity(Activity.watching("New messages..."))
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(new CommandHandler())
                .addEventListeners(new ButtonHandler())
                .addEventListeners(new ChatHandler())
                .build();

        SqlManager.connect();
        ServerManager.updateServerList();
        ChatHandler.getBannedWords();

        builder.upsertCommand("settings", "Global settings for the bot.").queue();
        builder.upsertCommand("announce", "announce something to all servers.").queue();
        builder.upsertCommand("ping", "check the ping of the bot.").queue();
        builder.upsertCommand("leaderboard", "show the leaderboard of the server.").queue();
        builder.upsertCommand("report", "report a message").addOption(OptionType.STRING, "messageid", "the ID of the message you want to report.").addOption(OptionType.STRING, "reason", "the reason for the report.").queue();
        builder.upsertCommand("admin", "admin commands").addOption(OptionType.STRING, "command", "the command you want to execute.").addOption(OptionType.USER, "user", "the user you want to execute the command on.").queue();
    }
}