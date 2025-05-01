package com.bnstra.npclib.example;

import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.api.NPC;
import com.bnstra.npclib.api.events.NPCInteractEvent;
import com.bnstra.npclib.api.skin.Skin;
import com.bnstra.npclib.api.state.NPCAnimation;
import com.bnstra.npclib.api.state.NPCSlot;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jitse Boonstra
 */
public class NPCLibTest2 extends JavaPlugin implements Listener {

    """
    A bit of clunky code that shows some of the libraries functionalities.
    """

    private NPC testNpc;
    private boolean skin1active = true;

    private static Skin SKIN1 = new Skin("ewogICJ0aW1lc3RhbXAiIDogMTc0NTUwNTc5MDA5NiwKICAicHJvZmlsZUlkIiA6ICI5ZDYzZjMxMGNiZWE0MWI1YmQ4YTM4ZmExYTBiMDI4MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa3libHVlZm94dGFpbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81ZjQwNGY3ZTcyYTEzMjA0NWIzMWIzNzIwYzJlNjljNzk2N2M3NmMyNmVhYmRlN2I2NGY3MmYxNDc1MmQ5YTRhIgogICAgfQogIH0KfQ==", "C86w+2a8sMinI1sXSlor2ttkpcoZKuE5rDWDybnY/kCnJsqeV2siOiJDW9skUCzE1G9g7zvc8mr2auBynPRhM70dlScrEIJNt9XzEcKk9rq1sGaSKbaMQQ4oXOQUrbUDV3XkKKZAN1ysbZqnJNDspbMROujYiDW+XyR7Fd+RJNA8eXK+pMWn+LDzNh+GNli8NN3iohLjDLY70CkLscce+A3zFalLgEpDxlCW2RVvz5MF6Ix4TKP9iXI2SvK0zoazHRdlX4Ld5JwPFmIAiZyd2+Z3spbUeIOwQX+Qc3opsBFLv13AOP944VmxoDQpGA7YaJEEjBS44pL1nPUD7C2cpVgh4gWngpOUU27SyzC7xrPEwfDEP9U1YBsgwHpUUO0OQp3nzOtali/M586PsJw+7YIaxDULgsfdoozXmo0Pg4cH8+AI4Gtmk4QulPmu1/4iaZhY6EE5N5sOsLd1Voi9bdMcBNS+wtnRV8OubTLmmIiSnOIqhz0acU5vSCY672+xKb308gBvgIrTm5lwRHCrSGD6dOdo0J43q6uUfIEUb1rOqKasKFZ4bSvXXD3Hr3XfcsCF2rhQViiJMNT6o6Z4+qOxQuabHdZv81qOySzofgqLC3HdM99kKx+xvBYEe54qY74duMM10+zmeyaiIMbHvN4gxbwjTQN3qduvP8uc0gQ=");
    private static Skin SKIN2 = new Skin("ewogICJ0aW1lc3RhbXAiIDogMTc0NTcyMjg4Nzk1NiwKICAicHJvZmlsZUlkIiA6ICJhZTg3MzEyNjBmMzY0ZWE2YjU3YTRkYjI5Mjk1YTA1OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJGdW50aW1lX0ZveHlfMTkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmU2MDkzNmE3ODg0YjBlMWE3ZWE3MWM1ZTk4ZjlhNjc5NjQwZDYyM2IxNmUwMzA1Mjc2NmViNTU1OWE3MzQ0IgogICAgfQogIH0KfQ==", "N4yQXhVsRJVIGMxYI2cd0wBRSEb9qBlvAn34CXeeLj0H88kV5k/fPLdZ3fWZC+AbOBLY/KGc8nWtd/aa6W4fyydblf/+b+0sEcvc6mIzfZ7y4Cv3O966VnLfCbzDhJgxehPank8jbPnzQlk8KywaRqobjdPxyRzhh5JlG1097P8m5L02uCSAhvVpTx5xfIhOTcrM3utv2WLKe9X2oxDjDRrdlGiAvell640Ep0pD5jzokfIhJojWQTSwCGj0zdlivHr6jXfk43ywuCJqtAoPcu/ylrg6p5O59BbDEzK33RikkcOiuLMtx013+zLyAekv04NCHaN0WCfOCepVnEblRcU3FwR/lw8s+OZr7NhvMR6eykKb0c5mdnDyzF9f41hsgcuyODeV47RfRXFU7hyVFKBhaXBEKZMvmEfWITNhEu+trU6Yzm4lhBmwF6lhIdjvR3WSJaaIvfAyxGxcsqwC62HB1m1oYFqiCetzOLP/pOXXeV/MdgZnBA82LhxHDNnSqLKAMRcS5colGilyXNkwivQhFibW5cHFoaVyEirU56TsvC1/HeOmLHjxWKGywwdynIK9WTxDOheD4tQzPKVAq+7NU3OVn3LKhmYFLgDUGsIg1XlcrmNNGURlzUYAzj4u5yKmP9ltXvyg2mosckRJffIibbpM5WLObJXZmAXs1YM=");

    @Override
    public void onEnable() {
        NPCLib npcLib = new NPCLib(this);

        this.testNpc = npcLib.createNPC(Arrays.asList(ChatColor.WHITE + "This is a test", ChatColor.RED + "Click me!"));
        testNpc.setSkin(SKIN1);
        testNpc.setLocation(new Location(Bukkit.getWorld("world"), -156.5, 64, -71.5, 0.0f, 0.0f));
        testNpc.setItem(NPCSlot.CHESTPLATE, new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        testNpc.setItem(NPCSlot.OFFHAND, new ItemStack(Material.DIAMOND_BLOCK));
        testNpc.setItem(NPCSlot.MAINHAND, new ItemStack(Material.DIAMOND_HOE));
        testNpc.create();

        for (Player player : Bukkit.getOnlinePlayers())
            testNpc.show(player);

        AtomicInteger i = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (testNpc == null) return;

            String dots = Strings.repeat(".", i.get());
            for (Player player : Bukkit.getOnlinePlayers())
                testNpc.setText(player, Arrays.asList(ChatColor.WHITE + "This is a test" + dots,
                        ChatColor.RED.toString() + (i.get()%2 == 0 ? "" : ChatColor.MAGIC) + "Click me!"));
            i.getAndIncrement();

            if (i.get() >= 4) i.set(0);
        }, 0, 10);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        testNpc.destroy();
    }

    @EventHandler
    public void onPlayerInteract(NPCInteractEvent event) {
        if (event.getClickType() == NPCInteractEvent.ClickType.LEFT_CLICK) {
            testNpc.updateSkin(skin1active ? SKIN2 : SKIN1);
            skin1active = !skin1active;
            testNpc.playAnimation(NPCAnimation.SWING_MAINHAND);
            event.getWhoClicked().sendMessage("Changed skin of NPC and played animations (left-click)");
        }

        if (event.getClickType() == NPCInteractEvent.ClickType.RIGHT_CLICK) {
            testNpc.lookAt(event.getWhoClicked().getLocation());
            testNpc.playAnimation(NPCAnimation.MAGICAL_DAMAGE);
            testNpc.playAnimation(NPCAnimation.CRITICAL_DAMAGE);
            testNpc.playAnimation(NPCAnimation.TAKE_DAMAGE);
            event.getWhoClicked().sendMessage("Changed head orientation of NPC and played animations (right-click)");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (testNpc == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "NPC was not loaded yet.");
            return;
        }
        testNpc.show(event.getPlayer());
    }
}