
import java.io.Serializable;
import java.util.Comparator;


class CaseInsensitiveComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareToIgnoreCase(str2);
        }
  }

class AudioTrack implements Serializable{
    
    private String title;
    private String path;
    
    AudioTrack(String title, String path)
    {
        this.title = title;
        this.path = path;
    }
    
    void setTitle(String title)
    {
        this.title = title;
    }
    
    void setPath(String path)
    {
        this.path = path;
    }
    
    String getPath()
    {
        return path;
    }
    
    String getTitle()
    {
        return title;
    }
    
}


