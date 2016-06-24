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
    private Boolean forked;

    public Repository(String repoName, String repoDescription, String repoOwner, int pageNumber,
                      String repoURL, String ownerURL, boolean forked) {
        this.repoName = repoName;
        this.repoDescription = repoDescription;
        this.repoOwner = repoOwner;
        this.pageNumber = pageNumber;
        this.repoURL = repoURL;
        this.ownerURL = ownerURL;
        this.forked = forked;
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

    public Boolean isForked() {
        return forked;
    }

    public void setForked(boolean forked) {
        this.forked = forked;
    }

}
