package android.example.com.githubrepos;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


//TODO start fetching more data before actually reaching the last list item,
//TODO and maybe parse the JSONArray response in a background thread to prevent list stuttering
//TODO if fetching is started before reaching the end of the list.

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    ListView reposListView;
    ReposListAdapter reposListAdapter;
    List<Repository> repositories;
    ProgressDialog progressDialog;

    //At first = 1, then increment at each fetching trial when the user reaches the end of the list
    int pageNumber = 1;

    //Boolean that will be true whenever a fetching process is happening in the background
    boolean fetchingData = false;

    //Boolean that will indicate whether there's more repos to be retrieved from the server or not
    boolean noMoreRepos = false;

    //The user for which to show the repositories
    String user;

    //SwipeRefreshLayout is responsible for showing the loading animations to the user
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the listview and the repositories list
        reposListView = (ListView) findViewById(R.id.repos_list_view);
        repositories = new ArrayList<>();

        //Initialize the SwipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        //Get the username of the repository owner
        SharedPreferences prefs = getSharedPreferences("com.example.android.githubrepos.user", 0);
        user = prefs.getString("username", "square");

        //Initalize the local database
        DBHelper.getInstance(this);

        //Initalize the listview adapter and set it to the listview
        reposListAdapter = new ReposListAdapter(this, repositories);
        reposListView.setAdapter(reposListAdapter);

        //Hide the scrollbar from the listview
        reposListView.setVerticalScrollBarEnabled(false);

        //Add data to the listview from the local database, if empty then from the GitHub API
        loadData();

        //Set the list onLongClickListener and ask the user which page to open
        reposListView.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id){

                //Show a dialog that asks the user which url to open
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Open external page")
                        .setMessage("Which page would you like to open?")
                        .setPositiveButton("Repository", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = repositories.get(position).getRepoURL();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                browserIntent.setData(Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        })
                        .setNegativeButton("Owner", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = repositories.get(position).getOwnerURL();
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                browserIntent.setData(Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        })
                        .show();
                return true;
            }
        });

        //Set the listview's onScrollListener to detect when the list is nearly at the bottom
        reposListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("LIST", "Top list item: " + firstVisibleItem);
                Log.d("LIST", "No of visible items: " + visibleItemCount);
                Log.d("List", "Bottom list item: " + (firstVisibleItem+visibleItemCount));
                Log.d("List", "Total items: " + totalItemCount);
                if((firstVisibleItem + visibleItemCount) >= totalItemCount){
                    if(!fetchingData && !noMoreRepos){
                        getLatestRepos(user, ++pageNumber);
                    }
                }
            }
        });
    }

    //This method gets the repositories from GitHub API, 10 repositories at a time, for a given user
    //and gets the page corresponding to the pageNumber variable
    public void getLatestRepos(String user, int pageNumber){

        //Set the bool value to true to prevent calling this method multiple times at the same time
        fetchingData = true;

        //Progress dialog to be shown when the app is fetching the repositories from GitHub
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Getting repositories...");
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        //progressDialog.show();

        String url = "https://api.github.com/users/"+user+"/repos?page="+pageNumber+"&per_page=10";

        //Request a response from the provided URL.
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            JSONArray jsonArray;
            List<Repository> receivedRepos = new ArrayList<>();
            @Override
            public void onResponse(String response) {
                Log.d("MainPage", "Volley response: " + response);
                if (response.length() > 2){ //2 as empty json array contains '[]' brackets
                    try {
                        jsonArray = new JSONArray(response);
                        //Parse the data then add it to the list of repos
                        receivedRepos = parseJSON(jsonArray);
                        repositories.addAll(receivedRepos);
                        //Notify the listview adapter that there's new data to be displaed
                        reposListAdapter.notifyDataSetChanged();
                        //Insert the retrieved repos into the local database
                        DBHelper.insertRepositories(receivedRepos);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.d("MainPage", "Volley: No repos found!");
                    noMoreRepos = true;
                    if(repositories.isEmpty()){
                        Toast.makeText(MainActivity.this, "No repos found!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "No more repos found!", Toast.LENGTH_SHORT).show();
                    }
                }
                //Dismiss the progressDialog
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                //Dismiss the swiping animations
                swipeRefreshLayout.setRefreshing(false);
                //Set the boolean to false as we finished retrieving the data
                fetchingData = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse nResponse = error.networkResponse;
                Log.d("MainPage", "Volley error: " + nResponse.statusCode);
                if(nResponse.statusCode == 404){
                    Toast.makeText(MainActivity.this, "404, user not found.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to reach server.", Toast.LENGTH_SHORT).show();
                }
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                //Dismiss the swiping animations
                swipeRefreshLayout.setRefreshing(false);
                //Set the boolean to false as we finished retrieving the data
                fetchingData = false;
            }
        });
        //Add the request to the RequestQueue.
        VolleyQueue.getInstance(this).addToRequestQueue(request);
    }


    //This method parses the array that contains a number of repos and converts them into an ArrayList of Repository objects
    private List<Repository> parseJSON(JSONArray jArray){
        //The list that will be returned
        List<Repository> receivedRepositories = new ArrayList<>();

        //Temp variables to hold the data that'll be added to the Repository objects
        String repoName;
        String repoDescription;
        String repoOwner;
        String repoURL;
        String ownerURL;
        Boolean forked = false;
        //pageNumber

        //Temp Repository object that'll hold the data which will be added to the list
        Repository repo;

        JSONObject repoJSONObject;
        JSONObject ownerJSONObject;
        //Loop over the json objects
        int jsonArrayLength = jArray.length();
        for(int i = 0; i < jsonArrayLength; i++){
            try {
                //Construct the json objects
                repoJSONObject = jArray.getJSONObject(i);
                ownerJSONObject = repoJSONObject.getJSONObject("owner");

                //Get the data from the json objects
                repoName = repoJSONObject.getString("name");
                repoDescription = repoJSONObject.getString("description");
                repoOwner = ownerJSONObject.getString("login");
                repoURL = repoJSONObject.getString("html_url");
                ownerURL = ownerJSONObject.getString("html_url");
                //If the 'fork' variable is not found, it'll throw an exception, hence it's value is false
                try{
                    if(repoJSONObject.getBoolean("fork")){
                        forked = true;
                    }
                    else {
                        forked = false;
                    }
                }catch (JSONException jException){
                    forked = false;
                }

                //Create a Repository object and add it to the list
                repo = new Repository(repoName, repoDescription, repoOwner, pageNumber, repoURL, ownerURL, forked);
                receivedRepositories.add(repo);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return receivedRepositories;
    }

    //This method adds data to the listview from the local database, if empty then from the GitHub API
    private void loadData(){
        //Put the repository owner's name in the actionbar
        getSupportActionBar().setTitle(user + " repositories:");

        //Start the refreshing animation
        swipeRefreshLayout.setRefreshing(true);
        //Clear the list if it contains anything and load new data from the local database
        repositories.clear();
        repositories.addAll(DBHelper.getLocalRepos(user));
        reposListAdapter.notifyDataSetChanged();
        //Dismiss the refreshing animation
        swipeRefreshLayout.setRefreshing(false);

        //Get the maxPageNumber from the database
        pageNumber = DBHelper.getMaxPageNumber(user);
        if(pageNumber == 0){
            pageNumber = 1;
        }
        Log.d("MainPage", "PageNumber= " + pageNumber);

        if(repositories.isEmpty()){
            //Get data from GitHub API
            getLatestRepos(user, pageNumber);
        }
    }

    //This method will clear the local data on the device and retrieve fresh data from the GitHub API
    private void refreshData(){
        DBHelper.deleteAllRepos(user);
        repositories.clear();
        noMoreRepos = false;
        pageNumber = 1;
        getLatestRepos(user, pageNumber);
    }

    //This method will be called whenever the user swipes down to refresh
    @Override
    public void onRefresh(){
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_refresh) {
            //delete the data from the local database and get new data from the api
            swipeRefreshLayout.setRefreshing(true);
            refreshData();

            return true;
        }
        if(id == R.id.menu_change_user){
            //Show a dialog that asks the user which url to open
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Change user");
            builder.setMessage("Enter the new user to view their repositories");
            final EditText userEditText = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lParameters = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            userEditText.setLayoutParams(lParameters);
            builder.setView(userEditText);

            builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(userEditText.getText().toString() != null && userEditText.getText().toString().length() > 0){
                        user = userEditText.getText().toString();
                        SharedPreferences prefs = getSharedPreferences("com.example.android.githubrepos.user", 0);
                        SharedPreferences.Editor prefsEditor = prefs.edit();
                        prefsEditor.putString("username", user);
                        prefsEditor.apply();
                        loadData();
                    }

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();

            //Show the keyboard
            userEditText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
