## Player Commands: 

- /request create &lt;Team&gt; &lt;info&gt;
    - Allows a player to create a new ticket for the specified team.
    
- /request view
    - Allows a player to view their active requests.
    
- /request viewCompleted
    - Allows a player to view their completed requests (and add reviews if not yet reviewed)
      
- /request cancel &lt;number&gt;
    - Allows a player to cancel a ticket using the numbered list provided by ```/request view```

- /request cancel all
    - Allows a player to cancel all of their current requests.
    
## Staff Commands:
- /request staff persistent
    - Allows a staff member to set themselves to "on duty." This means that the staff member will receive
    a notification whenever a new ticket is created for their team, or when a ticket is assigned to their team.

- /request staff offDuty
    - Sets a staff member to "off duty." This means the staff member will no longer see notifications for new
    tickets.
    
    
    ```
    Note for persistent and offDuty: A staff member is set to off-duty automatically upon leaving the server,
    and must manually go back on-duty when logging back on.
    ```
    
- /request staff view &lt;Team&gt;
    - Allows a staff member to view the currently open tickets for a specified team (if they have permission
    to view said team's tickets).
 
- /request staff viewAll
    - Allows a staff member to view all available tickets assigned to any team they are apart of.
    
- /request staff viewClaimed
    - Allows a staff member to view all currently-claimed tickets for any teams which they are a part of.
     
- /request staff viewClaimed &lt;Team&gt;
    - Allows a staff member to view all currently-claimed tickets for a specified team.
    
- /request staff viewMyClaimed
    - Allows a staff member to view their currently claimed tickets.
    
### Example of Workflow

A staff-member types '/request staff viewAll' and sees the following tickets:

![Image of Tickets Being Viewed](https://i.gyazo.com/65a1e28fde22cccbcd2be9a9fc782a76.png)

*The tickets appear in the quick-view list in the following format,*

```TEAM PREFIX ``` ```USERNAME``` ```FIRST CHARS``` ```X TIME AGO``` ```CLAIMER```

Where: 
- ```Team PREFIX``` represents the team the ticket is assigned to (M for moderator, B for build, G for global)
- ```USERNAME ``` represents the username of the player who created the ticket.
- ```FIRST CHARS ``` displays the first portion of the ticket's description.
    - Hovering over the ticket will display the full description. 
- ```X TIME AGO``` displays how long ago the ticket was created.
- ```CLAIMER``` displays the staff-member who has claimed the ticket ("UNCLAIMED" if, well, unclaimed).

From here, a staff-member may do the following:
- Click on the ```TEAM PREFIX``` to bring up the reassign command:
    ![Image of Reassign](https://i.gyazo.com/cef6449a18ba9030ae2ec0eebcfa7ccb.png)
    
- Click on the ```USERNAME```  to ```/msg``` the user who created the ticket.

- Click on the ```FIRST CHARS``` or ```X TIME AGO``` to open an expanded view of the ticket.

- Click on the ```CLAIMER``` to attempt to claim the ticket
 (if already claimed, only team-managers may claim the ticket).
 

Upon expanding the ticket, the staff-member is presented with expanded information, ie:
    ![Image of Expanded Ticket](https://i.gyazo.com/20c77f1adb70c743d5fb02fa69d177f8.png)
    
When presented with this expanded ticket, the staff member may take the following options:

- Clicking on the ```Player``` will prompt the staff-member to message that player.

- Clicking on the ```Status``` will attempt to claim the ticket.

- Clicking on the ```Team``` will prompt the staff-member to reassign the ticket.

- Clicking on the ```Location``` will teleport the staff-member to the origin-location of the ticket.

Additionally, the staff-member may add staff comments, add comments visible to both the player and staff, and view
current comments.

*IF* the staff-member has claimed the ticket, then they will also have an option to close the ticket.
 






