package android.example.com.githubrepos;


//This class is a Java object representation of a repository
//It'll be used when populating the listviews or when handling the repositories as objects in our app's logic
public class Repository {
    private String repoName;
    private String repoDescription;
    private String repoOwner;
    private int pageNumber;
    private String repoURL;
    private String ownerURL;
    private Boolean forkable;

    public Repository(String repoName, String repoDescription, String repoOwner, int pageNumber,
                      String repoURL, String ownerURL, boolean forkable) {
        this.repoName = repoName;
        this.repoDescription = repoDescription;
        this.repoOwner = repoOwner;
        this.pageNumber = pageNumber;
        this.repoURL = repoURL;
        this.ownerURL = ownerURL;
        this.forkable = forkable;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoDescription() {
        return repoDescription;
    }

    public void setRepoDescription(String repoDescription) {
        this.repoDescription = repoDescription;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getOwnerURL() {
        return ownerURL;
    }

    public void setOwnerURL(String ownerURL) {
        this.ownerURL = ownerURL;
    }

    public Boolean isForkable() {
        return forkable;
    }

    public void setForkable(boolean forkable) {
        this.forkable = forkable;
    }

}
