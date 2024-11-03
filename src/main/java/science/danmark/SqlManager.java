package science.danmark;

import java.sql.DriverManager;
import java.sql.SQLException;
public class SqlManager {

    public static java.sql.Connection connection;
    public static java.sql.Statement statement;

    public static void connect() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:GlobalData.sqlite");
            statement = connection.createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS `servers` (GuildID VARCHAR(255), ChannelID VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS `usersChannels` (UserID VARCHAR(255), ChannelID VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS `users` (UserID VARCHAR(255), RoleID VARCHAR(255), State INT DEFAULT 1)");
            statement.execute("CREATE TABLE IF NOT EXISTS `messages` (UserID VARCHAR(255), MessageID VARCHAR(255), GuildID VARCHAR(255), ChannelID VARCHAR(255), Message VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS `reports` (UserID VARCHAR(255), MessageID VARCHAR(255), GuildID VARCHAR(255), Reason VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS `bannedWords` (Word VARCHAR(255))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeStatement(String statement){
        try {
            SqlManager.statement.execute(statement);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public static java.sql.ResultSet getStatement(String statement){
        try {
            return SqlManager.statement.executeQuery(statement);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
