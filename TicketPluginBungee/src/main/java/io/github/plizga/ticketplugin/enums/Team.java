package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;
import co.lotc.core.util.ColorUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public enum Team
{

    Admin     (ChatColor.of(Color.decode("#1abc9c")), ".admin"),
    Moderator (ChatColor.of(Color.decode("#1a67ed")), ".mod"),
    Tech      (ChatColor.of(Color.decode("#95de16")), ".dev"),
    Lore      (ChatColor.of(Color.decode("#fae36e")), ".lore"),
    Event     (ChatColor.of(Color.decode("#cf0606")), ".event"),
    Build     (ChatColor.of(Color.decode("#ec9706")), ".build"),
    Reception (ChatColor.of(Color.decode("#8634b3")), ".reception"),
    //Design    (ChatColor.of(Color.decode("#8634b3")), ".design"),
    Global    (ChatColor.WHITE,                       ".staff");

    public final ChatColor color;
    public final String permission;

    Team(ChatColor color, String permission) {
        this.color = color;
        this.permission = permission;
    }

    public static List<String> getAvailable(Sender player)
    {
        ArrayList<String> list = new ArrayList<>();

        for(Team t: values())
        {
            list.add(t.name());
        }
        return list;
    }

    public static Team getByName(String name)
    {
        for(Team t: values())
        {
            if(t.name().equalsIgnoreCase(name))
            {
                return t;
            }
        }
        return null;
    }


}
