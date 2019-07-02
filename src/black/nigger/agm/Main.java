package black.nigger.agm;

import io.netty.channel.*;
import net.minecraft.server.v1_12_R1.PacketPlayInFlying;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/*
    Created by John
    7/1/2019
    this is the first thing i've done with packets in a plugin so pls dont make fun of me
 */

public class Main extends JavaPlugin implements Listener {

    public HashMap<Player, Integer> a = new HashMap<Player, Integer>();
    public HashMap<Player, Integer> b = new HashMap<Player, Integer>();

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
        a.put(event.getPlayer(), 0);
        b.put(event.getPlayer(), 0);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
        a.put(event.getPlayer(), 0);
        b.put(event.getPlayer(), 0);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getPlayer().getVehicle() != null) {
            a.put(event.getPlayer(), 1);
            if(b.get(event.getPlayer()) > 3) {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "" + event.getPlayer() + " tried getting into godmode");
                event.getPlayer().leaveVehicle();
                a.put(event.getPlayer(), 0);
                b.put(event.getPlayer(), 0);
            }
        }
    }

    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(()-> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler(){

          @Override
          public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception{
              if(packet instanceof PacketPlayInFlying.PacketPlayInPosition && a.get(player) > 0) {
                  int count = b.containsKey(player) ? b.get(player) : 0;
                  b.put(player, count + 1);
                  a.put(player, 0);
              }
              super.channelRead(channelHandlerContext, packet);
          }

          @Override
          public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
              super.write(channelHandlerContext, packet, channelPromise);
          }

        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }

}