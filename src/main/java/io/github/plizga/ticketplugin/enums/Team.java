package io.github.plizga.ticketplugin.enums;



public enum Team
{
    ADMINISTRATOR, MODERATOR, EVENT, TECH, LORE, BUILD, NONE;

    public static boolean isTeam(String name)
    {
        return(name.equalsIgnoreCase(MODERATOR.toString()) ||
                name.equalsIgnoreCase(EVENT.toString()) || name.equalsIgnoreCase(TECH.toString()) ||
                name.equalsIgnoreCase(LORE.toString()) || name.equalsIgnoreCase(BUILD.toString()));



    }


}
