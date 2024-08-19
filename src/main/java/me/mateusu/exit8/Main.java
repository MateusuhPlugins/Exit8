package me.mateusu.exit8;

import me.mateusu.exit8.InGame.Comandos;
import me.mateusu.exit8.InGame.Eventos;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public Exit8 exit8;

    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Exit8 habilitado!");
        saveDefaultConfig();
        getCommand("anomalia").setExecutor(new Comandos(this));
        getCommand("enter").setExecutor(new Comandos(this));
        getServer().getPluginManager().registerEvents(new Eventos(this), (Plugin)this);
        this.exit8 = new Exit8(this);
    }

    public void onDisable() {
        this.exit8.stop();
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Exit8 desabilitado!");
        saveConfig();
    }
}
