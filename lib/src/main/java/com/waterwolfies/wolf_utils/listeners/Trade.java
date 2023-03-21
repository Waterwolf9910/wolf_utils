package com.waterwolfies.wolf_utils.listeners;

import com.waterwolfies.wolf_utils.Plugin;
import com.waterwolfies.wolf_utils.util.PersonalInventory;
import com.waterwolfies.wolf_utils.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class Trade extends BaseListener implements TabCompleter {

    public Trade(Plugin plugin) {
        super(plugin);
        if (config.getBoolean("trade")) {
            var command = plugin.getCommand("trade");
            command.setExecutor(new TradeCommand());
            command.setTabCompleter(this);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completes = new ArrayList<>();
        if (args.length == 1) {
            if ("inventory".startsWith(args[0])) {
                completes.add("inventory");
            }
            if ("request".startsWith(args[0])) {
                completes.add("request");
            }
            if ("accept".startsWith(args[0])) {
                completes.add("accept");
            }
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completes.add(PlainTextComponentSerializer.plainText().serialize(player.displayName()));
            }
        }
        return completes;
    }

    /**
     * Cooldowns for {@link #onPlayerClick(PlayerInteractEntityEvent) event}
     */
    public Map<String, Integer> cd = new HashMap<>();
    /**
     * <p> The map of requested inventories 
     * <p> Format ${reqUUID}: { ${sendUUID}: ${{@link TradeInventory inventory}} }
     */
    public Map<String, Map<String, TradeInventory>> tradeStorage = new HashMap<>();
    /**
     * <p> A Map of {@link java.util.Timer Timers}
     * <p> Format ${sender}: { ${requestee}: ${timer}  }
     */
    public Map<String, Map<String, Timer>> timeouts = new HashMap<>();
    /**
     * <p> A Map of {@link org.bukkit.inventory.Inventory Inventories} mapped to a uuid
     */
    public Map<String, Inventory> tradeInventories = new HashMap<>();

    /**
     * Checks when a {@link org.bukkit.entity.Player Player} right clicks on another {@link org.bukkit.entity.Player Player}
     * @param event The Event
     */
    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) {
        if (!config.getBoolean("trade")) {
            return;
        }
        Player eventPlayer = event.getPlayer();
        if (cd.containsKey(eventPlayer.getUniqueId().toString()) && cd.get(eventPlayer.getUniqueId().toString()) == 1) {
            cd.put(eventPlayer.getUniqueId().toString(), 0);
            return;
        }
        cd.put(eventPlayer.getUniqueId().toString(), 1);
        if (!(eventPlayer.isSneaking() && (event.getRightClicked()) instanceof Player targetPlayer)) {
            return;
        }
        
        requestSend(eventPlayer, targetPlayer);
    }

    /**
     * Send a Trade Request to a {@link org.bukkit.entity.Player Player}
     * @param sender The Player sending the request
     * @param requestee The Player recieving the request
     * @return False if the request was already sent
     */
    public void requestSend(Player sender, Player requestee) {
        // eventPlayer.getWorld().sendMessage(Component.text(targetPlayer.getUniqueId().toString()));
        String senderUUID = sender.getUniqueId().toString();
        String requesteeUUID = requestee.getUniqueId().toString();
        timeouts.computeIfAbsent(senderUUID, key -> new HashMap<>());
        var timeout = timeouts.get(senderUUID);
        if (timeout.containsKey(requesteeUUID)) {
            sender.sendMessage(Component.text("You have already sent this player a trade request", NamedTextColor.RED));
            return;
        }
        Timer timer = new Timer("Trade Timeout", false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeouts.get(senderUUID).remove(requesteeUUID);
                sender.sendMessage(Component.text("Trade Timedout"));
                requestee.sendMessage(Component.text("Trade Timedout"));
            }
            
            //mil -> s -> min
        }, 1000 * 60 * 2l);
        sender.sendMessage(Component.text("Trade Request Sent", NamedTextColor.GREEN));
        requestee.sendMessage(sender.displayName()
            .colorIfAbsent(NamedTextColor.GREEN)
            .append(Component.text(" has requested to trade with you. ", NamedTextColor.GREEN))
            .append(Component.text("Click Here", Style.style(NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .clickEvent(ClickEvent.runCommand("/trade accept " + PlainTextComponentSerializer.plainText().serialize(sender.displayName())))
                .hoverEvent(HoverEvent.showText(Component.text("Accept Trade"))))
            .append(Component.text(" to trade with them.", NamedTextColor.GREEN))
        );
        timeouts.get(senderUUID).put(requesteeUUID, timer);
        TradeInventory inventory = new TradeInventory(senderUUID, requesteeUUID);
        tradeStorage.computeIfAbsent(requesteeUUID, key -> new HashMap<>());
        tradeStorage.get(requesteeUUID).put(senderUUID, inventory);
        // sender.openInventory(inventory.getInventory());
        // requestee.openInventory(inventory.getInventory());
    }

    @EventHandler
    public void onPlayerCloseInv2(InventoryCloseEvent event){
        if (!(event.getInventory().getHolder() instanceof PersonalInventory inv) || !config.getBoolean("trade")) {
            return;
        }
        PersonalInventory _inv = (PersonalInventory) tradeInventories.get(event.getPlayer().getUniqueId().toString()).getHolder();
        if (inv.getID() != _inv.getID()) {
            return;
        }
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack item : inv.getInventory().getContents()) {
            if (item != null) {
                list.add(item);
            }
        }
        HashMap<Integer, ItemStack> remain = event.getPlayer().getInventory().addItem(list.toArray(new ItemStack[0]));
        if (!remain.isEmpty()) {
            return;
        }
        inv.getInventory().setContents(remain.values().toArray(new ItemStack[0]));
    }

    @EventHandler
    public void onPlayerCloseInv(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeInventory tradeInventory) || !config.getBoolean("trade")) {
            return;
        }
        try {
            tradeInventory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeInventory) || !config.getBoolean("trade")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.DOUBLE_CLICK || event.isShiftClick()) {
            if (event.getView().getTopInventory() instanceof TradeInventory) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof TradeInventory tradeInventory)  || !config.getBoolean("trade")) {
            return;
        }
        HumanEntity player = event.getWhoClicked();
        boolean isRight = player.getUniqueId().toString().equals(tradeInventory.getRightUUID());
        int slot = event.getSlot();

        if (slot == 22) {
            tradeInventory.setConfirm(true, isRight);
            event.setCancelled(true);
        } else if (slot == 31) {
            try {
                tradeInventory.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            event.setCancelled(true);
        } else if (slot == -999) {// outside of inventory
        } else if (
            ((slot % 9) < 5 && isRight) ||
            ((slot % 9) > 3 && !isRight)
        ) {
            event.setCancelled(true);
        } else {
            tradeInventory.setConfirm(false, isRight);
        }
    }
    
    protected class TradeInventory implements InventoryHolder {
        private final Inventory inventory;
        private final String leftUUID;
        private final String rightUUID;
        private boolean confirmLeft = false;
        private boolean confirmRight = false;
        private boolean disposed = false;
        public TradeInventory(String leftUUID, String rightUUID) {
            inventory = Bukkit.createInventory(this, 54, Component.text("Trading"));
            ItemStack none = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta noneMeta = none.getItemMeta();
            noneMeta.displayName(Component.text(" "));
            none.setItemMeta(noneMeta);
            ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.displayName(Component.text("Confirm", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.ITALIC)));
            confirm.setItemMeta(confirmMeta);
            ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.displayName(Component.text("Cancel", Style.style(NamedTextColor.RED, TextDecoration.BOLD)));
            cancel.setItemMeta(cancelMeta);
            int placement = 4;
            while (placement < 54) {
                inventory.setItem(placement, none.ensureServerConversions());
                placement = placement + 9;
            }
            inventory.setItem(22, confirm.ensureServerConversions());
            inventory.setItem(31, cancel.ensureServerConversions());
            this.leftUUID = leftUUID;
            this.rightUUID = rightUUID;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }

        public String getLeftUUID() {
            return this.leftUUID;
        }

        public String getRightUUID() {
            return rightUUID;
        }

        public void setConfirm(boolean result, boolean isRight) {
            if (isRight) {
                confirmRight = result;
            } else {
                confirmLeft = result;
            }
            if (!isConfirmed()) {
                return;
            }
            List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (viewers.isEmpty()) {
                    return;
                }
                viewers.forEach(entity -> {
                    disposed = true;
                    entity.sendMessage(Component.text("Trade Complete!", NamedTextColor.GREEN));
                    entity.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
                    String uuid = entity.getUniqueId().toString();
                    tradeInventories.computeIfAbsent(uuid, k -> new PersonalInventory("Trade Result").getInventory());
                    var _inventory = tradeInventories.get(uuid);
                    boolean right = uuid.equals(rightUUID);
                    int slot = right ? 0 : 5;
                    while (slot < 54) {
                        if (( (slot % 9) < 5 && !right ) || ( (slot % 9) > 3 && right )) {
                            slot++;
                            continue;
                        }
                        ItemStack item = inventory.getItem(slot);
                        slot++;
                        if (item == null) {
                            continue;
                        }
                        _inventory.addItem(item);
                    }
                    entity.openInventory(_inventory);
                });
            });
        }

        public boolean isConfirmed() {
            return confirmLeft && confirmRight;
        }

        public void close() {
            if (!disposed) {
                disposed = true;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, inventory::close);
                /* var viewers = inventory.getViewers();
                if (viewers.isEmpty()) {
                    return;
                }
                viewers.forEach(entity -> {
                    if (!entity.getUniqueId().toString().equals(firstUUID)) {
                        entity.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        entity.sendMessage(Component.text("Canceled", NamedTextColor.RED));
                    }
                }); */
            }
        }
    }

    protected class TradeCommand implements CommandExecutor {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("A Player must run this command", NamedTextColor.RED));
                return false;
            }
            
            if (args.length < 1) {
                player.sendMessage(Component.text("No Argument found", NamedTextColor.RED));
                return false;
            }
            switch (args[0]) {
                case "inventory": {
                    if (!tradeInventories.containsKey(player.getUniqueId().toString())) { 
                        player.sendMessage(Component.text("You have no items in your trade inventory", NamedTextColor.RED));
                        return true;
                    }
                    if (Utils.isInvEmpty(tradeInventories.get(player.getUniqueId().toString()))) {
                        player.sendMessage(Component.text("You have no items in your trade inventory", NamedTextColor.RED));
                        return true;
                    }
                    player.openInventory(tradeInventories.get(player.getUniqueId().toString()));
                    break;
                }
                case "request": {
                    if (args.length < 2) {
                        player.sendMessage(Component.text("Missing Player", NamedTextColor.RED));
                        return false;
                    }
                    Player reqPlayer = Bukkit.getPlayer(args[1]);
                    if (reqPlayer == null) {
                        player.sendMessage(Component.text("No player was found", NamedTextColor.RED));
                        return true;
                    }
                    if (reqPlayer.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                        player.sendMessage(Component.text("You cannot trade with yourself"));
                    }
                    break;
                }
                case "confirm":
                case "accept": {
                    if (args.length < 2) {
                        player.sendMessage(Component.text("Missing Player", NamedTextColor.RED));
                        return false;
                    }
                    Player senderPlayer = Bukkit.getPlayer(args[1]);
                    if (senderPlayer == null) {
                        player.sendMessage(Component.text("No player was found", NamedTextColor.RED));
                        return true;
                    }
                    var map = tradeStorage.get(player.getUniqueId().toString());
                    if (!map.containsKey(senderPlayer.getUniqueId().toString())) {
                        player.sendMessage(Component.text("No request from " + PlainTextComponentSerializer.plainText().serialize(senderPlayer.displayName())));
                        return true;
                    }
                    timeouts.get(senderPlayer.getUniqueId().toString()).get(player.getUniqueId().toString()).cancel();
                    timeouts.get(senderPlayer.getUniqueId().toString()).get(player.getUniqueId().toString()).purge();
                    timeouts.get(senderPlayer.getUniqueId().toString()).remove(player.getUniqueId().toString());
                    player.openInventory(map.get(senderPlayer.getUniqueId().toString()).getInventory());
                    senderPlayer.openInventory(map.get(senderPlayer.getUniqueId().toString()).getInventory());
                }
            }

            return true;
        }
        
    }

}
