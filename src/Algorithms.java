
import java.util.ArrayList;


public class Algorithms {
    
    Algorithms()
    {
        
    }
    
        
     int binarySearchPath(ArrayList<AudioTrack> myFavorites, String path)
    {
        int low = 0;
        int high = myFavorites.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            String midElement = myFavorites.get(mid).getPath();

            int comparison = midElement.compareTo(path);

            if (comparison == 0) {
                return mid; // Element found
            } else if (comparison < 0) {
                low = mid + 1; 
            } else {
                high = mid - 1; // Search in the left half
            }
        }

        // If not found, return the negative index where it should be inserted
        return -1;
    }
    
   
    
     int binarySearchFav(ArrayList<AudioTrack> myFavorites, String element)
    {
        int low = 0;
        int high = myFavorites.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            String midElement = myFavorites.get(mid).getTitle();

            int comparison = midElement.compareTo(element);

            if (comparison == 0) {
                return mid; // Element found
            } else if (comparison < 0) {
                low = mid + 1; 
            } else {
                high = mid - 1; // Search in the left half
            }
        }

        // If not found, return the negative index where it should be inserted
        return -1;
    }
    
      void mergeSort(ArrayList<AudioTrack> audioTracks, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // Recursively sort the two halves
            mergeSort(audioTracks, left, mid);
            mergeSort(audioTracks, mid + 1, right);

            // Merge the sorted halves
            merge(audioTracks, left, mid, right);
        }
    }
   
      void merge(ArrayList<AudioTrack> audioTracks, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        // Create temporary arrays
        ArrayList<AudioTrack> leftArray = new ArrayList<>(audioTracks.subList(left, left + n1));
        ArrayList<AudioTrack> rightArray = new ArrayList<>(audioTracks.subList(mid + 1, mid + 1 + n2));

        // Merge the temporary arrays back into audioTracks
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray.get(i).getTitle().toLowerCase().compareTo(rightArray.get(j).getTitle().toLowerCase()) <= 0) {
                audioTracks.set(k, leftArray.get(i));
                i++;
            } else {
                audioTracks.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }

        // Copy the remaining elements of leftArray (if any)
        while (i < n1) {
            audioTracks.set(k, leftArray.get(i));
            i++;
            k++;
        }

        // Copy the remaining elements of rightArray (if any)
        while (j < n2) {
            audioTracks.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }
       
    
     void reverse(ArrayList<AudioTrack> array)
    {
        int n = array.size()-1;
        for(int i=0 ; i<(n/2)+1 ; i++)
        {
            AudioTrack temp = array.get(i);
            array.set(i, array.get(n-i));
            array.set(n-i, temp);
        }
    }
    
    
    
}
