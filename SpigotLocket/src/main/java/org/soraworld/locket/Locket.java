package org.soraworld.locket;

import net.minecraft.server.v1_7_R4.TileEntity;
import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.TileEntitySign;
import org.soraworld.violet.plugin.SpigotPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author Himmelt
 */
public final class Locket extends SpigotPlugin<LocketManager> {
    public static final String PLUGIN_ID = "locket";
    public static final String PLUGIN_NAME = "Locket";
    public static final String PLUGIN_VERSION = "1.2.3";

    static {
        try {
            // TODO Optimize
            Field field = TileEntity.class.getDeclaredField("i");
            field.setAccessible(true);
            HashMap map = (HashMap<String, Class<?>>) field.get(null);
            map.put("Sign", TileEntitySign.class);
            field = TileEntity.class.getDeclaredField("j");
            field.setAccessible(true);
            map = (HashMap<Class<?>, String>) field.get(null);
            map.put(TileEntitySign.class, "Sign");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
