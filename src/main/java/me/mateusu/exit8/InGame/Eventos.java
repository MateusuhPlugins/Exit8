package me.mateusu.exit8.InGame;

import java.util.UUID;

import me.mateusu.exit8.Main;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Eventos implements Listener {
    private Main plugin;

    public Eventos(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (UUID playerId : this.plugin.exit8.playersPlaying) {
            if (playerId == player.getUniqueId())
                for (Entity entities : this.plugin.exit8.entitiesToRestore) {
                    if (entities.getType() == EntityType.ARMOR_STAND) {
                        ArmorStand as = (ArmorStand)entities;
                        if (as.getHelmet().getType() == Material.SKELETON_SKULL || as
                                .getHelmet().getType() == Material.WITHER_SKELETON_SKULL)
                            e.setCancelled(true);
                    }
                }
        }
    }

    @EventHandler
    private void onDropItem(PlayerDropItemEvent e)
    {
        Player player = e.getPlayer();
        for (UUID playerId : this.plugin.exit8.playersPlaying) {
            if (playerId == player.getUniqueId())
                if(e.getItemDrop().getItemStack().getType() == Material.PAPER && e.getItemDrop().getItemStack().hasItemMeta())
                {
                    e.setCancelled(true);
                }
        }
    }
}
