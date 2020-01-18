package org.soraworld.locket;

import org.soraworld.locket.manager.LocketManager;
import org.soraworld.locket.nms.Helper;
import org.soraworld.violet.plugin.SpigotPlugin;

/**
 * @author Himmelt
 */
public final class Locket extends SpigotPlugin<LocketManager> {
    public static final String PLUGIN_ID = "locket";
    public static final String PLUGIN_NAME = "Locket";
    public static final String PLUGIN_VERSION = "1.2.3";

    @Override
    public void onLoad() {
        Helper.init(getLogger());
        super.onLoad();
    }
}
