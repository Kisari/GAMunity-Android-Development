package rmitcom.asm1.gamunity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.model.Post;
import rmitcom.asm1.gamunity.model.User;

public class ForumMoreInfoListAdapter extends ArrayAdapter<User> {
    public ForumMoreInfoListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ForumMoreInfoListAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ForumMoreInfoListAdapter(@NonNull Context context, int resource, @NonNull User[] objects) {
        super(context, resource, objects);
    }

    public ForumMoreInfoListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull User[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ForumMoreInfoListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
    }

    public ForumMoreInfoListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<User> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.activity_forum_more_info_tab, parent, false);
            } else {
                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            }
        }

        return listItem;
    }
}
