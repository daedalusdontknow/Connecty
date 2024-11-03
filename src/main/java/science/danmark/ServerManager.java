package science.danmark;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    static List<String[]> servers = new ArrayList<>();
    static List<String[]> userChannels = new ArrayList<>();

    public static void updateServerList() {
        servers.clear();
        try {
            java.sql.ResultSet rs = SqlManager.getStatement("SELECT * FROM servers");
            while (rs.next()) {
                servers.add(new String[]{rs.getString("GuildID"), rs.getString("ChannelID")});
            }

            rs = SqlManager.getStatement("SELECT * FROM usersChannels");
            while (rs.next()) {
                userChannels.add(new String[]{rs.getString("UserID")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addServer(String guildID, String channelID) {
        try {
            SqlManager.executeStatement("INSERT INTO servers (GuildID, ChannelID) VALUES ('" + guildID + "', '" + channelID + "')");
            servers.add(new String[]{guildID, channelID});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteServer(String guildID) {
        try {
            SqlManager.executeStatement("DELETE FROM servers WHERE GuildID = '" + guildID + "'");
            for (int i = 0; i < servers.size(); i++) {
                if (servers.get(i)[0].equals(guildID)) {
                    servers.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
