package android.example.com.githubrepos;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


//This is the llistview's adapter that will be responsible for showing the repositories in the listview
public class ReposListAdapter extends ArrayAdapter {

    List<Repository> repositories;
    Activity activity;

    public ReposListAdapter(Activity activity, List reposList){
        super(activity,R.layout.repo_list_item, reposList);
        this.repositories = reposList;
        this.activity = activity;
    }

    @Override
    public Object getItem(int position) {
        return repositories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return repositories.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;
        if(rowView == null){
            rowView = activity.getLayoutInflater().inflate(R.layout.repo_list_item, null);
            holder = new ViewHolder();
            holder.repoNameTextView = (TextView) rowView.findViewById(R.id.repo_name_text_view);
            holder.repoDescriptionTextView = (TextView) rowView.findViewById(R.id.repo_description_text_view);
            holder.repoOwnerTextView = (TextView) rowView.findViewById(R.id.repo_owner_text_view);
            holder.repoItemView = rowView.findViewById(R.id.repo_item);

            rowView.setTag(holder);
        }
        else{
            holder = (ViewHolder)rowView.getTag();
        }

        holder.repoNameTextView.setText(repositories.get(position).getRepoName());
        holder.repoDescriptionTextView.setText(repositories.get(position).getRepoDescription());
        holder.repoOwnerTextView.setText(repositories.get(position).getRepoOwner());

        if(!repositories.get(position).isForked()){
            holder.repoItemView.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_green_light));
        }
        else{
            holder.repoItemView.setBackgroundColor(Color.WHITE);
        }

        return rowView;
    }

    static class ViewHolder {
        TextView repoNameTextView;
        TextView repoDescriptionTextView;
        TextView repoOwnerTextView;
        View repoItemView;
    }
}
