package net.minecraft.server.v1_7_R4;

public abstract class PlayerConnection {
    public abstract void sendPacket(Packet packet);
}
