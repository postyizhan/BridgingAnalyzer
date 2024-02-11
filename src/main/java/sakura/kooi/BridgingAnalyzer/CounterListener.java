package sakura.kooi.BridgingAnalyzer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import sakura.kooi.BridgingAnalyzer.utils.ActionBarUtils;
import sakura.kooi.BridgingAnalyzer.utils.TitleUtils;

public class CounterListener implements Listener {
    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        if (e.getPlayer() != null && !BridgingAnalyzer.isPlacedByPlayer(e.getBlock())) {
            if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (e.getAction().toString().contains("CLICK")) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) if (e.isCancelled()) return;
            Counter c = BridgingAnalyzer.getCounter(e.getPlayer());
            c.countCPS();
            if (!c.isSpeedCountEnabled()) return;
            ActionBarUtils.sendActionBar(e.getPlayer(),
                    "§c§l最大CPS - " + c.getMaxCPS() + " §d§l当前CPS - " + c.getCPS() + " §a§l| §c§l最远距离 - " + c.getMaxBridgeLength() + " §d§l当前距离 - " + c.getBridgeLength());
        }
    }

    @EventHandler
    public void onFallDown(PlayerMoveEvent e) {
        if (e.getTo().getY() < BridgingAnalyzer.getReturnHeight()) {
            Counter c = BridgingAnalyzer.getCounter(e.getPlayer());
            if (c.isSpeedCountEnabled()) {
                TitleUtils.sendTitle(e.getPlayer(), "", "§cMax - " + c.getMaxBridgeSpeed() + " block/s", 1, 40, 1);
            }
            c.reset();
            BridgingAnalyzer.teleportCheckPoint(e.getPlayer());
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlaceBlock(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer() != null) {
            if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            // Addition
            if (e.getBlockReplacedState().getType() != Material.AIR) {
                Location fbLoc = e.getBlockReplacedState().getBlock().getLocation();
                boolean isPlacedByPlayer = false;
                for (Counter counter : BridgingAnalyzer.getCounters().values()) {
                    for (Block block : counter.getAllBlocks()) {
                        Location tbLoc = block.getLocation();
                        if (fbLoc.getBlockX() == tbLoc.getBlockX() && fbLoc
                                .getBlockY() == tbLoc.getBlockY() && fbLoc
                                .getBlockZ() == tbLoc.getBlockZ()) {
                            isPlacedByPlayer = true;
                            break;
                        }
                    }
                    if (isPlacedByPlayer) break;
                }
                if (!isPlacedByPlayer) {
                    e.setCancelled(true);
                    return;
                }
            }
            // --------------------
            Counter c = BridgingAnalyzer.getCounter(e.getPlayer());
            c.countBridge(e.getBlock());
            if (c.isSpeedCountEnabled()) {
                TitleUtils.sendTitle(e.getPlayer(), "", "§b" + c.getBridgeSpeed() + " block/s", 1, 40, 1);
            }
            final ItemStack mainHand = e.getPlayer().getItemInHand();
            final ItemStack item = new ItemStack(mainHand.getType(), 1, (short) 0, mainHand.getData().getData());
            Bukkit.getScheduler().runTaskLater(BridgingAnalyzer.getInstance(), () -> e.getPlayer().getInventory().addItem(item), 1);
        }
    }

    @EventHandler
    public void onPlaceLiquid(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer() != null) {
            if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            Counter c = BridgingAnalyzer.getCounter(e.getPlayer());
            c.addLogBlock(e.getBlockClicked().getRelative(e.getBlockFace()));
            Bukkit.getScheduler().runTaskLater(BridgingAnalyzer.getInstance(), () -> e.getPlayer().getInventory().remove(Material.BUCKET), 1);
        }
    }
}
