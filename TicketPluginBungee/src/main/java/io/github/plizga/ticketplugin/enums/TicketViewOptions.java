package io.github.plizga.ticketplugin.enums;

import co.lotc.core.agnostic.Sender;

import java.util.ArrayList;
import java.util.List;

public enum TicketViewOptions
{
    Team, Player, All;

    public static List<String> getAvailable(Sender player)
    {
        ArrayList<String> list = new ArrayList<>();

        for(TicketViewOptions t: values())
        {
            list.add(t.name());
        }
        return list;
    }

    public static TicketViewOptions getByName(String name)
    {
        for(TicketViewOptions t: values())
        {
            if(t.name().equalsIgnoreCase(name))
            {
                return t;
            }
        }
        return null;
    }


    }
