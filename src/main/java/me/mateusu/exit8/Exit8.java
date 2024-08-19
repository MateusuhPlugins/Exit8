package me.mateusu.exit8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Exit8 {

    private List<String> availableAnomalySpawns = new ArrayList();
    private Map<String, Location> rooms = new HashMap();
    public List<NPC> npcs = new ArrayList();
    public List<UUID> playersPlaying = new ArrayList();
    public List<Block> blocksToRestore = new ArrayList();
    public List<Entity> entitiesToRestore = new ArrayList();

    private boolean playerWin = false;
    private boolean playerInRoom = false;
    private Random random = new Random();
    private final Location firstRoom;
    private final Main plugin;
    public String currentRoom;
    private Rooms room;
    public ItemStack ticket;

    public Exit8(Main plugin) {
        this.plugin = plugin;
        this.firstRoom = this.getMainLocation();
        this.room = new Rooms(plugin);
        this.initializeRooms();
        this.initializeTicket();
    }

    public void countdown() {
        (new BukkitRunnable() {
            int i = 5;

            public void run() {
                Iterator var1 = Exit8.this.playersPlaying.iterator();

                while(var1.hasNext()) {
                    UUID playerId = (UUID)var1.next();
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&fJogo começando em: &a" + this.i), "");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        if (this.i == 0) {
                            player.resetTitle();
                            Exit8.this.start();
                            this.cancel();
                        }
                    }
                }

                --this.i;
            }
        }).runTaskTimer(this.plugin, 0L, 20L);
    }

    private void start() {
        Iterator var1 = this.playersPlaying.iterator();

        while(var1.hasNext()) {
            UUID playerId = (UUID)var1.next();
            Player player = Bukkit.getPlayer(playerId);
            this.currentRoom = "Main";
            player.teleport(this.firstRoom);
            this.playerLocationCheck(player);
        }

        this.initializeAvailableAnomalySpawns();
    }

    public void stop() {
        Iterator var1 = this.playersPlaying.iterator();

        while(var1.hasNext()) {
            UUID playerId = (UUID)var1.next();
            Player player = Bukkit.getPlayer(playerId);
            ItemStack[] var4 = player.getInventory().getContents();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                ItemStack item = var4[var6];
                if(item != null) {
                    if (item.getType() == Material.PAPER) {
                        item.setAmount(0);
                        break;
                    }
                }
            }

            if(playerWin)
            {
                playerWin = false;
                return;
            }

            player.teleport(Bukkit.getWorld("void").getSpawnLocation());
            player.stopSound(Sound.ENTITY_CHICKEN_EGG);
            player.stopSound(Sound.ENTITY_CHICKEN_HURT);
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0F, 1.0F);
            Bukkit.broadcastMessage(ChatColor.RED + "O jogo foi finalizado.");
        }

        this.restoreRooms();
        this.killNpcs();
        this.playersPlaying.clear();
        this.currentRoom = null;
    }

    public void killNpcs() {
        Iterator var1 = this.npcs.iterator();

        while(var1.hasNext()) {
            NPC npc = (NPC)var1.next();
            npc.destroy();
        }

        this.npcs.clear();
    }

    public void initializeTicket() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Ticket");
        meta.setCustomModelData(1);
        item.setItemMeta(meta);
        this.ticket = item;
    }

    private void playerLocationCheck(final Player player) {
        (new BukkitRunnable() {
            public void run() {
                if (!Exit8.this.playersPlaying.contains(player.getUniqueId())) {
                    this.cancel();
                } else {
                    if (!Exit8.this.playerInRoom && player.getWorld().getBlockAt(player.getLocation().subtract(0.0D, 1.0D, 0.0D)).getType() == Material.GRAY_CONCRETE && Exit8.this.currentRoom != null) {
                        Exit8.this.playerInRoom = true;
                        Exit8.this.startRoom(Exit8.this.currentRoom);
                    } else if (Exit8.this.playerInRoom) {
                        if (player.getLocation().getBlock().getType() == Material.WATER) {
                            Exit8.this.playerDie();
                            player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
                            return;
                        }

                        Iterator var1 = Exit8.this.plugin.getConfig().getConfigurationSection("win-spawns").getKeys(false).iterator();

                        String key;
                        Location lostLocation;
                        ItemStack[] var4;
                        int var5;
                        int var6;
                        ItemStack item;
                        while(var1.hasNext()) {
                            key = (String)var1.next();
                            lostLocation = Exit8.this.getWinLocation(key);
                            if (lostLocation != null && lostLocation.distance(player.getLocation()) < 1.5D) {
                                var4 = player.getInventory().getContents();
                                var5 = var4.length;

                                for(var6 = 0; var6 < var5; ++var6) {
                                    item = var4[var6];
                                    if (item != null && item.getType() == Material.PAPER && item.hasItemMeta()) {
                                        item.setAmount(item.getAmount() + 1);
                                    }
                                }

                                Exit8.this.teleportPlayer();
                                return;
                            }
                        }

                        var1 = Exit8.this.plugin.getConfig().getConfigurationSection("lost-spawns").getKeys(false).iterator();

                        while(var1.hasNext()) {
                            key = (String)var1.next();
                            lostLocation = Exit8.this.getLostLocation(key);
                            if (lostLocation != null && lostLocation.distance(player.getLocation()) < 1.5D) {
                                var4 = player.getInventory().getContents();
                                var5 = var4.length;

                                for(var6 = 0; var6 < var5; ++var6) {
                                    item = var4[var6];
                                    if (item != null && item.getType() == Material.PAPER && item.hasItemMeta()) {
                                        item.setAmount(1);
                                    }
                                }

                                Exit8.this.teleportPlayer();
                                return;
                            }
                        }
                    }

                }
            }
        }).runTaskTimer(this.plugin, 0L, 1L);
    }

    public void playerDie() {
        Iterator var1 = this.playersPlaying.iterator();

        while(var1.hasNext()) {
            UUID playerId = (UUID)var1.next();
            Player player = Bukkit.getPlayer(playerId);
            player.sendTitle(ChatColor.RED + "Você morreu", "");
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0F, 1.0F);
        }

        this.stop();
    }

    private void teleportPlayer() {
        this.killNpcs();
        this.restoreRooms();

        Player player;
        for(Iterator var1 = this.playersPlaying.iterator(); var1.hasNext(); this.currentRoom = this.getCurrentRoom(player.getLocation())) {
            UUID playerId = (UUID)var1.next();
            player = Bukkit.getPlayer(playerId);
            this.playerInRoom = false;
            player.stopSound(Sound.ENTITY_CHICKEN_EGG);
            player.stopSound(Sound.ENTITY_CHICKEN_HURT);
            ItemStack[] var4 = player.getInventory().getContents();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                ItemStack items = var4[var6];
                if(items != null) {
                    if (items.getType() == Material.PAPER && items.hasItemMeta() && items.getAmount() > 9) {
                        playerWin = true;

                        player.sendTitle("Chegou ao seu destino!", ChatColor.GOLD + "Xique Xique - Bahia");
                        player.playSound(player.getLocation(), Sound.ENTITY_MINECART_RIDING, 1.0f ,1.0f);
                        player.teleport(new Location(player.getWorld(), -11, 112, -74));
                        items.setAmount(0);
                        this.stop();
                        return;
                    }
                }
            }

            if (this.getRandom() < 35) { //30% de chance de NÃO ser uma anomalia
                player.teleport(this.getMainLocation());
            } else {
                player.teleport(this.getRandomSpawnLocation());
            }
        }

    }

    private void restoreRooms() {
        Iterator var1 = this.blocksToRestore.iterator();

        while(var1.hasNext()) {
            Block block = (Block)var1.next();
            if (block.getType() == Material.WATER) {
                block.setType(Material.AIR);
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    block.setType(Material.TRIPWIRE);
                }, 80L);
            } else {
                BlockData blockData;
                if (block.getType() == Material.LEVER) {
                    blockData = block.getBlockData();
                    ((Powerable)blockData).setPowered(false);
                    block.setBlockData(blockData);
                } else if (block.getType() == Material.IRON_DOOR) {
                    blockData = block.getBlockData();
                    ((Openable)blockData).setOpen(false);
                    block.setBlockData(blockData);
                } else if (block.getType() == Material.BLACK_WOOL) {
                    block.setType(Material.SEA_LANTERN);
                }
            }
        }

        this.blocksToRestore.clear();
        var1 = this.entitiesToRestore.iterator();

        while(true) {
            while(true) {
                Entity entities;
                do {
                    if (!var1.hasNext()) {
                        this.entitiesToRestore.clear();
                        var1 = this.playersPlaying.iterator();

                        while(var1.hasNext()) {
                            UUID playerId = (UUID)var1.next();
                            Bukkit.getPlayer(playerId).removePotionEffect(PotionEffectType.DARKNESS);
                        }

                        return;
                    }

                    entities = (Entity)var1.next();
                } while(entities.getType() != EntityType.ARMOR_STAND);

                ArmorStand as = (ArmorStand)entities;
                if (as.getHelmet().getType() == Material.SKELETON_SKULL) {
                    as.teleport(new Location(Bukkit.getWorld("void"), -11.0D, 104.0D, 84.0D, 180.0F, 0.0F));
                    as.setHelmet(new ItemStack(Material.IRON_HELMET));
                } else if (as.getHelmet().getType() == Material.BLACK_CONCRETE || as.getHelmet().getType() == Material.WITHER_SKELETON_SKULL) {
                    as.remove();
                }
            }
        }
    }

    private void startRoom(String coordKey) {
        String skinName;
        String signature;
        String data;
        if (coordKey.equals("16")) {
            skinName = "http://textures.minecraft.net/texture/4d1272de2b5e2b815470918cfc0cefd7f6416eb303aa6dc28436c9c7133183f5";
            signature = "tiJwUAtZxmSCogVTuIGm9IxAdI+yULGCi9wk557iwxJ24wDbuBS9ORYD+kQ6nIg91XElExmQex5Eiesygg1BdkXHliw8xcZEqj1HsMu92Qdggwx7Q1fVCWRnpBXAAwkengYEp/LydAAOujiBfTZfbwpkGW2g1H6yH+XeC1tLPRKrCfoD4DiM1du5pyhUkoLMvtVg4m2z7vIV/7gBrz9+KCjjhZRYagJLMkm4qUHOF1OkUgTxcXv/bGKwFnQd5vTxOQNaH8mK0WWz2ciywBWRh2Ttp3nCW9u7t/B3tQDm8vFA7AaaHha3EI+TJjC5Y1lp60KM/RUOI4kgcnpbwjDJJYMl4KCl/Iv8Lw0Q0Xo44PnXCsucDl+irCuh3TN5mM02y1d8ao0gl2xoE9X0RjfTuWS7pBB+7+qhxHzqgwRwcgGO/a5+HFfrkastQMdZ5Az+5pEsjIltrfLbFGeIbdCQP7yjEvMlBdBaZKi4/WTs51keoU4jyt8B05cEe1sPtWbInEAh+MHTJXA02rcThMu3yNGD0wz8lH19Sr/B+M3COwBq/nMLKzKu3JK095WAsOMtCxT9y0HfiA8GYpVKNANi4vMG4udnP4HyTaNMIUiVHoYOx3nXjCDFHz9KXYZbMsqK1KMTIjQRpsWh2/lkXgvAw411u4eN0ygOtUKqSU65HO8=";
            data = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTUxODQwMjk3NiwKICAicHJvZmlsZUlkIiA6ICJjY2MxNGM2ZDUwMDE0MjBmYmMxYjkyMTM2Y2JmOWU4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJXaGlybGluZ0F0b2w5NDQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQxMjcyZGUyYjVlMmI4MTU0NzA5MThjZmMwY2VmZDdmNjQxNmViMzAzYWE2ZGMyODQzNmM5YzcxMzMxODNmNSIKICAgIH0KICB9Cn0=";
        } else {
            skinName = "http://textures.minecraft.net/texture/ff48a6cbae4809facf150a44352f5d145853f15699d4e7b9fe05c09bb7a874ec";
            signature = "WFBLJccYJijyeUCMy1EzU01VGaYcAlBJerClLrzdPL/ORJnXF3Fvrp5nPiPzPy4XlVkyqq1gDNyGMdJogDWvpbJn+qriTKu2MDXsbLnf70KpsFZAnseZbxoOEyJpOVBtEJym5gpRvvb/lZ2EAGi803ZS1JQeyzGItN9DlhOd8kWJEby8ykK4S7lYd1EKKkvaCt134XHT9nYHMyrSiwqmwkWLAEnJUjLDd0QlJ7MmazZYCWA2aTcWkA8xplpgvckDMZthosUXZ5tJAij42f6VJynHMpxEOACnB+kJoO2X+PZtWqFIrQDf+iIzYHIhAXT3UGJIfMAi4HhT+nSY65Ham0Xv2tj9CwrR89zKBSTifIbjY+Py0d7lCd/nY0TI8fvmrUGXHbSxitFvgl7MjtVTKIe9rNoU6TUCj/wqw0ve8RVR+dXcyKULe0B4j44x3b/33aLtxQZa6JsxIMNsVTnWFkJw34RKyWmD2Sa3c8GRO2YCEwUGnCjsNpNBqvhy6qVqZx3MOA3+h+LaW3uPG3FsOhYhq239IU7gYuNOhnJtBIb6+jV8RI3RrfEjfxAcAn9prufJvhQmfvnwTGKzC+JoJr4nCNnNcoU8ydRFJPHPAwxBK3WMUCisHOzJH0AZnFECbtaACwVxhvwdNejIurW7l+lb5fI9MsU9UE8ih4ZAJtA=";
            data = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTM3NjY2MzUyOCwKICAicHJvZmlsZUlkIiA6ICI1MjhlYzVmMmEzZmM0MDA0YjYwY2IwOTA5Y2JiMjdjYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQdWxpenppIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZmNDhhNmNiYWU0ODA5ZmFjZjE1MGE0NDM1MmY1ZDE0NTg1M2YxNTY5OWQ0ZTdiOWZlMDVjMDliYjdhODc0ZWMiCiAgICB9CiAgfQp9";
        }

        NPC manoel = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Manoel Gomes");
        SkinTrait skinTrait = (SkinTrait)manoel.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(skinName, signature, data);
        manoel.spawn(this.getNpcLocation(coordKey));
        this.npcs.add(manoel);
        this.room.setNPC(manoel);
        this.room.executeRoom(coordKey);
    }

    private int getRandom() {
        return this.random.nextInt(100);
    }

    public Location getNpcLocation(String coordKey) {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("npc-spawns." + coordKey);
        if (section == null) {
            return null;
        } else {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float)section.getDouble("yaw");
            return new Location(world, x, y, z, yaw, 0.0F);
        }
    }

    public Location getNpcPathLocation(String coordKey) {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("npc-paths." + coordKey);
        if (section == null) {
            return null;
        } else {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float)section.getDouble("yaw");
            return new Location(world, x, y, z, yaw, 0.0F);
        }
    }

    public Location getWinLocation(String coordKey) {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("win-spawns." + coordKey);
        if (section == null) {
            return null;
        } else {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            return new Location(world, x, y, z);
        }
    }

    public Location getLostLocation(String coordKey) {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("lost-spawns." + coordKey);
        if (section == null) {
            return null;
        } else {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            return new Location(world, x, y, z);
        }
    }

    public Location getRandomSpawnLocation() {
        if (this.availableAnomalySpawns.isEmpty()) {
            this.initializeAvailableAnomalySpawns();
        }

        String randomKey = (String)this.availableAnomalySpawns.remove(this.random.nextInt(this.availableAnomalySpawns.size()));
        return this.getAnomalyLocation(randomKey);
    }

    private Location getAnomalyLocation(String coordKey) {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("anomaly-spawns." + coordKey);
        if (section == null) {
            return null;
        } else {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float)section.getDouble("yaw");
            return new Location(world, x, y, z, yaw, 0.0F);
        }
    }

    private void initializeAvailableAnomalySpawns() {
        this.availableAnomalySpawns.clear();
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("anomaly-spawns");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            this.availableAnomalySpawns.addAll(keys);
        }

    }

    public Location getMainLocation() {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("main-spawn");
        if (section == null) {
            this.plugin.getLogger().warning("Não foi localizado um main spawn!");
            return null;
        } else {
            String worldName = section.getString("world");
            if (worldName == null) {
                this.plugin.getLogger().warning("Esse mundo não possui main spawn!");
                return null;
            } else {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    this.plugin.getLogger().warning("O mundo '" + worldName + "' não foi encontrado!");
                    return null;
                } else {
                    double x = section.getDouble("x");
                    double y = section.getDouble("y");
                    double z = section.getDouble("z");
                    float yaw = (float)section.getDouble("yaw");
                    return new Location(world, x, y, z, yaw, 0.0F);
                }
            }
        }
    }

    private void initializeRooms() {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection anomalySection = config.getConfigurationSection("anomaly-spawns");
        if (anomalySection != null) {
            Iterator var3 = anomalySection.getKeys(false).iterator();

            while(var3.hasNext()) {
                String key = (String)var3.next();
                this.rooms.put(key, this.getAnomalyLocation(key));
            }
        }

        Location mainLocation = this.getMainLocation();
        if (mainLocation != null) {
            this.rooms.put("main-spawn", mainLocation);
        }

    }

    private String getCurrentRoom(Location location) {
        Iterator var2 = this.rooms.entrySet().iterator();

        Entry entry;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            entry = (Entry)var2.next();
        } while(!(((Location)entry.getValue()).distance(location) < 1.0D));

        if (((String)entry.getKey()).contains("main")) {
            return "Main";
        } else {
            return (String)entry.getKey();
        }
    }
}