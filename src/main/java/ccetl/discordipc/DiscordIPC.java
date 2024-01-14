package ccetl.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.management.ManagementFactory;

@SuppressWarnings("unused")
public class DiscordIPC {
    protected static final JsonParser JSON_PARSER = new JsonParser();

    private static final Gson GSON = new Gson();

    private static ErrorCallback errorCallback = new DefaultErrorCallback();

    private static Connection connection;
    private static Runnable onReady;

    private static boolean receivedDispatch;
    private static JsonObject queuedActivity;

    private static IPCUser user;

    /**
     * Sets the error callback
     */
    public static void setErrorCallback(ErrorCallback errorCallback) {
        DiscordIPC.errorCallback = errorCallback;
    }

    /**
     * Tries to open a connection to a locally running Discord instance
     *
     * @param appId   the application id to use
     * @param onReady callback called when a successful connection happens, from that point {@link #getUser()} will return non-null object up until {@link #stop()} is called or an error happens
     * @return true if a connection was opened successfully
     */
    public static boolean start(long appId, Runnable onReady) {
        // Open connection
        connection = Connection.open(DiscordIPC::onPacket);
        if (connection == null) {
            return false;
        }

        DiscordIPC.onReady = onReady;

        // Handshake
        JsonObject o = new JsonObject();
        o.addProperty("v", 1);
        o.addProperty("client_id", Long.toString(appId));
        connection.write(Opcode.Handshake, o);

        return true;
    }

    /**
     * @return true if it is currently connected to a local Discord instance
     */
    public static boolean isConnected() {
        return connection != null;
    }

    /**
     * @return the user that is logged in in the connected Discord instance
     */
    public static IPCUser getUser() {
        return user;
    }

    /**
     * Sets account's activity
     *
     * @param presence the rich presence to set the activity to
     */
    public static void setActivity(RichPresence presence) {
        if (connection == null) {
            return;
        }

        queuedActivity = presence.toJson();
        if (receivedDispatch) {
            sendActivity();
        }
    }

    /**
     * Closes the connection to the locally running Discord instance if it is open
     */
    public static void stop() {
        if (connection != null) {
            connection.close();

            connection = null;
            onReady = null;
            receivedDispatch = false;
            queuedActivity = null;
            user = null;
        }
    }

    private static void sendActivity() {
        JsonObject args = new JsonObject();
        args.addProperty("pid", getPID());
        args.add("activity", queuedActivity);

        JsonObject o = new JsonObject();
        o.addProperty("cmd", "SET_ACTIVITY");
        o.add("args", args);

        connection.write(Opcode.Frame, o);
        queuedActivity = null;
    }

    private static void onPacket(Packet packet) {
        // Close
        if (packet.getOpcode() == Opcode.Close) {
            errorCallback.error(packet.getData().get("code").getAsInt(), packet.getData().get("message").getAsString());
            stop();
        }
        // Frame
        else if (packet.getOpcode() == Opcode.Frame) {
            // Error
            if (packet.getData().has("evt") && packet.getData().get("evt").getAsString().equals("ERROR")) {
                JsonObject d = packet.getData().getAsJsonObject("data");
                errorCallback.error(d.get("code").getAsInt(), d.get("message").getAsString());
            }
            // Dispatch
            else if (packet.getData().has("cmd") && packet.getData().get("cmd").getAsString().equals("DISPATCH")) {
                receivedDispatch = true;
                user = GSON.fromJson(packet.getData().getAsJsonObject("data").getAsJsonObject("user"), IPCUser.class);

                if (onReady != null) {
                    onReady.run();
                }
                if (queuedActivity != null) {
                    sendActivity();
                }
            }
        }
    }

    private static int getPID() {
        String pr = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(pr.substring(0, pr.indexOf('@')));
    }

    protected static ErrorCallback getErrorCallback() {
        return errorCallback;
    }

    private static class DefaultErrorCallback implements ErrorCallback {
        @Override
        @SuppressWarnings("CallToPrintStackTrace")
        public void error(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void error(int code, String message) {
            System.err.println("Discord IPC error " + code + " with message: " + message);
        }
    }
}
