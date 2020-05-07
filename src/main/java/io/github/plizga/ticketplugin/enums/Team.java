package io.github.plizga.ticketplugin.enums;


import co.lotc.core.agnostic.Sender;

import java.util.ArrayList;
import java.util.List;

public enum Team
{
    ADMINISTRATOR, MODERATOR, EVENT, TECH, LORE, BUILD, NONE;

    public static boolean isTeam(String name)
    {
        return(name.equalsIgnoreCase(MODERATOR.toString()) ||
                name.equalsIgnoreCase(EVENT.toString()) || name.equalsIgnoreCase(TECH.toString()) ||
                name.equalsIgnoreCase(LORE.toString()) || name.equalsIgnoreCase(BUILD.toString()));



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
