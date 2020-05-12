package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;

import java.util.ArrayList;
import java.util.List;

public enum Team
{
    Admin, Moderator, Event, Tech, Lore, Build, Global, Design;

    /*public static boolean isTeam(String name) //this method's fuckin poopy
    {
        return(name.equalsIgnoreCase(Moderator.toString()) ||
                name.equalsIgnoreCase(Event.toString()) || name.equalsIgnoreCase(Tech.toString()) ||
                name.equalsIgnoreCase(Lore.toString()) || name.equalsIgnoreCase(Build.toString()));



    }*/



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
        }
        return null;
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
