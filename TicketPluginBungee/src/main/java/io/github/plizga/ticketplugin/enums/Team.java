package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;
import co.lotc.core.util.ColorUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public enum Team
{

    Admin     (ChatColor.of(ColorUtil.hexToColor("#1abc9c")), ".admin"),
    Moderator (ChatColor.of(ColorUtil.hexToColor("#1a67ed")), ".mod"),
    Tech      (ChatColor.of(ColorUtil.hexToColor("#95de16")), ".dev"),
    Lore      (ChatColor.of(ColorUtil.hexToColor("#fae36e")), ".lore"),
    Event     (ChatColor.of(ColorUtil.hexToColor("#cf0606")), ".event"),
    Build     (ChatColor.of(ColorUtil.hexToColor("#ec9706")), ".build"),
    Reception (ChatColor.of(ColorUtil.hexToColor("#8634b3")), ".reception"),
    Design    (ChatColor.of(ColorUtil.hexToColor("#8634b3")), ".design"),
    Global    (ChatColor.WHITE,                               ".staff");

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
