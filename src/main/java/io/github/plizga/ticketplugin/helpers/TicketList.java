package io.github.plizga.ticketplugin.helpers;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TicketList
{
    private final List<List<Ticket>> allTickets;
    private int currentIndex;

    public TicketList(List ticketList, int elementsPerPage)
    {
        int partition = ((ticketList.size() - 1) / elementsPerPage) + 1;

        this.allTickets = Lists.partition(ticketList, partition);
        this.currentIndex = 0;
    }

    public List<Ticket> getPageContent()
    {
        if(currentIndex < 0 || currentIndex > allTickets.size() -1)
        {
            throw new IllegalArgumentException("Invalid page!");
        }
        return allTickets.get(currentIndex);
    }

    public void previousPage()
    {
        if(currentIndex != 0)
        {
            currentIndex --;
        }
    }

    public void nextPage()
    {
        if(currentIndex != allTickets.size() -1)
        {
            currentIndex ++;
        }
    }

}
