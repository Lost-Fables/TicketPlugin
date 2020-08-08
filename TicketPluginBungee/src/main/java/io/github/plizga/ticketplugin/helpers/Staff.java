package io.github.plizga.ticketplugin.helpers;

public class Staff
{
    private String uuid;
    private boolean persistent;

    public Staff(String uuid, boolean persistent)
    {
        this.uuid = uuid;
        this.persistent = persistent;
    }

    public String getUuid()
    {
        return uuid;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

}
