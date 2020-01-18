package org.soraworld.locket.nms;

import net.minecraft.server.v1_7_R4.TileEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.soraworld.violet.nms.Version.v1_7_R4;

public class Helper {
    public static void init(Logger logger) {
        if (v1_7_R4) {
            try {
                // i => field_145855_i => nameToClassMap
                Field nameToClass = getFiled(TileEntity.class, "i", "field_145855_i", "nameToClassMap");
                nameToClass.setAccessible(true);
                HashMap<String, Class<?>> nameToClassMap = (HashMap<String, Class<?>>) nameToClass.get(null);
                nameToClassMap.put("Sign", TileEntitySign.class);
                logger.info("Inject id Sign to class " + TileEntitySign.class.getName());
                // j => field_145853_j => classToNameMap
                Field classToName = getFiled(TileEntity.class, "j", "field_145853_j", "classToNameMap");
                classToName.setAccessible(true);
                HashMap<Class<?>, String> classToNameMap = (HashMap<Class<?>, String>) classToName.get(null);
                classToNameMap.put(TileEntitySign.class, "Sign");
                logger.info("Inject class " + TileEntitySign.class.getName() + " to id Sign");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static Field getFiled(Class<?> clazz, String... names) throws NoSuchFieldException {
        if (names == null || names.length == 0) throw new NoSuchFieldException("empty field name");
        for (String name : names) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (Throwable ignored) {
            }
        }
        throw new NoSuchFieldException(Arrays.toString(names));
    }
}
