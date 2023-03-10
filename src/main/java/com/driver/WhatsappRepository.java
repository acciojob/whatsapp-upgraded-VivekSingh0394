package com.driver;

import java.util.*;

public class WhatsappRepository {
    HashMap<String,User> userDb = new HashMap<>();
    HashMap<Group,List<User>> groupsDb=new HashMap<>();
    HashMap<User,List<Message>> usermsgDb = new HashMap<>();
    List<Message> messagesDb = new ArrayList<>();
    HashMap<Group,List<Message>> groupMessageDb =new HashMap<>();
    public String createUser(String name,String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        if(userDb.containsKey(mobile))
        {
            throw new Exception("User already exists");
        }
        userDb.put(mobile,user);
        return "SUCCESS";
    }
    public Group createGroup(List<User> users)
    {
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept
        // as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name
        // of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan},
        // userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names
        // would be "Group 1", "Evan", and "Group 2" respectively.
        String groupName ="";
        int numberOfparticipants=0;
        if(users.size()==2)
        {

            Group group = new Group();
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(2);
            groupsDb.put(group,users);
            return group;
        }
        Group group1 = new Group();
           int count =0;
           for(List<User> userList:groupsDb.values())
           {
               int size = userList.size();
               if(size > 2)
                   count++;
           }
           int numberOfGroups =count+1;
           groupName = "Group "+String.valueOf(numberOfGroups);
           numberOfparticipants= users.size();
           group1.setName(groupName);
           group1.setNumberOfParticipants(numberOfparticipants);
           groupsDb.put(group1,users);
           return group1;


         // int numberOfGroups = groupsDb.size()+1;



    //    }
//        Group group = new Group();
//        group.setName(groupName);
//        group.setNumberOfParticipants(numberOfparticipants);
//        return group;

    }
    public int createMessage(String content) {
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        Message message =new Message();
        int id = messagesDb.size();
        id=id+1;
        message.setId(id);
        message.setContent(content);
        message.setTimestamp(new Date());

         messagesDb.add(message);
         return id;

    }
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

       // String groupName = group.getName();
        if(!groupsDb.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }
        List<User> userList = groupsDb.get(group);
        boolean check=false;
        for(User user : userList)
        {
            if(user.equals(sender))
            {
                check =true;
                break;
            }
        }
        if(check==false)
        {
            throw new Exception("You are not allowed to send message");
        }
    // update message list;
       // messagesDb.add(message);
      // update usermsgdb
        if(usermsgDb.containsKey(sender))
        {
            usermsgDb.get(sender).add(message);
//            List<Message> messageList = new ArrayList<>();
//            messageList.add(message);
//            usermsgDb.put(sender.getName(),messageList);
        }
        else {

            List<Message> messageList = new ArrayList<>();
            messageList.add(message);
            usermsgDb.put(sender,messageList);
        }

       if(groupMessageDb.containsKey(group))
       {
           groupMessageDb.get(group).add(message);
          // List<Message> messageList = new ArrayList<>();
          // messageList.add(message);
          // groupMessageDb.put(groupName,messageList);
       }
       else {
           List<Message> messageList =new ArrayList<>();
            messageList.add(message);
           groupMessageDb.put(group,messageList);
       }

    int sizeofmessages = groupMessageDb.get(group).size();
        return sizeofmessages;
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there
        // is only one admin and the admin rights are transferred from approver to user.

        //String groupName = group.getName();
        if(!groupsDb.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }
        //String approverName = approver.getName();
        List<User> userList = groupsDb.get(group);
        User admin = userList.get(0);
        if(!approver.equals(admin))
        {
            throw  new Exception("Approver does not have rights");
        }
        boolean check = false;
        for(User user1 : userList)
        {
            if(user1.equals(user))
            {
                check = true;
                break;
            }
        }
        if(check == false)
        {
            throw  new Exception("User is not a participant");
        }
        userList.remove(user);
        userList.add(0,user);

        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception{
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from
        // all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users
        // in the group + the updated number of messages in group + the updated number of overall messages)

        boolean check = false;
        int groupSize = 0;
        int messageCount = 0;
        int overallMessageCount = messagesDb.size();
        Group groupToRemoveFrom = null;
        for (Map.Entry<Group, List<User>> entry : groupsDb.entrySet()) {
            List<User> userList = entry.getValue();
            if (userList.contains(user))
            {
                check = true;
                groupToRemoveFrom = entry.getKey();
                if (userList.get(0).equals(user))
                {
                    throw new Exception("Cannot remove admin");
                }
               messageCount= usermsgDb.get(user).size();
                userList.remove(user);
                groupSize = userList.size();
                break;
            }
        }
        if (check == false)
        {
            throw new Exception("User not found");
        }
        if (usermsgDb.containsKey(user))
        {
       //     messageCount = usermsgDb.get(user).size();
            usermsgDb.remove(user);
        }
return groupSize + groupMessageDb.get(groupToRemoveFrom).size()-messageCount + overallMessageCount-messageCount;
//        if (usermsgDb.containsKey(user))
//        {
//            messageCount = usermsgDb.get(user).size() - 2;
//            usermsgDb.remove(user);
//        }
//
//
//        return groupSize + messageCount + overallMessageCount;
    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        // This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        int msgCount =0;
        Date latest = null;
        String msg="";
      for(int i =0 ; i < messagesDb.size();i++)
      {
          Date s= messagesDb.get(i).getTimestamp();
          if(s.compareTo(start) >0 && s.compareTo(end)<0)
          {
              msgCount++;

          }
          if(latest == null || latest.compareTo(s)<0)
          {
              latest =s;
              msg= messagesDb.get(i).getContent();
          }


      }
      if(msgCount <K)
      {
         throw new Exception("K is greater than the number of messages");
      }

        return msg;
    }
}
