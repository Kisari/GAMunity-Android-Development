package rmitcom.asm1.gamunity.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Constant {
    public final int SUCCESS = 200;
    public final int CREATE = 201;
    public final int EDIT = 203;
    public final int DELETE = 204;
    public final int CHAT_REQUEST = 600;
    public final int PROFILE_REQUEST = 700;
    public final String VIEW = "VIEW";

    public final String forums = "FORUMS";

    public final String users = "USERS";
    public final String JOIN_FORUM = "JOIN_FORUM";
    public final String EDIT_FORUM = "EDIT_FORUM";
    public final String deviceTokens = "DEVICE_TOKENS";
    public final String notifications = "NOTIFICATIONS";
    public final String shareFragment = "SHARE_FRAGMENT";
    public final String notification_url = "https://fcm.googleapis.com/fcm/send";
    public final String server_key = "key=AAAAvDkna0I:APA91bHs0AaSE8NerBsGT8kwS2Xy-KQJI10Jc_P_EgUZ8ErdmZQp9ORpYtz-ko9iQ15Laqf_5aPYWar8eywjQLtnSgeFOOeSbrLFUZ76JTHvamRA0ROi_k2gQxBjHM_hNbihpNkp3QSv";
    public final String groupchats = "GROUPCHATS";
    public final ArrayList<String> tagList = new ArrayList<>(Arrays.asList("Action", "Open World", "Fighting", "Survive", "Horror", "Turn Based"));
    public final int PICK_IMAGE_BACKGROUND_REQUEST = 101;
    public final int PICK_IMAGE_ICON_REQUEST = 102;
    public final int PICK_POST_IMAGE_REQUEST = 103;
    public final int PICK_COMMENT_IMAGE_REQUEST = 104;
    public final int PICK_CHAT_IMAGE_REQUEST = 105;

    public Constant() {
    }
}
