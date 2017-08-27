package org.soraworld.locket.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.soraworld.locket.api.LocketAPI;
import org.soraworld.locket.constant.Constants;
import org.soraworld.locket.constant.LangKeys;
import org.soraworld.locket.data.Serializers;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ConfigSerializable
public class Config {

    @Setting(value = "lang", comment = "Language: zh_cn,en_us...")
    private String lang = "en_us";
    @Setting(value = "chatType", comment = "ChatType: chat,action-bar")
    private ChatType chatType = ChatTypes.CHAT;
    @Setting(value = "adminNotify", comment = "Notify when player is using admin permission")
    private boolean adminNotify = false;
    @Setting(value = "protectTileEntity", comment = "Whether protect all tileentities")
    private boolean protectTileEntity = false;
    @Setting(value = "protectCarrier", comment = "Whether protect all containers")
    private boolean protectCarrier = false;
    @Setting(value = "defaultSign", comment = "Default Private text")
    private Text defaultSign = Text.of("[Private]");
    @Setting(value = "lockables", comment = "Lockable Block ID(s)")
    private List<String> lockables = new ArrayList<>();
    @Setting(value = "doubleBlocks", comment = "The double-chest like blocks, which can be opened from neighbors")
    private List<String> doubleBlocks = new ArrayList<>();

    private Path cfgDir;
    private Text privateSign;
    private CommentedConfigurationNode rootNode;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private static final ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);

    static {
        TypeSerializerCollection collection = TypeSerializers.getDefaultSerializers();
        collection.registerType(TypeToken.of(Text.class), new Serializers.TextSerializer());
        collection.registerType(TypeToken.of(ChatType.class), new Serializers.ChatTypeSerializer());
    }

    private void init() {
        lockables.add(BlockTypes.CHEST.getId());
        lockables.add(BlockTypes.TRAPPED_CHEST.getId());
        doubleBlocks.add(BlockTypes.CHEST.getId());
        doubleBlocks.add(BlockTypes.TRAPPED_CHEST.getId());
    }

    public Config() {
        init();
    }

    public Config(Path cfgDir, ConfigurationLoader<CommentedConfigurationNode> loader, ObjectMapperFactory factory) {
        this.cfgLoader = loader;
        this.cfgDir = cfgDir;
        options.setObjectMapperFactory(factory);
        init();
    }

    private void copy(Config config) {
        lang = config.lang == null ? lang : config.lang;
        chatType = config.chatType == null ? chatType : config.chatType;
        adminNotify = config.adminNotify;
        protectTileEntity = config.protectTileEntity;
        protectCarrier = config.protectCarrier;
        defaultSign = config.defaultSign == null ? defaultSign : config.defaultSign;
        lockables = config.lockables == null ? lockables : config.lockables;
    }

    public void load() {
        try {
            rootNode = cfgLoader.load(options);
            LocketAPI.LOGGER.info("config loaded from file.");
        } catch (IOException e) {
            LocketAPI.LOGGER.warn("Unable to load config from file.");
            rootNode = SimpleCommentedConfigurationNode.root(options);
        }
        // 加载节点
        try {
            copy(rootNode.getValue(Constants.TOKEN_CONFIG, this));
        } catch (ObjectMappingException e) {
            LocketAPI.LOGGER.error("ObjectMappingException");
            e.printStackTrace();
        }
        // 加载语言
        Path langFile = cfgDir.resolve("lang_" + lang + ".conf");
        ConfigurationLoader<CommentedConfigurationNode> langLoader = HoconConfigurationLoader.builder().setPath(langFile).build();
        if (Files.notExists(langFile)) {
            Asset asset = LocketAPI.PLUGIN.getAsset("lang/" + "lang_" + lang + ".conf").orElse(null);
            try {
                if (asset != null) {
                    asset.copyToFile(langFile, true);
                } else {
                    LocketAPI.LOGGER.warn("Unable to generate lang, asset was null.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                LocketAPI.LOGGER.warn("Unable to generate lang from assets.");
            }
        }
        CommentedConfigurationNode langNode;
        try {
            langNode = langLoader.load(options);
        } catch (IOException e) {
            LocketAPI.LOGGER.warn("Unable to load lang from file.");
            langNode = SimpleCommentedConfigurationNode.root(options);
        }
        // 加载节点
        try {
            I18n.LANGUAGES = langNode.getValue(Constants.TOKEN_HASH_MAP, new HashMap<>());
        } catch (ObjectMappingException e) {
            I18n.LANGUAGES = new HashMap<>();
        }
        privateSign = I18n.formatText(LangKeys.PRIVATE_SIGN);
    }

    public void save() {
        try {
            rootNode.setValue(Constants.TOKEN_CONFIG, this);
            cfgLoader.save(rootNode);
            LocketAPI.LOGGER.info("config saved to file.");
        } catch (Exception e) {
            LocketAPI.LOGGER.error("Unable to save config to file.");
        }
    }

    public boolean isLockable(Location<World> block) {
        BlockType type = block.getBlockType();
        System.out.println(type);
        if (type == BlockTypes.WALL_SIGN || type == BlockTypes.STANDING_SIGN) return false;
        TileEntity tile = block.getTileEntity().orElse(null);
        System.out.println(tile + "" + protectTileEntity + protectCarrier);
        return protectTileEntity && tile != null || protectCarrier && tile != null && tile instanceof TileEntityCarrier || lockables.contains(type.getId());
    }

    public boolean isAdminNotify() {
        return adminNotify;
    }

    public void addType(String id) {
        lockables.add(id);
    }

    public void removeType(String id) {
        lockables.remove(id);
    }

    public ChatType getChatType() {
        return chatType;
    }

    public boolean isPrivate(String line) {
        return privateSign.toPlain().equals(line) || defaultSign.toPlain().equals(line);
    }

    public Text getPrivateText() {
        return privateSign;
    }

    public Text getOwnerText(String owner) {
        return I18n.formatText(LangKeys.OWNER_FORMAT, owner);
    }

    public boolean isDChest(BlockType type) {
        return doubleBlocks.contains(type.getId());
    }
}
