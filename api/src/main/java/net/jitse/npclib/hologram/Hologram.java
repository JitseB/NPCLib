package net.jitse.npclib.hologram;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface Hologram {

    void show(Player player);

    void hide(Player player);

    void silentHide(UUID uuid);

    void updateText(List<String> text);
}
