# GAMunity project
Truong Bach Minh - s3891909
<br />
Nguyen Thi Nha Uyen - s3819293
<br />
Note: Members within team have the same contribution to the project.
<br />
<br />

## Table of Contents
- [Instructions](#instructions)
- [Functionalities](#functionalities)
  - [Login/Signup](#loginsignup)
  - [Home View](#home-view)
  - [Notification View](#notification-view)
  - [Profile View](#profile-view)
  - [Forum View](#forum-view)
  - [Post View](#post-view)
  - [Chat Room View](#chat-room-view)
- [Technologies](#technologies)
- [Review](#review)

## Instructions
**Operation system**: Android <br />
**Development language**: Java <br />
**Target/CompileSDK recommend**: 34 <br />
**Min SDK require version**: 26 <br />
Dowload the project and run in Android Studio as normal. Taking times to install dependencies and accessories library.<br />
Number of sending messages, images, notifications may be limited due to the free version of FireBase storage.<br />

## Functionalities
### Login/Signup
-Login: The users can login with existed accounts or google gmail.<br />
-Signup: The users can signup for a new account.<br />
### Home View
-Create forum: The user can create a new forum. The ceator will be set as chief admin of that forum.<br />
-Forum Search: The users are able to search for the specific forum by its name. <br />
-Forum Filter: The users are able to filter the specific tags that belong to Forum. <br />
-Join/UnJoin: The users can join or unjoin a forum directly in this page. <br />
-Forum Detail: The users can click to the forum and navigate to the specific forum with details. <br />
### Notification View
-Notification click: The users can click to the notification to view the forum info.<br />
-Push up notification: The users receive the nofication when the owner update forum info or someone join their own forum. <br />
### Profile View**
-View other profile: The users can click the post owner in **Post View** to view their info.<br /> 
-Follow/UnFollow: The users can follow or unfollow the specific user when visits their profile.<br />
-View own forum: The users can click on the option menu to view their joined and owned forums.<br />
-Update information: The users can update their own inforamtion.<br />
-Delete acccount: The users can delete their account which can not be reverted.<br />
### Forum View
-View forum info: Any users can see detail info of the forum (title, description, caterory, list of members and their role).<br />
-Edit forum info: chief admin and moderators can edit some of the forum info (title, description, caterory, forum icon and forum background).<br />
-Delete forum: only chief admin can delete a forum, once delete it will pop up an alert box to ask for comfirmation. If process, then all information of the forum, posts of the forum and comments of thge posts will be delete and users data will be updated accordingly.<br />
-Join/Unjoin forum: a user can join a forum they haven't joined before and vise versa. When joined, user will be added to the forum database as a member. Only members and moderators can unjoin.<br />
-Promote member to moderator/Demote moderator to member: Chief admin only. Chief admin cam make a member of the forum become the moderator and vice versa.<br />
-Remove user: Chief admin only, chief admin can remove any members or moderators out of the forum.<br />
-Access forum group chat: Any users can access the forum group chat if the are a member, moderator, chief admin of the forum through a chat icon. The users role in the chat will be similar to their role in the forum.<br />
-Create post: Any users of the forum can craete a post in the forum. When create, it will be display with somme basic information (post owner info, title, image if have, and number of like/dislike/comment).<br />
-Post details: User can click on the post and navigate to that specific post for more information.<br />
### Post View
-Edit post info: Only owner of the post can edit the info of a post (post title, description, image), currently cannot remove image if added.<br />
-Delete post: Chief admin, moderators, and owner of the post can delete the post. It will popup an alert for comfirmation. If process then the post info and all it comment will be delete and related databse be updated accordingly.<br />
-Like/Dislike post: Any user can like or dislike a post.<br />
-Create comment: Users of the forum can comment on a post. When create, it will be display with somme basic information (comment owner info, title, image if have, and number of like/dislike).<br />
-Edit comment: Only owner of the comment can edit the info of a comment (post title, description, image), currently cannot remove image if added.<br />
-Delete comment: Chief admin, moderators, and owner of the comment can delete the comment. It will popup an alert for comfirmation. If process then the comment info will be delete and related databse be updated accordingly.<br />
-Like/Dislike comment: Any user can like or dislike a comment.<br />
### Chat Room View
-Display chat: It will display the lastest 50 chat make in the chat room.<br />
-Send text message: Chat room users will be able to send text message into the chat room.<br />
-Send image message: Chat room users will be able to send image message into the chat room.<br />
-Edit chat room: Admin and moderator (for normal group chat room or individual chat room only) can edit the chat room data. For forum group chat, data will be edit through the forum (title and icon will be set as forum title and icon).<br />
-Delete chat room: Only chief admin of the chat can delete the chat room (applied for all chat room type).<br />
-Access more info: Users of chat room can see more details of the chat (name, icon, number of people join and their role).<br />
-Add new member: Admin of the chatroom (for individual chat room and normal chat room only) can add another user to the group. If added from individual chat room, it will create a new group chat and set the user that create the group chat room be admin. If add from a group chat room, then choosen user will be added to the current group chat room.<br />
-Remove a member: Admin of the chat room can remove any user of a group chat room (will not appear for individual chat room).<br />
## Technologies
**Glide**: Fast and efficient library support to load the image from the url with ==HttpUrlConnection** and store in the memory for reusing in the near future.<br />
**FireBase**: Data storage and management with easy-to-use functions and fast set up intructions.<br />
## Review

<p align="center">
   <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/32a3b773-0ac9-4623-9c4a-1c20234c026c" width="200px" height="100%" alt="login screen">
   </kbd>

  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/f73b2bbf-c57c-41d0-b20c-23ca9aea8cab" width="200px" height="100%" alt="signup screen">
   </kbd>
</p>

<p align="center">
   <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/c3748be6-c023-4350-b975-6122893172ba" width="200px" height="100%" alt="home screen">
   </kbd>
  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/d1b01d3c-85b9-46e3-9a1d-ed68866e9cd1" width="200px" height="100%" alt="post view screen">
   </kbd>
  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/3ee9bac1-2c6e-49e6-a4f2-f3e0d1e910d2" width="200px" height="100%" alt="comment view screen">
   </kbd>
  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/1a43c189-30fb-46c9-a629-81f830851d25" width="200px" height="100%" alt="write comment view screen">
   </kbd>
</p>

<p align="center">
   <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/06521a8f-d584-4875-a43c-99675a3e4eca" width="200px" height="100%" alt="chat view screen">
   </kbd>
</p>

<p align="center">
   <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/5aed8152-776b-479a-a3d7-068b5e7f60d8" width="200px" height="100%" alt="notification view screen">
   </kbd>

  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/6ae3871d-46cc-440d-9827-d19cc958d7ad" width="200px" height="100%" alt="pushup notification view screen">
   </kbd>
</p>

<p align="center">
   <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/3a2fe5c9-2896-4baf-8d61-c8bb1d5cff7d" width="200px" height="100%" alt="all forum joined view">
   </kbd>

  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/09ba13ff-c751-409b-a0d5-41f511e8ae2b" width="200px" height="100%" alt="profile view">
  </kbd>

  <kbd> 
     <img src="https://github.com/Kisari/GAMunity-Android-Development/assets/86811396/2f42a571-f193-4ac7-b0bb-c64842ae3127" width="200px" height="100%" alt="other users view">
  </kbd>
</p>







