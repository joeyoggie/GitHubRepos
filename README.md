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
parse the JSONArray response in a background thread to prevent list stuttering, and start fetching before actually reaching the last list item


How to use:  
-Import the project into Android Studio  
-Build the project and run it on an Android device, or if you're patient you can use the emulator.  
