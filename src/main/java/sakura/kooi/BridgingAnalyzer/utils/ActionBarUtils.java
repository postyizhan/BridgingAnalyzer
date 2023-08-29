package sakura.kooi.BridgingAnalyzer.utils;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("SpellCheckingInspection")
public class ActionBarUtils {
    public static boolean works;
    public static String nmsVersion;
    private static Class<?> classCraftPlayer;
    private static Class<?> classPacketChat;
    private static Class<?> classChatSerializer;
    private static Class<?> classIChatComponent;
    private static Method methodSeralizeString;
    private static Class<?> classChatComponentText;
    private static Method methodGetHandle;
    private static Field fieldConnection;
    private static Method methodSendPacket;

    static {
        try {
            nmsVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            classPacketChat = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> classPacket = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            if (nmsVersion.equalsIgnoreCase("v1_8_R1") || nmsVersion.equalsIgnoreCase("v1_7_")) {
                classChatSerializer = Class.forName("net.minecraft.server." + nmsVersion + ".ChatSerializer");
                classIChatComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                methodSeralizeString = classChatSerializer.getDeclaredMethod("a", String.class);
            } else {
                classChatComponentText = Class.forName("net.minecraft.server." + nmsVersion + ".ChatComponentText");
                classIChatComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
            }
            methodGetHandle = classCraftPlayer.getDeclaredMethod("getHandle");
            Class<?> classEntityPlayer = Class.forName("net.minecraft.server." + nmsVersion + ".EntityPlayer");
            fieldConnection = classEntityPlayer.getDeclaredField("playerConnection");
            Class<?> classPlayerConnection = Class.forName("net.minecraft.server." + nmsVersion + ".PlayerConnection");
            methodSendPacket = classPlayerConnection.getDeclaredMethod("sendPacket", classPacket);
            works = true;
        } catch (Exception e) {
            works = false;
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void sendActionBar(Player player, String message) {
        if (!works) return;
        try {
            Object p = classCraftPlayer.cast(player);
            Object ppoc;
            if (nmsVersion.equalsIgnoreCase("v1_8_R1") || nmsVersion.equalsIgnoreCase("v1_7_")) {
                Object cbc = classIChatComponent.cast(methodSeralizeString.invoke(classChatSerializer, "{\"text\": \"" + message + "\"}"));
                ppoc = classPacketChat.getConstructor(new Class<?>[]{classIChatComponent, byte.class}).newInstance(cbc, (byte) 2);
            } else {
                Object o = classChatComponentText.getConstructor(new Class<?>[]{String.class}).newInstance(message);
                ppoc = classPacketChat.getConstructor(new Class<?>[]{classIChatComponent, byte.class}).newInstance(o, (byte) 2);
            }
            Object h = methodGetHandle.invoke(p);
            Object pc = fieldConnection.get(h);
            methodSendPacket.invoke(pc, ppoc);
        } catch (Exception ex) {
            ex.printStackTrace();
            works = false;
        }
    }

}