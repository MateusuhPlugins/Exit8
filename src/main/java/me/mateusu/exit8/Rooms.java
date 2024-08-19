package me.mateusu.exit8;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Rooms {
    private Main plugin;
    private Random random = new Random();
    private NPC manoel;

    public Rooms(Main plugin) {
        this.plugin = plugin;
    }

    public void setNPC(NPC manoel) {
        this.manoel = manoel;
    }

    public void executeRoom(final String coordKey) {
        Sound sound = coordKey.equals("3") ? Sound.ENTITY_CHICKEN_EGG : Sound.ENTITY_CHICKEN_HURT;
        this.manoel.getEntity().getWorld().playSound(this.manoel.getStoredLocation(), sound, 100.0F, 1.0F);
        if (coordKey.equals("4")) {
            this.manoel.addTrait(LookClose.class);
            LookClose lookCloseTrait = (LookClose) this.manoel.getTrait(LookClose.class);
            lookCloseTrait.lookClose(true);
            lookCloseTrait.setRange(25.0D);
        }

        if (coordKey.equals("10")) {
            this.manoel.getNavigator().getLocalParameters().baseSpeed(2.0F);
        }

        if (coordKey.equals("11")) {
            LivingEntity livingEntity = (LivingEntity) this.manoel.getEntity();
            livingEntity.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1.4D);
        }

        if (coordKey.equals("6")) {
            (new BukkitRunnable() {
                int count = 0;

                public void run() {
                    Location npcLocation = Rooms.this.plugin.exit8.getNpcLocation(coordKey).add(1.0D, 0.0D, 1.0D);
                    World world = npcLocation.getWorld();

                    for (int z = 0; z < 25; ++z) {
                        Location blockLocation = npcLocation.clone().subtract(0.0D, 0.0D, (double) z);
                        Block block = world.getBlockAt(blockLocation);
                        if (block.getType() == Material.TRIPWIRE) {
                            block.setType(Material.WATER);
                            Rooms.this.plugin.exit8.blocksToRestore.add(block);
                            ++this.count;
                            break;
                        }
                    }

                    if (this.count == 3) {
                        this.cancel();
                    }

                }
            }).runTaskTimer(this.plugin, 0L, 40L);
        }

        Location npcLocation;
        if (coordKey.equals("8")) {
            npcLocation = this.plugin.exit8.getNpcLocation(coordKey).add(1.0D, 0.0D, 0.0D);
            Iterator var4 = npcLocation.getWorld().getNearbyEntities(npcLocation, 1.0D, 1.0D, 1.0D).iterator();

            while (var4.hasNext()) {
                Entity entity = (Entity) var4.next();
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    final ArmorStand armorStand = (ArmorStand) entity;
                    (new BukkitRunnable() {
                        int elapsed = 0;
                        Location asDefaultLocation = armorStand.getLocation().clone();
                        BukkitTask task;

                        public void run() {
                            Iterator var1 = armorStand.getNearbyEntities(5.0D, 5.0D, 11.0D).iterator();

                            while (var1.hasNext()) {
                                Entity entity = (Entity) var1.next();
                                if (entity instanceof Player) {
                                    Player player = (Player) entity;
                                    if (Rooms.this.plugin.exit8.playersPlaying.contains(player.getUniqueId())) {
                                        Location playerLocation = player.getLocation();
                                        Location armorStandLocation = armorStand.getLocation();
                                        armorStandLocation.setZ(playerLocation.getZ());
                                        armorStand.teleport(armorStandLocation);
                                        armorStandLocation.setDirection(playerLocation.toVector().subtract(armorStandLocation.toVector()));
                                        armorStand.teleport(armorStandLocation);
                                        armorStand.setHelmet(new ItemStack(Material.SKELETON_SKULL));
                                        playerLocation.setDirection(armorStandLocation.toVector().subtract(playerLocation.toVector()));
                                        if (this.elapsed == 0) {
                                            this.task = Bukkit.getScheduler().runTaskTimer(Rooms.this.plugin, () -> {
                                                player.teleport(playerLocation);
                                            }, 0L, 0L);
                                            Rooms.this.plugin.exit8.entitiesToRestore.add(armorStand);
                                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 100.0F, 1.5F);
                                        } else if (this.elapsed >= 3) {
                                            armorStand.setHelmet(new ItemStack(Material.IRON_HELMET));
                                            armorStand.teleport(this.asDefaultLocation);
                                            Rooms.this.plugin.exit8.entitiesToRestore.remove(armorStand);
                                            Rooms.this.plugin.exit8.playerDie();
                                            this.task.cancel();
                                            this.cancel();
                                        }

                                        ++this.elapsed;
                                    }
                                }
                            }

                        }
                    }).runTaskTimer(this.plugin, 0L, 20L);
                }
            }
        }

        Iterator var11;
        UUID playerId;
        if (coordKey.equals("15")) {
            var11 = this.plugin.exit8.playersPlaying.iterator();

            while (var11.hasNext()) {
                playerId = (UUID) var11.next();
                final Player player = Bukkit.getPlayer(playerId);
                (new BukkitRunnable() {
                    public void run() {
                        if (player.getLocation().getZ() > 113.0D) {
                            Location entityLoc = new Location(player.getWorld(), 42.0D, 104.0D, 108.0D);
                            final ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(entityLoc, EntityType.ARMOR_STAND);
                            as.setHelmet(new ItemStack(Material.BLACK_CONCRETE));
                            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                            LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
                            chestplateMeta.setColor(Color.BLACK);
                            chestplate.setItemMeta(chestplateMeta);
                            as.setChestplate(chestplate);
                            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                            leggings.setItemMeta(chestplateMeta);
                            as.setLeggings(leggings);
                            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                            boots.setItemMeta(chestplateMeta);
                            as.setBoots(boots);
                            Location loc = as.getLocation();
                            loc.setYaw(0.0F);
                            as.teleport(loc);
                            Rooms.this.plugin.exit8.entitiesToRestore.add(as);
                            (new BukkitRunnable() {
                                int elapsed = 0;

                                public void run() {
                                    if (Rooms.this.plugin.exit8.entitiesToRestore.contains(as)) {
                                        if (player.getLocation().getYaw() > 91.0F || player.getLocation().getYaw() < -91.0F) {
                                            Location loc = as.getLocation();
                                            loc.setZ(player.getLocation().getZ() - 1.0D);
                                            as.teleport(loc);
                                            loc.setDirection(player.getLocation().toVector().subtract(loc.toVector()));
                                            as.teleport(loc);
                                            if (this.elapsed == 0) {
                                                Location playerLocation = player.getLocation();
                                                playerLocation.setDirection(loc.toVector().subtract(playerLocation.toVector()));
                                                player.teleport(playerLocation);
                                                as.setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
                                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 100.0F, 1.5F);
                                            } else if (this.elapsed >= 60) {
                                                Rooms.this.plugin.exit8.entitiesToRestore.remove(as);
                                                as.remove();
                                                Rooms.this.plugin.exit8.playerDie();
                                                this.cancel();
                                            }

                                            ++this.elapsed;
                                        }
                                    } else {
                                        this.cancel();
                                    }

                                }
                            }).runTaskTimer(Rooms.this.plugin, 0L, 1L);
                            this.cancel();
                        }

                    }
                }).runTaskTimer(this.plugin, 0L, 1L);
            }
        }

        if (coordKey.equals("17")) {
            this.manoel.setName("leonaM semoG");
        }

        Location buttonLocation;
        if (coordKey.equals("20")) {
            npcLocation = this.plugin.exit8.getNpcLocation(coordKey).clone();
            buttonLocation = npcLocation.clone().add(0.0D, 3.0D, 0.0D);
            Location secondLeverLine = npcLocation.clone().add(2.0D, 3.0D, 0.0D);
            final List<Block> alavancas = new ArrayList();

            int i;
            Block block;
            for (i = 0; i < 25; ++i) {
                block = npcLocation.getWorld().getBlockAt(buttonLocation.clone().subtract(0.0D, 0.0D, (double) i));
                if (block.getBlockData() instanceof Powerable && block.getType() == Material.LEVER) {
                    alavancas.add(block);
                }
            }

            for (i = 0; i < 25; ++i) {
                block = npcLocation.getWorld().getBlockAt(secondLeverLine.clone().subtract(0.0D, 0.0D, (double) i));
                if (block.getBlockData() instanceof Powerable && block.getType() == Material.LEVER) {
                    alavancas.add(block);
                }
            }

            this.plugin.exit8.blocksToRestore.addAll(alavancas);
            (new BukkitRunnable() {
                public void run() {
                    if (Rooms.this.plugin.exit8.currentRoom != null && Rooms.this.plugin.exit8.currentRoom.equals("20")) {
                        Iterator var1 = alavancas.iterator();

                        while (var1.hasNext()) {
                            Block lever = (Block) var1.next();
                            int r = Rooms.this.random.nextInt(100);
                            if (r < 80) {
                                BlockData blockData = lever.getBlockData();
                                ((Powerable) blockData).setPowered(!((Powerable) blockData).isPowered());
                                lever.setBlockData(blockData);
                                lever.getWorld().playSound(lever.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.3F, 1.0F);
                            }
                        }
                    } else {
                        alavancas.clear();
                        this.cancel();
                    }

                }
            }).runTaskTimer(this.plugin, 0L, 10L);
        }

        if (coordKey.equals("21")) {
            npcLocation = this.plugin.exit8.getNpcLocation(coordKey).clone();
            buttonLocation = npcLocation.clone().add(1.0D, 4.0D, 0.0D).subtract(0.0D, 0.0D, 3.0D);
            Iterator var16 = buttonLocation.getWorld().getNearbyEntities(buttonLocation, 1.0D, 1.0D, 1.0D).iterator();

            while (var16.hasNext()) {
                Entity entities = (Entity) var16.next();
                if (entities.getType() == EntityType.ITEM_FRAME) {
                    final ItemFrame itemFrame = (ItemFrame) entities;
                    if (itemFrame.getLocation().getBlockX() == buttonLocation.getBlockX() && itemFrame.getLocation().getBlockY() == buttonLocation.getBlockY() && itemFrame.getLocation().getBlockZ() == buttonLocation.getBlockZ()) {
                        new BukkitRunnable() {
                            int counter = 0;
                            int counterLooking = 0;
                            boolean wasLooking = false;

                            public void run() {
                                boolean isLooking = false;
                                Iterator var2 = Rooms.this.plugin.exit8.playersPlaying.iterator();

                                while (var2.hasNext()) {
                                    UUID playerId = (UUID) var2.next();
                                    Player player = Bukkit.getPlayer(playerId);
                                    if (Rooms.this.isLookingAt(player, itemFrame) && player.getLocation().getZ() > 182.0D) {
                                        isLooking = true;
                                        break;
                                    }
                                }

                                if (isLooking) {
                                    if (!this.wasLooking) {
                                        itemFrame.setGlowing(true);
                                        this.wasLooking = true;
                                    }

                                    if (this.counterLooking == 20 || this.counterLooking == 40) {
                                        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                    }

                                    if (this.counterLooking >= 60) {
                                        itemFrame.setGlowing(false);
                                        Location leverLocation = Rooms.this.plugin.exit8.getWinLocation(coordKey).clone().add(1.0D, 0.0D, 2.0D);
                                        Block block = leverLocation.getBlock();
                                        if (block.getType() == Material.IRON_DOOR && block.getBlockData() instanceof Openable) {
                                            BlockData leverData = block.getBlockData();
                                            ((Openable) leverData).setOpen(true);
                                            block.setBlockData(leverData);
                                            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 100.0F, 1.0F);
                                            itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F);
                                            Rooms.this.plugin.exit8.blocksToRestore.add(block);
                                            this.cancel();
                                        }
                                    }

                                    ++this.counterLooking;
                                    this.counter = 0;
                                } else if (this.wasLooking) {
                                    this.counterLooking = 0;
                                    ++this.counter;
                                    if (this.counter >= 3) {
                                        itemFrame.setGlowing(false);
                                        this.wasLooking = false;
                                        itemFrame.getWorld().playSound(itemFrame.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.2F, 1.0F);
                                    }
                                }

                            }
                        }.runTaskTimer(this.plugin, 0L, 1L);
                    }
                }
            }
        }

        if (coordKey.equals("22")) {
            var11 = this.plugin.exit8.playersPlaying.iterator();

            while (var11.hasNext()) {
                playerId = (UUID) var11.next();
                this.room22(Bukkit.getPlayer(playerId), 60);
            }
        }

        this.npcWalksTo(coordKey);
    }

    private void room22(Player player, int delay) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Location npcLocation = this.plugin.exit8.getNpcLocation("22").clone().add(0.0D, 5.0D, 1.0D).subtract(1.0D, 0.0D, 0.0D);
            List<Block> blocksToRestore = new ArrayList();

            int i;
            Block block;
            for (i = 0; i <= 25; ++i) {
                block = npcLocation.clone().subtract(0.0D, 0.0D, (double) i).getBlock();
                if (block.getType() == Material.SEA_LANTERN) {
                    block.setType(Material.BLACK_WOOL);
                    blocksToRestore.add(block);
                }
            }

            for (i = 0; i <= 25; ++i) {
                block = npcLocation.clone().add(4.0D, 0.0D, 0.0D).subtract(0.0D, 0.0D, (double) i).getBlock();
                if (block.getType() == Material.SEA_LANTERN) {
                    block.setType(Material.BLACK_WOOL);
                    blocksToRestore.add(block);
                }
            }

            int duration = 120;
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration, 0));
            if (this.plugin.exit8.currentRoom.equals("22")) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    Iterator var4 = blocksToRestore.iterator();

                    while (var4.hasNext()) {
                        Block block1 = (Block) var4.next();
                        if (block1.getType() == Material.BLACK_WOOL) {
                            block1.setType(Material.SEA_LANTERN);
                        }
                    }

                    if (this.plugin.exit8.currentRoom.equals("22")) {
                        this.room22(player, delay);
                    }

                }, duration);
            }
        }, delay);
    }

    private void npcWalksTo(final String coordKey) {
        (new BukkitRunnable() {
            public void run() {
                if (Rooms.this.manoel != null && Rooms.this.manoel.isSpawned()) {
                    Location targetLocation = Rooms.this.plugin.exit8.getNpcPathLocation(coordKey);
                    if (Rooms.this.manoel.getEntity().getLocation().distance(targetLocation) < 1.5D) {
                        Rooms.this.manoel.setSneaking(true);
                        targetLocation.setYaw(targetLocation.getYaw());
                        Rooms.this.manoel.teleport(targetLocation, TeleportCause.PLUGIN);
                        if (Rooms.this.manoel.getStoredLocation().getYaw() == targetLocation.getYaw()) {
                            this.cancel();
                        } else {
                            Rooms.this.manoel.teleport(targetLocation, TeleportCause.PLUGIN);
                        }
                    } else {
                        Rooms.this.manoel.getNavigator().setTarget(targetLocation);
                    }
                } else {
                    this.cancel();
                }

            }
        }).runTaskTimer(this.plugin, 0L, 20L);
    }

    private boolean isLookingAt(Player player, Entity entity) {
        Location eye = player.getEyeLocation();
        Vector toEntity = entity.getLocation().toVector().subtract(eye.toVector());
        Vector direction = eye.getDirection();
        double dot = toEntity.normalize().dot(direction);
        return dot > 0.99D;
    }
}