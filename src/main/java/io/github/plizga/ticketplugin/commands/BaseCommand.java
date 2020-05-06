package io.github.plizga.ticketplugin.commands;

import co.lotc.core.command.CommandTemplate;
import io.github.plizga.ticketplugin.TicketPlugin;



public abstract class BaseCommand extends CommandTemplate
{
    protected TicketPlugin plugin = TicketPlugin.getTicketPluginInstance();


}
