package com.ong.core;

import com.ong.controllers.GroupElements.GroupChatController;
import com.ong.controllers.GroupElements.GroupMembersController;
import com.ong.controllers.HomepageElements.OnlineFriendsController;
import com.ong.controllers.PrivateChatController;
import com.ong.controllers.ScreenController;
import javafx.util.Pair;

/**
 * Class responsible for handling all Messages that are received from the Server.
 */
public class MessageHandler {
    private final Message message;

    public MessageHandler(Message message) {
        this.message = message;
    }

    public void handle() {

        if (message.getType() == null) {
            Log.warning("No message type: " + message);
            return;
        }
        /**
         * Handles the Server message which contains the information about the Online status of all User friends, by
         * forwarding it to the Homepage OnlineFriends Tab.
         */
        else if (message.getType().equals(Message.MessageType.RESPONSE_FRIENDS_ONLINE)) {
            OnlineFriendsController onlineFriendsController =
                    (OnlineFriendsController) ScreenController.getInstance().getController("OnlineFriends");
            onlineFriendsController.updateOnline(message);
        }
        /**
         * Handles the Server message which contains the information about the Online status of all members of a group,
         * by forwarding it to the appropriate GroupMembers Tab. (the name of the group that requested the
         * information is located in Message field "fromUsername")
         */
        else if (message.getType().equals(Message.MessageType.RESPONSE_MEMBERS_ONLINE)) {
            Group group = ScreenController.getInstance().getGroup(message.getFromUsername());
            GroupMembersController groupMembersController = (GroupMembersController) group.getMembersTabController();
            groupMembersController.updateOnlineMembers(message);
        }
        /**
         * Handles the receiving of a Direct message to User and forwards it to the appropriate PrivateChat Tab.
         * (the name of the user that sent the message is located in Message "fromUsername").
         * The name of all controllers of Private chats located in the ScreenController class are prefixed with "DM_".
         */
        else if (message.getType().equals(Message.MessageType.DIRECT_MESSAGE)) {
            PrivateChatController privateChatController = (PrivateChatController) ScreenController.getInstance().
                    getController("DM_" + message.getFromUsername());
            privateChatController.addMessage(message);

        }
        /**
         * Handles the receiving of a message to a Group and forwards it to the appropriate group and to the
         * appropriate channel. (the names of the group, of the category and of the channel that should be receiving
         * the message are located in Message "toGroup", "toCategory", "toChannel")
         */
        else if (message.getType().equals(Message.MessageType.GROUP_MESSAGE)) {
            Pair<String, String> pair = new Pair<>(message.getToCategory(), message.getToChannel());

            GroupChatController groupChatController = ScreenController.getInstance().getGroup(message
                    .getToGroup()).getChatTabs().get(pair).getValue();
            groupChatController.addMessageToView(message);
        }
    }
}
