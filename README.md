# GitHubRepos
This is an Android app that enables users to view the public repositories of users on GitHub.

Features:  
-Users can select which user they'd like to view from the 'Change user' menu option and then entering the username of the user  
-Users can long click on any repository to open the repository page or the owner profile in a browser  
-A local SQLite database is used to cache the retreived reposiories  
-The app requests only 10 repos at a time, and requests more when the user scrolls to the end of the list  
-Users can swipe down to refresh the data from the GitHub API 
-Repositories that are not forkable are shown with a grey background  

TODO:  
Start fetching more data before actually reaching the last list item, and maybe parse the JSONArray response in a background thread to prevent list stuttering if fetching is started before reaching the end of the list.  


How to use:  
1-Import the project into Android Studio  
2-Build the project and run it on an Android device, or if you're patient you can use the emulator.  

Alternatively, you can just install the GitHubRepos.apk to take a quick look.
