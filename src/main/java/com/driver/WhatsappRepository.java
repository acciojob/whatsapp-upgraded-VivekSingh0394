package com.driver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class WhatsappRepository {
    HashMap<String,User> userDb = new HashMap<>();
    HashMap<String,List<User>> groupsDb=new HashMap<>();
    HashMap<String,List<Message>> usermsgDb = new HashMap<>();
    List<Message> messagesDb = new ArrayList<>();
    HashMap<String,List<Message>> groupMessageDb =new HashMap<>();
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
            groupName = users.get(1).getName();
            numberOfparticipants=2;
            groupsDb.put(groupName, users);
        }


        else if (users.size()>2)
       {
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
           groupsDb.put(groupName,users);

       }
         // int numberOfGroups = groupsDb.size()+1;



    //    }
        Group group = new Group();
        group.setName(groupName);
        group.setNumberOfParticipants(numberOfparticipants);
        return group;

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

        String groupName = group.getName();
        if(!groupsDb.containsKey(groupName))
        {
            throw new Exception("Group does not exist");
        }
        List<User> userList = groupsDb.get(groupName);
        boolean check=false;
        for(User user : userList)
        {
            if(user.getName().equals(sender.getName()))
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
        messagesDb.add(message);
      // update usermsgdb
        if(usermsgDb.size()==0 || !usermsgDb.containsKey(sender.getName()))
        {
            List<Message> messageList = new ArrayList<>();
            messageList.add(message);
            usermsgDb.put(sender.getName(),messageList);
        }
        else {

            List<Message> messageList = usermsgDb.get(sender.getName());
            if (messageList == null)
                messageList = new ArrayList<>();
            messageList.add(message);
        }

       if(groupMessageDb.size()==0 || !groupMessageDb.containsKey(groupName))
       {
           List<Message> messageList = new ArrayList<>();
           messageList.add(message);
           groupMessageDb.put(groupName,messageList);
       }
       else {
           List<Message> messageList = groupMessageDb.get(groupName);
           if (messageList == null)
               messageList = new ArrayList<>();
           messageList.add(message);
       }

    int sizeofmessages = groupMessageDb.get(groupName).size();
        return sizeofmessages;
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there
        // is only one admin and the admin rights are transferred from approver to user.

        String groupName = group.getName();
        if(!groupsDb.containsKey(groupName))
        {
            throw new Exception("Group does not exist");
        }
        String approverName = approver.getName();
        List<User> userList = groupsDb.get(groupName);
        String adminName = userList.get(0).getName();
        if(approverName.compareTo(adminName)!=0)
        {
            throw  new Exception("Approver does not have rights");
        }
        boolean check = false;
        for(User user1 : userList)
        {
            if(user1.getName().equals(user.getName()))
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
        try {
            String userName = user.getName();
            String groupName = "";
            boolean check = false;
            for(String x:groupsDb.keySet()) {

                List<User> users = groupsDb.get(x);
                for (User user1 : users) {
                    if (user1.getName().equals(userName)) {
                        groupName = x;
                        check = true;
                        break;
                    }
                }
            }

//            int i = 0;
//            for (i = 0; i < groupsDb.size(); i++) {
//                List<User> userList = groupsDb.get(i);
//                for (User user1 : userList) {
//                    if (user1.getName().equals(userName)) {
//
//                        check = true;
//                        break;
//                    }
//                }
//
//            }
            if (check == false) {
                throw new Exception("User not found");
            }
            List<User> users = groupsDb.get(groupName);
            String adminName = users.get(0).getName();
            if (user.getName().equals(adminName)) {
                throw new Exception("Cannot remove admin");
            }
            // get message list from usermsgdb and also find id of msgs
            List<Message> messageList = usermsgDb.get(userName);
            List<Integer> idList = new ArrayList<>();
            for (Message message : messageList) {
                idList.add(message.getId());
            }
            // removed from usermsgdb
            usermsgDb.remove(userName);
            // removed from msglist
            for (int j = 0; j < idList.size(); j++) {
                int id = idList.get(j);
                for (int k = 0; k < messagesDb.size(); k++) {
                    if (messagesDb.get(k).getId() == id) {
                        messagesDb.remove(messagesDb.get(k));
                    }
                }
            }

            List<Message> messageList1 = groupMessageDb.get(groupName);

            for (int k = 0; k < idList.size(); k++) {
                int id = idList.get(k);
                for (Message message : messageList1) {
                    if (message.getId() == id) {
                        messageList1.remove(message);
                    }
                }
            }
            List<User> userList = groupsDb.get(groupName);
            for (User user1 : userList) {
                if (user1.getName().equals(user.getName())) {
                    userList.remove(user1);
                }
            }
            int totalacount = 0;
            totalacount += groupsDb.get(groupName).size() + groupMessageDb.get(groupName).size();
            for(String grpname:groupMessageDb.keySet())
            {
               totalacount+= groupMessageDb.get(grpname).size();

            }
            return totalacount;
        }
        catch (Exception e)
        {
           return 0;
        }
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
