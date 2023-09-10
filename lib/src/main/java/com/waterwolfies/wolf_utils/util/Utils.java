package com.waterwolfies.wolf_utils.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {

    private Utils() {}
    
    public static boolean isInvEmpty(Inventory inventory) {
        boolean result = true;

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                result = false;
            }
        }

        return result;
    }

    public static boolean isInvFull(Inventory inventory) {
        return inventory.firstEmpty() == -1;
    }

    public static boolean isCompostable(Material mat) {
        switch (mat) {
            // 100%
            case CAKE:
            case PUMPKIN_PIE:
            // 85%
            case BAKED_POTATO:
            case BREAD:
            case COOKIE:
            case FLOWERING_AZALEA:
            case HAY_BLOCK:
            case RED_MUSHROOM_BLOCK:
            case BROWN_MUSHROOM_BLOCK:
            case NETHER_WART_BLOCK:
            case PITCHER_PLANT:
            case TORCHFLOWER:
            case WARPED_WART_BLOCK:
            // 65%
            case APPLE:
            case AZALEA:
            case BEETROOT:
            case BIG_DRIPLEAF:
            case CARROT:
            case COCOA_BEANS:
            case FERN:
            case LARGE_FERN:
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case SUNFLOWER:
            case LILAC:
            case ROSE_BUSH:
            case PEONY:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case NETHER_WART:
            case CRIMSON_FUNGUS:
            case WARPED_FUNGUS:
            case LILY_PAD:
            case MUSHROOM_STEM:
            case POTATO:
            case PUMPKIN:
            case CARVED_PUMPKIN:
            case CRIMSON_ROOTS:
            case WARPED_ROOTS:
            case SEA_PICKLE:
            case SHROOMLIGHT:
            case SPORE_BLOSSOM:
            case WHEAT:
            // 50%
            case CACTUS:
            case DRIED_KELP_BLOCK:
            case FLOWERING_AZALEA_LEAVES:
            case GLOW_LICHEN:
            case MELON_SLICE:
            case NETHER_SPROUTS:
            case SUGAR_CANE:
            case TWISTING_VINES:
            case VINE:
            case WEEPING_VINES:
            // 30%
            case BEETROOT_SEEDS:
            case DRIED_KELP:
            case GLOW_BERRIES:
            case TALL_GRASS:
            case GRASS:
            case HANGING_ROOTS:
            case MANGROVE_ROOTS:
            case KELP:
            case OAK_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
            case BIRCH_LEAVES:
            case SPRUCE_LEAVES:
            case MANGROVE_LEAVES:
            case AZALEA_LEAVES:
            case MELON_SEEDS:
            case MOSS_CARPET:
            case PINK_PETALS:
            case PITCHER_POD:
            case PUMPKIN_SEEDS:
            case OAK_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case BIRCH_SAPLING:
            case SPRUCE_SAPLING:
            case SEAGRASS:
            case SMALL_DRIPLEAF:
            case SWEET_BERRIES:
            case TORCHFLOWER_SEEDS:
            case WHEAT_SEEDS: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static boolean isCompostable(ItemStack item) {
        return isCompostable(item.getType());
    }

    /* public static class LocationJSONSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {

        @Override
        public Location deserialize(JsonElement _json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            HashMap<String, Object> data = new HashMap<>();
            JsonObject json = _json.getAsJsonObject();
            data.put("world", json.get("world").getAsString());
            data.put("x", json.get("x").getAsDouble());
            data.put("y", json.get("y").getAsDouble());
            data.put("z", json.get("z").getAsDouble());
            data.put("yaw", json.get("yaw").getAsFloat());
            data.put("pitch", json.get("pitch").getAsFloat());
            return Location.deserialize(data);
        }

        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject data = new JsonObject();
            for (var entry : src.serialize().entrySet()) { // Should not contain "objects"
                JsonPrimitive value;
                if (entry.getValue() instanceof String str) {
                    value = new JsonPrimitive(str);
                } else if (entry.getValue() instanceof Boolean bool) {
                    value = new JsonPrimitive(bool);
                } else if (entry.getValue() instanceof Character _char) {
                    value = new JsonPrimitive(_char);
                } else if (entry.getValue() instanceof Number num) {
                    value = new JsonPrimitive(num);
                } else {
                    continue;
                }
                data.add(entry.getKey(), value);
            }

            return data;
        }

    } */
}
