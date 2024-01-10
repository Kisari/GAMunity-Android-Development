package rmitcom.asm1.gamunity.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Constant {
    public final int SUCCESS = 200;
    public final int CREATE = 201;
    public final int EDIT = 203;
    public final int DELETE = 204;
    public final String forums = "FORUMS";
    public final String users = "USERS";
    public final String groupchats = "GROUPCHATS";

    public final ArrayList<String> tagList = new ArrayList<>(Arrays.asList("Action", "Open World", "Fighting", "Survive", "Horror", "Turn Based"));

    public final int PICK_IMAGE_BACKGROUND_REQUEST = 101;
    public final int PICK_IMAGE_ICON_REQUEST = 102;
    public final int PICK_POST_IMAGE_REQUEST = 103;
    public final int PICK_COMMENT_IMAGE_REQUEST = 104;

    public Constant() {
    }
}
