package me.mateusu.exit8.InGame;

import me.mateusu.exit8.Exit8;
import me.mateusu.exit8.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Comandos implements CommandExecutor {
    private Main plugin;

    public Comandos(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("enter")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Uso correto: /enter <nome do jogador>");
                return false;
            }
            Player targetPlayer = null;
            if (sender instanceof BlockCommandSender) {
                if (args[0].equalsIgnoreCase("@p")) {
                    BlockCommandSender commandSender = (BlockCommandSender)sender;
                    Block block = commandSender.getBlock();
                    for (Entity entities : block.getWorld().getNearbyEntities(block.getLocation(), 5.0D, 5.0D, 5.0D)) {
                        if (entities instanceof Player) {
                            Player player1 = (Player)entities;
                            if (!player1.isOp())
                                targetPlayer = player1;
                        }
                    }
                }
            } else {
                targetPlayer = Bukkit.getPlayer(args[0]);
            }
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Jogador nencontrado: " + args[0]);
                return false;
            }
            Exit8 exit8 = this.plugin.exit8;
            if (!exit8.playersPlaying.contains(targetPlayer.getUniqueId()))
                exit8.playersPlaying.add(targetPlayer.getUniqueId());
            targetPlayer.sendMessage(ChatColor.GOLD + "Você comprou seu Ticket.");
            targetPlayer.getInventory().addItem(new ItemStack[] { exit8.ticket });
            exit8.countdown();
            return true;
        }
        Player player = (Player)sender;
        FileConfiguration config = this.plugin.getConfig();
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando spode ser usado por jogadores.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("Uso: /anomalia setspawn | /anomalia list | /anomalia setnpc [n| /anomalia setlost [número]");
            return true;
        }
        if (args[0].equalsIgnoreCase("setspawn")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("Main")) {
                Location loc = player.getLocation();
                ConfigurationSection section = config.createSection("main-spawn");
                section.set("world", loc.getWorld().getName());
                section.set("x", Double.valueOf(loc.getX()));
                section.set("y", Double.valueOf(loc.getY()));
                section.set("z", Double.valueOf(loc.getZ()));
                section.set("yaw", Float.valueOf(loc.getYaw()));
                this.plugin.saveConfig();
                player.sendMessage("Sala principal salva em: " + loc.toString());
            } else {
                Location loc = player.getLocation();
                ConfigurationSection section = config.getConfigurationSection("anomaly-spawns");
                if (section == null)
                    section = config.createSection("anomaly-spawns");
                int nextIndex = section.getKeys(false).size() + 1;
                section.set(nextIndex + ".world", loc.getWorld().getName());
                section.set(nextIndex + ".x", Double.valueOf(loc.getX()));
                section.set(nextIndex + ".y", Double.valueOf(loc.getY()));
                section.set(nextIndex + ".z", Double.valueOf(loc.getZ()));
                section.set(nextIndex + ".yaw", Float.valueOf(loc.getYaw()));
                this.plugin.saveConfig();
                player.sendMessage("Anomalia salva em: " + loc.toString());
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("Coordenadas das Anomalias:");
            ConfigurationSection section = config.getConfigurationSection("anomaly-spawns");
            if (section == null || section.getKeys(false).isEmpty()) {
                player.sendMessage("Nenhuma anomalia salva.");
            } else {
                for (String key : section.getKeys(false)) {
                    String world = section.getString(key + ".world");
                    double x = section.getDouble(key + ".x");
                    double y = section.getDouble(key + ".y");
                    double z = section.getDouble(key + ".z");
                    player.sendMessage(key + ": " + world + " [" + x + ", " + y + ", " + z + "]");
                }
            }
            ConfigurationSection mainSection = config.getConfigurationSection("main-spawn");
            if (mainSection != null) {
                String world = mainSection.getString("world");
                double x = mainSection.getDouble("x");
                double y = mainSection.getDouble("y");
                double z = mainSection.getDouble("z");
                player.sendMessage("Main: " + world + " [" + x + ", " + y + ", " + z + "]");
            }
        } else if (args[0].equalsIgnoreCase("setnpc")) {
            if (args.length < 2) {
                player.sendMessage("Uso correto: /anomalia setnpc [número]");
                return true;
            }
            String coordKey = args[1];
            ConfigurationSection anomalySection = config.getConfigurationSection("anomaly-spawns." + coordKey);
            if (anomalySection == null && !coordKey.equalsIgnoreCase("Main")) {
                player.sendMessage("A coordenada " + coordKey + " nexiste.");
                return true;
            }
            Location loc = player.getLocation();
            ConfigurationSection npcSection = config.getConfigurationSection("npc-spawns");
            if (npcSection == null)
                npcSection = config.createSection("npc-spawns");
            npcSection.set(coordKey + ".world", loc.getWorld().getName());
            npcSection.set(coordKey + ".x", Double.valueOf(loc.getX()));
            npcSection.set(coordKey + ".y", Double.valueOf(loc.getY()));
            npcSection.set(coordKey + ".z", Double.valueOf(loc.getZ()));
            npcSection.set(coordKey + ".yaw", Float.valueOf(loc.getYaw()));
            this.plugin.saveConfig();
            player.sendMessage("NPC salvo em: " + loc.toString() + " para a coordenada " + coordKey);
        } else if (args[0].equalsIgnoreCase("npcPath")) {
            if (args.length < 2) {
                player.sendMessage("/anomalia npcPath [número]");
                return true;
            }
            String key = args[1];
            Location loc = player.getLocation();
            config.set("npc-paths." + key + ".world", loc.getWorld().getName());
            config.set("npc-paths." + key + ".x", Double.valueOf(loc.getX()));
            config.set("npc-paths." + key + ".y", Double.valueOf(loc.getY()));
            config.set("npc-paths." + key + ".z", Double.valueOf(loc.getZ()));
            config.set("npc-paths." + key + ".yaw", Float.valueOf(loc.getYaw()));
            this.plugin.saveConfig();
            player.sendMessage("sairde " + key);
        } else if (args[0].equalsIgnoreCase("setlost")) {
            if (args.length < 2) {
                player.sendMessage("Uso correto: /anomalia setlost [número]");
                return true;
            }
            String coordKey = args[1];
            ConfigurationSection anomalySection = config.getConfigurationSection("anomaly-spawns." + coordKey);
            if (anomalySection == null && !coordKey.equalsIgnoreCase("Main")) {
                player.sendMessage("A coordenada " + coordKey + " nexiste.");
                return true;
            }
            Location loc = player.getLocation();
            ConfigurationSection lostSection = config.getConfigurationSection("lost-spawns");
            if (lostSection == null)
                lostSection = config.createSection("lost-spawns");
            lostSection.set(coordKey + ".world", loc.getWorld().getName());
            lostSection.set(coordKey + ".x", Double.valueOf(loc.getX()));
            lostSection.set(coordKey + ".y", Double.valueOf(loc.getY()));
            lostSection.set(coordKey + ".z", Double.valueOf(loc.getZ()));
            lostSection.set(coordKey + ".yaw", Float.valueOf(loc.getYaw()));
            this.plugin.saveConfig();
            player.sendMessage("Coordenada perdida salva em: " + loc.toString() + " para a coordenada " + coordKey);
        } else if (args[0].equalsIgnoreCase("setwin")) {
            if (args.length < 2) {
                player.sendMessage("Uso correto: /anomalia setwin [número]");
                return true;
            }
            String coordKey = args[1];
            ConfigurationSection anomalySection = config.getConfigurationSection("anomaly-spawns." + coordKey);
            if (anomalySection == null && !coordKey.equalsIgnoreCase("Main")) {
                player.sendMessage("A coordenada " + coordKey + " nexiste.");
                return true;
            }
            Location loc = player.getLocation();
            ConfigurationSection winSection = config.getConfigurationSection("win-spawns");
            if (winSection == null)
                winSection = config.createSection("win-spawns");
            winSection.set(coordKey + ".world", loc.getWorld().getName());
            winSection.set(coordKey + ".x", Double.valueOf(loc.getX()));
            winSection.set(coordKey + ".y", Double.valueOf(loc.getY()));
            winSection.set(coordKey + ".z", Double.valueOf(loc.getZ()));
            winSection.set(coordKey + ".yaw", Float.valueOf(loc.getYaw()));
            this.plugin.saveConfig();
            player.sendMessage("Coordenada de vitsalva em: " + loc + " para a coordenada " + coordKey);
        } else if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            player.sendMessage("Configurarecarregada.");
        } else if (args[0].equalsIgnoreCase("stop")) {
            Exit8 exit8 = this.plugin.exit8;
            exit8.stop();
        } else {
            player.sendMessage("Comando desconhecido. Uso: /anomalia setspawn | /anomalia list | /anomalia setnpc [n| /anomalia setlost [n| /anomalia setwin [n| /anomalia reload");
        }
        return true;
    }
}