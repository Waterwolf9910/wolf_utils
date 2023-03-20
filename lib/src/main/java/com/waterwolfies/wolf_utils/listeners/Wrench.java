package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Wrench extends BaseListener {

    public Wrench(Plugin plugin) {
        super(plugin);
        if (!config.getBoolean("wrench")) {
            return;
        }
        if (Bukkit.getRecipe(recipe_key) == null) {
            ItemStack item = Bukkit.getItemFactory().createItemStack("minecraft:golden_hoe");
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.STONE_HOE);
            meta.displayName(Component.text("Wrench", NamedTextColor.GOLD));
            meta.getPersistentDataContainer().set(item_key, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.LOYALTY, 5, true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("THE BIG SPIN~", NamedTextColor.AQUA));
            meta.lore(lore);
            item.setItemMeta(meta);
            Bukkit.addRecipe(new WrenchRecipe(item.ensureServerConversions()));
        }
    }

    private NamespacedKey item_key = NamespacedKey.fromString("wrench", plugin);
    private NamespacedKey recipe_key = NamespacedKey.fromString("wrench_recipe", plugin);

    // @EventHandler
    // public void onBlockClick(Event event) {
    //     event.getPlayer();
    // }

    /**
     * Allow Item Frames to be turned invisible
     */
    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (!config.getBoolean("wrench")) {
            return;
        }
        if (!(event.getRightClicked() instanceof ItemFrame frame)) {
            return;
        }
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (item.getType() != Material.GOLDEN_HOE || !item.getItemMeta().getPersistentDataContainer().has(item_key)) {
            return;
        }

        if (event.getPlayer().isSneaking()) {
            frame.setVisible(!frame.isVisible());
            event.setCancelled(true);
        }
    }
    
    /**
     * Checks when a block is clicked
     */
    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (!config.getBoolean("wrench") || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getInteractionPoint() == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() != Material.GOLDEN_HOE || !item.getItemMeta().getPersistentDataContainer().has(item_key)) {
            return;
        }
        // String uuid = event.getPlayer().getUniqueId().toString();
        // if (cd.containsKey(uuid) && cd.get(uuid) == 1) {
        //     cd.put(uuid, 0);
        //     return;
        // }
        // cd.put(uuid, 1);
        Player player = event.getPlayer();
        Block block = player.getTargetBlockExact(5);
        if (block == null) {
            return;
        }
        if (config.getStringList("wrench_blacklist").contains(block.getType().getKey().asString())) {
            return;
        }
        
        // System.out.println(d);
        BlockData bd = block.getBlockData();
        if (player.isSneaking() && bd instanceof Openable blockData) {
            blockData.setOpen(!blockData.isOpen());
        } else if ((bd) instanceof Orientable blockData) {
            // bd = 
            reorientate(blockData);
        } else if (bd instanceof Directional blockData) {
            changedir(blockData);
        } else if (bd instanceof Rail blockData) {
            changeshape(blockData);
        } else if (bd instanceof Rotatable blockData) {
            changerot(blockData);
        } else if (bd instanceof Slab blockData) {
            changetype(blockData);
        } else if (bd instanceof FaceAttachable blockData) {
            changeface(blockData);
        }

        player.getWorld().setBlockData(block.getLocation(), bd);
        event.setCancelled(true);
    }

    protected FaceAttachable changeface(FaceAttachable blockData) {
        boolean useNext = false;
        FaceAttachable.AttachedFace _face = blockData.getAttachedFace();
        // System.out.println(faces);
        for (FaceAttachable.AttachedFace face : FaceAttachable.AttachedFace.values()) {
            // System.out.println(face);
            if (face == _face) {
                useNext = true;
                continue;
            }
            if (useNext) {
                blockData.setAttachedFace(face);
                useNext = false;
                break;
            }
        }
        // System.out.println(blockData.getFacing());
        if (useNext) {
            blockData.setAttachedFace(FaceAttachable.AttachedFace.values()[0]);
        }
        return blockData;
    }

    protected Rotatable changerot(Rotatable blockData) {
        boolean useNext = false;
        BlockFace _rot = blockData.getRotation();
        // System.out.println(faces);
        for (BlockFace rot : BlockFace.values()) {
            // System.out.println(face);
            if (rot == _rot) {
                useNext = true;
                continue;
            }
            if (useNext) {
                blockData.setRotation(rot);
                useNext = false;
                break;
            }
        }
        // System.out.println(blockData.getFacing());
        if (useNext) {
            blockData.setRotation(BlockFace.values()[0]);
        }
        return blockData;
    }

    protected Slab changetype(Slab blockData) {
        if (blockData.getType() == Slab.Type.TOP) {
            blockData.setType(Slab.Type.BOTTOM);
        } else {
            blockData.setType(Slab.Type.TOP);
        }
        return blockData;
    }

    protected Rail changeshape(Rail blockData) {
        boolean useNext = false;
        Rail.Shape _shape = blockData.getShape();
        Set<Rail.Shape> shapes = blockData.getShapes();
        // System.out.println(faces);
        for (Rail.Shape shape : shapes) {
            // System.out.println(face);
            if (shape == _shape) {
                useNext = true;
                continue;
            }
            boolean as1 = shape.name().contains("ASCEND");
            boolean as2 = _shape.name().contains("ASCEND");
            if ( (as1 && !as2) || (!as1 && as2) ) {
                continue;
            }
            if (useNext) {
                blockData.setShape(shape);
                useNext = false;
                break;
            }
        }
        // System.out.println(blockData.getFacing());
        if (useNext) {
            blockData.setShape((Rail.Shape) blockData.getShapes().toArray()[0]);
        }
        return blockData;
    }

    protected Directional changedir(Directional blockData) {
        boolean useNext = false;
        BlockFace _face = blockData.getFacing();
        Set<BlockFace> faces = blockData.getFaces();
        // System.out.println(faces);
        for (BlockFace face : faces) {
            // System.out.println(face);
            if (face == _face) {
                useNext = true;
                continue;
            }
            if (useNext) {
                blockData.setFacing(face);
                useNext = false;
                break;
            }
        }
        // System.out.println(blockData.getFacing());
        if (useNext) {
            blockData.setFacing((BlockFace)blockData.getFaces().toArray()[0]);
        }
        return blockData;
    }
    
    protected Orientable reorientate(Orientable blockData) {
        boolean useNext = false;
        Axis _axis = blockData.getAxis();
        for (Axis axis : blockData.getAxes()) {
            // System.out.println(face);
            if (axis == _axis) {
                useNext = true;
                continue;
            }
            if (useNext) {
                blockData.setAxis(axis);
                useNext = false;
                break;
            }
        }
        if (useNext) {
            blockData.setAxis((Axis) blockData.getAxes().toArray()[0]);
        }
        /* switch (blockData.getAxis()) {
            case X: {
                if (blockData.getAxes().contains(Axis.Y)) {
                    blockData.setAxis(Axis.Y);
                } else if (blockData.getAxes().contains(Axis.Z)) {
                    blockData.setAxis(Axis.Z);
                }
                break;
            }
            case Y: {
                if (blockData.getAxes().contains(Axis.Z)) {
                    blockData.setAxis(Axis.Z);
                } else if (blockData.getAxes().contains(Axis.X)) {
                    blockData.setAxis(Axis.X);
                }
                break;
            }
            case Z: {
                if (blockData.getAxes().contains(Axis.X)) {
                    blockData.setAxis(Axis.X);
                } else if (blockData.getAxes().contains(Axis.Y)) {
                    blockData.setAxis(Axis.Y);
                }
                break;
            }
        } */
        return blockData;
    }

    public class WrenchRecipe extends ShapedRecipe {

        public WrenchRecipe(ItemStack item) {
            super(Wrench.this.recipe_key, item);
            this.shape("ees", "i|i", "g|g");
            this.setIngredient('e', Material.EMERALD);
            this.setIngredient('i', Material.IRON_INGOT);
            this.setIngredient('g', Material.GOLD_INGOT);
            this.setIngredient('|', Material.STICK);
            this.setIngredient('s', Material.STRING);
        }
    }
}
