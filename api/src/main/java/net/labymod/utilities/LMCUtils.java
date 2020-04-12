package net.labymod.utilities;

import com.comphenix.tinyprotocol.Reflection;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.bukkit.entity.Player;

import java.nio.charset.Charset;

public class LMCUtils {

    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getCraftBukkitClass("CraftPlayer");
    private static final Reflection.MethodInvoker GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS, "getHandle");
    private static final Class<?> PLAYER_CONNECTION_CLASS = Reflection.getMinecraftClass("PlayerConnection");
    private static final Reflection.FieldAccessor PLAYER_CONNECTION_FIELD = Reflection.getField(Reflection.getMinecraftClass("EntityPlayer"),
            "playerConnection", PLAYER_CONNECTION_CLASS);
    private static final Reflection.MethodInvoker SEND_PACKET_METHOD = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
            "sendPacket", Reflection.getMinecraftClass("Packet"));
    private static final Class<?> PACKET_DATA_SERIALIZER_CLASS = Reflection.getMinecraftClass("PacketDataSerializer");
    private static final Reflection.ConstructorInvoker PACKET_DATA_SERIALIZER_CONSTRUCTOR = Reflection.getConstructor(
            PACKET_DATA_SERIALIZER_CLASS, ByteBuf.class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_CUSTOM_PAYLOAD_CONSTRUCTOR = Reflection.getConstructor(
            Reflection.getMinecraftClass("PacketPlayOutCustomPayload"), String.class, PACKET_DATA_SERIALIZER_CLASS);

    /**
     * Send a LMC message to the minecraft client
     *
     * @param player         Minecraft Client
     * @param key            LMC message key
     * @param messageContent json object content
     */
    public static void sendLMCMessage(Player player, String key, JsonObject messageContent) {
        byte[] bytes = LMCUtils.getBytesToSend(key, messageContent.toString());

        // 12/4/20, JMB: Converted into reflections for multi-version support:
//        PacketDataSerializer pds = new PacketDataSerializer(Unpooled.wrappedBuffer(bytes));
//        PacketPlayOutCustomPayload payloadPacket = new PacketPlayOutCustomPayload("LMC", pds);
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(payloadPacket);
        Object pds = PACKET_DATA_SERIALIZER_CONSTRUCTOR.invoke(Unpooled.wrappedBuffer(bytes));
        Object payloadPacket = PACKET_PLAY_OUT_CUSTOM_PAYLOAD_CONSTRUCTOR.invoke("LMC", pds);
        SEND_PACKET_METHOD.invoke(PLAYER_CONNECTION_FIELD.get(GET_HANDLE_METHOD.invoke(CRAFT_PLAYER_CLASS.cast(player))), payloadPacket);
    }

    /**
     * Gets the bytes that are required to send the given message
     *
     * @param messageKey      the message's key
     * @param messageContents the message's contents
     * @return the byte array that should be the payload
     */
    public static byte[] getBytesToSend(String messageKey, String messageContents) {
        // Getting an empty buffer
        ByteBuf byteBuf = Unpooled.buffer();

        // Writing the message-key to the buffer
        writeString(byteBuf, messageKey);

        // Writing the contents to the buffer
        writeString(byteBuf, messageContents);

        // Copying the buffer's bytes to the byte array
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        // Returning the byte array
        return bytes;
    }

    /**
     * Writes a varint to the given byte buffer
     *
     * @param buf   the byte buffer the int should be written to
     * @param input the int that should be written to the buffer
     */
    private static void writeVarIntToBuffer(ByteBuf buf, int input) {
        while ((input & -128) != 0) {
            buf.writeByte(input & 127 | 128);
            input >>>= 7;
        }

        buf.writeByte(input);
    }

    /**
     * Writes a string to the given byte buffer
     *
     * @param buf    the byte buffer the string should be written to
     * @param string the string that should be written to the buffer
     */
    private static void writeString(ByteBuf buf, String string) {
        byte[] abyte = string.getBytes(Charset.forName("UTF-8"));

        if (abyte.length > Short.MAX_VALUE) {
            throw new EncoderException("String too big (was " + string.length() + " bytes encoded, max " + Short.MAX_VALUE + ")");
        } else {
            writeVarIntToBuffer(buf, abyte.length);
            buf.writeBytes(abyte);
        }
    }

    /**
     * Reads a varint from the given byte buffer
     *
     * @param buf the byte buffer the varint should be read from
     * @return the int read
     */
    public static int readVarIntFromBuffer(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    /**
     * Reads a string from the given byte buffer
     *
     * @param buf       the byte buffer the string should be read from
     * @param maxLength the string's max-length
     * @return the string read
     */
    public static String readString(ByteBuf buf, int maxLength) {
        int i = readVarIntFromBuffer(buf);

        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            byte[] bytes = new byte[i];
            buf.readBytes(bytes);

            String s = new String(bytes, Charset.forName("UTF-8"));
            if (s.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }
}