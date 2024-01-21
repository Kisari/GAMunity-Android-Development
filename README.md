# GAMunity project
Truong Bach Minh - s3891909
<br />
<br />
## Functionalities
**Home View**<br />
-Create a new forum: The user can create a new forum. The ceator will be set as chief admin of that forum.
-Forum Search: The users are able to search for the specific forum by its name. <br />
-Forum Filter: The users are able to filter the specific tags that belong to Forum. <br />
-Join/UnJoin: The users can join or unjoin a forum directly in this page. <br />
-Forum Detail: The users can click to the forum and navigate to the specific forum with details. <br />
<br />
**Notification View**<br />
-Notification click: The users can click to the notification to view the forum info.<br />
-Push up notification: The users receive the nofication when the owner update forum info or someone join their own forum. <br />
<br />
**Profile View**<br />
-View other profile: The users can click the post owner in **Post View** to view their info.<br /> 
-Follow/UnFollow: The users can follow or unfollow the specific user when visits their profile.<br />
-View own forum: The users can click on the option menu to view their joined and owned forums.<br />
-Update information: The users can update their own inforamtion.<br />
-Delete acccount: The users can delete their account which can not be reverted.<br />
<br />
**Forum View**<br />
-Edit forum info: chief admin and moderators can edit some of the forum info (title, description, caterory, forum icon and forum background).<br />
-Delete forum: only chief admin can delete a forum, once delete it will pop up an alert box to ask for comfirmation. If process, then all information of the forum, posts of the forum and comments of thge posts will be delete and users data will be updated accordingly.<br />
-Join/Unjoin forum: a user can join a forum they haven't joined before and vise versa. When joined, user will be added to the forum database as a member. Only members and moderators can unjoin.<br />
-Promote member to moderator/Demote moderator to member: Chief admin only. Chief admin cam make a member of the forum become the moderator and vice versa.<br />
-Remove user: Chief admin only, chief admin can remove any members or moderators out of the forum.<br />
-Access forum group chat: Any users can access the forum group chat if the are a member, moderator, chief admin of the forum through a chat icon. The users role in the chat will be similar to their role in the forum.<br />
-Create post: Any users of the forum can craete a post in the forum. When create, it will be display with somme basic information (post owner info, title, image if have, and number of like/dislike/comment).<br />
-Post details: User can click on the post and navigate to that specific post for more information.<br />
<br />
**Post View**<br />
-Edit post info: Only owner of the post can edit the info of a post (post title, description, image), currently cannot remove image if added.<br />
-Delete post: Chief admin, moderators, and owner of the post can delete the post. It will popup an alert for comfirmation. If process then the post info and all it comment will be delete and related databse be updated accordingly.<br />
-Like/Dislike post: Any user can like or dislike a post.<br />
-Create comment: Users of the forum can comment on a post. When create, it will be display with somme basic information (comment owner info, title, image if have, and number of like/dislike).<br />
-Edit comment: Only owner of the comment can edit the info of a comment (post title, description, image), currently cannot remove image if added.<br />
-Delete comment: Chief admin, moderators, and owner of the comment can delete the comment. It will popup an alert for comfirmation. If process then the comment info will be delete and related databse be updated accordingly.<br />
-Like/Dislike comment: Any user can like or dislike a comment.<br />
<br />
**Chat Room View**
-Display chat: It will display the lastest 50 chat make in the chat room.<br />
-Send text message: Chat room users will be able to send text message into the chat room.<br />
-Send image message: Chat room users will be able to send image message into the chat room.<br />
-Edit chat room: Admin and moderator (for normal group chat room or individual chat room only) can edit the chat room data. For forum group chat, data will be edit through the forum (title and icon will be set as forum title and icon).<br />
-Delete chat room: Only chief admin of the chat can delete the chat room (applied for all chat room type).<br />
-Access more info: Users of chat room can see more details of the chat (name, icon, number of people join and their role).<br />
-Add new member: Admin of the chatroom (for individual chat room and normal chat room only) can add another user to the group. If added from individual chat room, it will create a new group chat and set the user that create the group chat room be admin. If add from a group chat room, then choosen user will be added to the current group chat room.<br />
-Remove a member: Admin of the chat room can remove any user of a group chat room (will not appear for individual chat room).<br />
<br />
## Technologies
**Glide**: Fast and efficient library support to load the image from the url with == HttpUrlConnection == and store in the memory for reusing in the near future

