package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;
import co.lotc.core.util.ColorUtil;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Team
{

    Admin, Moderator, Event, Tech, Lore, Build, Global, Design;



    public static String getPermission(Team team)
    {

        switch(team)
        {
            case Admin:
                return ".admin";
            case Moderator:
                return ".mod";
            case Event:
                return ".event";
            case Tech:
                return ".dev";
            case Lore:
                return ".lore";
            case Build:
                return ".build";
            case Design:
                return ".design";
            case Global:
                return ".staff";
            default:
                return null;
        }
    }

    public static String getColor(Team team)
    {
        switch(team)
        {
            case Admin:
                return ChatColor.of(ColorUtil.hexToColor("#1abc9c")) + "";
            case Moderator:
                return ChatColor.of(ColorUtil.hexToColor("#1a67ed")) + "";
            case Event:
                return ChatColor.of(ColorUtil.hexToColor("#cf0606")) + "";
            case Tech:
                return ChatColor.of(ColorUtil.hexToColor("#95de16")) + "";
            case Lore:
                return ChatColor.of(ColorUtil.hexToColor("#fae36e")) + "";
            case Build:
                return ChatColor.of(ColorUtil.hexToColor("#ec9706")) + "";
            case Design:
                return ChatColor.of(ColorUtil.hexToColor("#8634b3")) + "";
            case Global:
                return ChatColor.RESET + "";
            default:
                return null;
        }
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
