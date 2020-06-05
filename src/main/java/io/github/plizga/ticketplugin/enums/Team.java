package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;
import org.bukkit.ChatColor;

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
                return ChatColor.DARK_RED + "";
            case Moderator:
                return ChatColor.BLUE +"";
            case Event:
                return ChatColor.GREEN + "";
            case Tech:
                return ChatColor.DARK_AQUA + "";
            case Lore:
                return ChatColor.DARK_GREEN +"";
            case Build:
                return ChatColor.GOLD + "";
            case Design:
                return ChatColor.DARK_PURPLE + "";
            case Global:
                return ChatColor.WHITE + "";
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
