package net.minecraft.server.v1_7_R4;

/**
 * @author Himmelt
 */
public abstract class NBTTagCompound {
    public abstract byte getByte(String key);

    public abstract short getShort(String key);

    public abstract int getInt(String key);

    public abstract long getLong(String key);

    public abstract float getFloat(String key);

    public abstract double getDouble(String key);

    public abstract String getString(String key);
}
