import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MediaPlayerApplication extends javax.swing.JFrame {
    long positionPtr = 0;
    AudioTrack selectedSearched;
    boolean state_pbtn;
    boolean state_nextbtn = false;
    boolean toLoop;
    Clip clip;
    int currentTrackIndex;
    long pausedMicrosecondPosition = 0;
    boolean isPlaying;
    Thread musicThread;
    ArrayList<AudioTrack> addedSongs = new ArrayList<>();
    ArrayList<AudioTrack> myFavorites = new ArrayList<>();
    Algorithms algo;
    JFileChooser filechooser;
    float currentVolume;
    float previousVolume;
    boolean activate = true;
    FloatControl fc;
    boolean muted = false;
    User user;

    

    
    void mantainHistory()
    {
        File file = new File("Users History\\"+user.username+"_history.txt");
        try{
            FileWriter fw = new FileWriter(file,true);
            for(AudioTrack adtk: addedSongs)
            {
                fw.write(adtk.getTitle()+";"+adtk.getPath()+"\n");
            }
            fw.close();
        }catch(Exception e)
        {
            
        }
    }
    
    void playSelectedMainThread()
    {   
        int i=0;
        for(AudioTrack adtk : addedSongs)
        {
            System.out.println(selectedSearched.getTitle());
            System.out.println(adtk.getTitle());
            System.out.println(i);
            if(adtk.getTitle().equals(selectedSearched.getTitle()))
            {
                System.out.println("MATCHED");
                break;   
            }
            i++;
        }
        
        stopMusic();
        musicThread = null;
        currentTrackIndex = i;
        System.out.println(currentTrackIndex);
        playMusic();
    }
     
    public MediaPlayerApplication(User user) {
        this.user = user;
        currentVolume = 0;
        previousVolume = 0;
        algo = new Algorithms();
        toLoop = false;
        filechooser = new JFileChooser();
        isPlaying = false;
        currentTrackIndex = 0;
        loadFavorites();
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame on the screen
        setVisible(true);
        AllSongsName.setEditable(false);
        setTitle("Funky Beats Player");
        selectedSearched = null;
        if (user != null) {
            mute_state.setText("Welcome " + user.fname + " " + user.lname);
        }

        currentVolume = 0;
    }
    //Volume Settings
    void volumeUp() {
        currentVolume += 2.0f;
        if (currentVolume > 6.0f) {
            currentVolume = 6.0f;
        }
        fc.setValue(currentVolume);
    }
    void volumeDown() {
        currentVolume -= 2.0f;
        if (currentVolume < -80.0f) {
            currentVolume = -80.0f;
        }
        fc.setValue(currentVolume);
    }
    void mute() {
        if (!muted) {
            muted = true;
            previousVolume = currentVolume;
            currentVolume = -80.0f;
            fc.setValue(currentVolume);
            mute_state.setText("Muted");
        } else {
            mute_state.setText("Enjoy the Music....");
            muted = false;
            currentVolume = previousVolume;
            fc.setValue(previousVolume);
        }
    }
    // Favorites Settings
    void writeFavorites() {
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(new FileOutputStream("Users Favorites\\" + user.personal_directory));
            for (AudioTrack obj : myFavorites) {
                oos.writeObject(obj);
            }
            System.out.println("Object list has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (AudioTrack ad : myFavorites) {
            System.out.println(ad.getPath());
        }

    }
    void loadFavorites() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("Users Favorites\\" + user.personal_directory));
            Object obj;
            while ((obj = ois.readObject()) != null) {
                if (obj instanceof AudioTrack) {
                    myFavorites.add((AudioTrack) obj);
                }
            }
            System.out.println("Object list has been read from the file.");
        } catch (Exception e) {
            // End of file reached, do nothing
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // PlayPuaseNextPrevious Settings
    void playMusic() {
        isPlaying = true;

        musicThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create AudioInputStream object
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(addedSongs.get(currentTrackIndex).getPath()));

                    // Get Clip
                    clip = AudioSystem.getClip();
                    // Open audioInputStream to the clip
                    clip.open(audioInputStream);

                    fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    fc.setValue(currentVolume);

                    nowPlaying.setText("Playing: " + addedSongs.get(currentTrackIndex).getTitle());
                    clip.setMicrosecondPosition(positionPtr);
                    System.out.println("Position Playing: "+positionPtr);
                    clip.start();                    
                    while (clip.getMicrosecondPosition() != clip.getMicrosecondLength()) {
                         positionPtr = clip.getMicrosecondPosition();
                       //  System.out.println(positionPtr);
                    }
                    //positionPtr = 0;
                    playNext();

                    // musicThread.join();  // Wait for the musicThread to complete
                } catch (Exception e) {
                    System.out.println("Exception in Playing Music");
                    //     System.out.println(e.getMessage());
                }
            }
        });

        musicThread.start();
    }
    void stopMusic() {
        try {
            if (isPlaying && clip != null) {
                isPlaying = false;
                positionPtr = clip.getMicrosecondPosition()+0;
                System.out.println(positionPtr);
                clip.stop();
                clip.flush(); // Add this line to clear the buffer
                clip.close();
                clip = null; // Reset the Clip object
               
            }
        } catch (Exception e) {
            System.out.println("Error Stopping");
        }
    }
    void playNext() {
        try {
            positionPtr = 0;
            stopMusic();
            musicThread = null;

            if (currentTrackIndex < addedSongs.size() - 1) {
                currentTrackIndex++;
                playMusic();
            } else {
                currentTrackIndex = 0;
                if (toLoop) {
                    playMusic();
                } else {
                    nowPlaying.setText("We Hit Different!!!");
                }

            }

        } catch (Exception e) {
            System.out.println("error in play next");
        }
    }
    void playPrevious() {

        try {
            stopMusic();
            musicThread = null;

            if (currentTrackIndex > 0) {
                currentTrackIndex--;
                playMusic();
            } else {
                currentTrackIndex = addedSongs.size() - 1;
                if (toLoop) {
                    playMusic();
                } else {
                    nowPlaying.setText("We Hit Different!!!");
                }
            }

        } catch (Exception e) {
            System.out.println("error in play next");
        }

    }
    void shuffle() {

        if (addedSongs.size() == 0) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        int startRange = 0;
        int endRange = addedSongs.size() - 1;

        Random random = new Random();

        int randomNumber = random.nextInt((endRange - startRange) + 1) + startRange;
        stopMusic();
        musicThread = null;
        currentTrackIndex = randomNumber;
        playMusic();
    }
    void addToFav() {

        if (musicThread != null) {
            if (algo.binarySearchFav(myFavorites, addedSongs.get(currentTrackIndex).getTitle()) == -1) {
                myFavorites.add(addedSongs.get(currentTrackIndex));
                algo.mergeSort(myFavorites, 0, myFavorites.size() - 1);
                writeFavorites();
            } else {
                JOptionPane.showMessageDialog(null, "Already Added to Favorites");
            }

        }
    }
    void playFavorites() {
        if (myFavorites.size() == 0) {
            JOptionPane.showMessageDialog(null, "No Tracks found in favorites");
        } else {
            try {
                stopMusic();
                musicThread = null;
                addedSongs.removeAll(addedSongs);
                addedSongs.addAll(myFavorites);
                String str = getSongsNamesForDisplay();
                AllSongsName.setText(str);
                AllSongsName.setEditable(false);
                currentTrackIndex = 0;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    String getSongsNamesForDisplay() {
        String str = "";
        for (AudioTrack ad : addedSongs) {
            str = str + ad.getTitle() + "\n";
        }
        return str;
    }
    void addTracks() {
        int option = filechooser.showDialog(null, "Open my File");

        try {
            FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("Audio Files", "mp3", "wav", "ogg");
            filechooser.setFileFilter(audioFilter);

            if (option == JFileChooser.APPROVE_OPTION) {
                System.out.println("Selected Path: " + filechooser.getSelectedFile().getAbsolutePath());
                System.out.println(filechooser.getSelectedFile().getName());

                int SameName = algo.binarySearchFav(addedSongs, filechooser.getSelectedFile().getName());
                int SamePath = algo.binarySearchPath(addedSongs, filechooser.getSelectedFile().getAbsolutePath());

                if (SameName != -1 && SamePath != -1) {
                    JOptionPane.showMessageDialog(null, addedSongs.get(SameName).getTitle() + " Already exists");
                    throw new Exception("File Already Exist");
                }

                AudioTrack adtk = new AudioTrack(filechooser.getSelectedFile().getName(), filechooser.getSelectedFile().getAbsolutePath());
                addedSongs.add(adtk);
            } else {
                System.out.println("File could not be selected");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        String str = getSongsNamesForDisplay();

        AllSongsName.setText(str);
        AllSongsName.setEditable(false);

    }
    boolean asc = false;
    boolean dsc = false;
    void sortAscending() {

        if (addedSongs.size() == 0) {
            JOptionPane.showMessageDialog(null, "No tracks added");
            return;
        }

        if (asc == true) {
            return;
        }

        stopMusic();
        musicThread = null;

        if (asc == false && dsc == true) {
            System.out.println("Sorting in Ascending by reversing the descending");
            algo.reverse(addedSongs);
        }

        if (asc == false && dsc == false) {
            System.out.println("Sorting in Ascending by using merge sort");
            algo.mergeSort(addedSongs, 0, addedSongs.size() - 1);
        }

        String str = getSongsNamesForDisplay();
        AllSongsName.setText(str);
        AllSongsName.setEditable(false);
        currentTrackIndex = 0;
        asc = true;
        dsc = false;

    }
    void sortDescending() {
        if (addedSongs.size() == 0) {
            JOptionPane.showMessageDialog(null, "No tracks added");
            return;
        }

        if (dsc == true) {
            return;
        }

        stopMusic();
        musicThread = null;

        if (asc == true && dsc == false) {
            System.out.println("Sorting in Ascending by reversing the descending");
            algo.reverse(addedSongs);
        }

        if (asc == false && dsc == false) {
            System.out.println("Sorting in Ascending by using merge sort");
            algo.mergeSort(addedSongs, 0, addedSongs.size() - 1);
            algo.reverse(addedSongs);
        }

        String str = getSongsNamesForDisplay();
        AllSongsName.setText(str);
        AllSongsName.setEditable(false);
        currentTrackIndex = 0;
        asc = false;
        dsc = true;
    }

    // Inner Classes for Global Scope Creation

class RecentPanel{
    private JFrame frame;
    private JTextArea displayResultsTextArea; // Declare the JTextArea
    private JPanel secondPanel;
    private JTextField textField2;
    private JButton prevButton;
    private JButton nextButton;
    private JButton playButton;
    int selected;
    ArrayList<AudioTrack> recentlyPlayed = new ArrayList<>();
    
    void load()
    {
        File file = new File("Users History\\"+user.username+"_history.txt");
        try{
        int count = 0;
        Scanner sc = new Scanner(file);
        
        while(sc.hasNextLine() && count<5)
        {
            boolean flag = false;
            String song = sc.nextLine();
            String[] fields = song.split(";");
            for(AudioTrack adtk: recentlyPlayed)
            {
                if(adtk.getTitle().equals(fields[0]) && adtk.getPath().equals(fields[1]))
                {
                    flag = true;
                }
            }
            if(!flag)
            {
                ++count;
                recentlyPlayed.add(new AudioTrack(fields[0],fields[1]));
            }
        }
        
        sc.close();
        display();
       
        }catch (Exception e)
        {
            System.out.println("Error Loading Favorites");
        }
    }
    
    void display()
    {
        displayResultsTextArea.setText("");
        for(AudioTrack adtk: recentlyPlayed)
        {
            displayResultsTextArea.setText(displayResultsTextArea.getText()+"\n"+adtk.getTitle());
        }
       
    }
   
    void prev()
    {
      
            if(selected>0)
            {
                selected--;
            }
        
        textField2.setText(recentlyPlayed.get(selected).getTitle());
    }
    
    void next()
    {
        if(selected < recentlyPlayed.size()-1)
        {
                selected++;
        }
        textField2.setText(recentlyPlayed.get(selected).getTitle());
    }
    
    void add()
    {
        addedSongs.add(recentlyPlayed.get(selected));
        AllSongsName.setText(getSongsNamesForDisplay());
    }
    
    public RecentPanel()
    {
        load();
        if(recentlyPlayed.size()==0)
        {
            JOptionPane.showMessageDialog(null, "No Recented Found ");
        }
        
        else{
        createAndShowGUI();
        selected = 0;
        textField2.setText(recentlyPlayed.get(selected).getTitle());
        }
        
    }

    private void createAndShowGUI() {
        frame = new JFrame("Remove Song Panel Example");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel mainPanel = new JPanel(new BorderLayout());
        // Second JTextField and Buttons
        secondPanel = new JPanel(new FlowLayout());
        textField2 = new JTextField(15);
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        playButton = new JButton("Add");

        // ActionListener for the Prev button
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Prev button action
                prev();
            }
        });

        // ActionListener for the Next button
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });

        // ActionListener for the Play button
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                add();
            }
        });

        secondPanel.add(textField2);
        secondPanel.add(prevButton);
        secondPanel.add(nextButton);
        secondPanel.add(playButton);

        mainPanel.add(secondPanel, BorderLayout.CENTER);

        // Text Area for Displaying Search Results (100x100 JTextArea)
        displayResultsTextArea = new JTextArea(5, 20); // 5 rows, 20 columns
        displayResultsTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(displayResultsTextArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.pack(); // Adjusts the frame size based on the preferred sizes of its components
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        display();
    }
}      
    
class RemoveSongPanel{
    private JFrame frame;
    private JTextArea displayResultsTextArea; // Declare the JTextArea
    private JPanel secondPanel;
    private JTextField textField2;
    private JButton prevButton;
    private JButton nextButton;
    private JButton playButton;
    int selected;
      
    
    void display()
    {
       displayResultsTextArea.setText("");
        for(AudioTrack adtk: addedSongs)
        {
            displayResultsTextArea.setText(displayResultsTextArea.getText()+"\n"+adtk.getTitle());
        }
       
    }
   
    void prev()
    {
      
            if(selected>0)
            {
                selected--;
            }
        
        textField2.setText(addedSongs.get(selected).getTitle());
    }
    
    void next()
    {
        if(selected < addedSongs.size()-1)
        {
                selected++;
        }
         
        textField2.setText(addedSongs.get(selected).getTitle());
    }
    
    void remove()
    {
      if(addedSongs.size() == 1)
      {
          nowPlaying.setText("Bas Bajna Chaiye Gaana, ");
          if(user!=null)
          {
              nowPlaying.setText("Bas Bajna Chaiye Gaana, "+user.fname + " " + user.lname);
          }
      }
      if(currentTrackIndex == selected)
      {
          stopMusic();
          musicThread =  null;
          addedSongs.remove(selected);
          AllSongsName.setText(getSongsNamesForDisplay());
          playMusic();
          frame.dispose();
          return;
      }
      addedSongs.remove(selected);
      AllSongsName.setText(getSongsNamesForDisplay());
      frame.dispose();
   
    }
    
    public RemoveSongPanel()
    {
        createAndShowGUI();
        selected = 0;
        textField2.setText(addedSongs.get(selected).getTitle());
        
    }

    private void createAndShowGUI() {
        frame = new JFrame("Remove Song Panel Example");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel mainPanel = new JPanel(new BorderLayout());
        // Second JTextField and Buttons
        secondPanel = new JPanel(new FlowLayout());
        textField2 = new JTextField(15);
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        playButton = new JButton("Remove");

        // ActionListener for the Prev button
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Prev button action
                prev();
            }
        });

        // ActionListener for the Next button
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });

        // ActionListener for the Play button
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        });

        secondPanel.add(textField2);
        secondPanel.add(prevButton);
        secondPanel.add(nextButton);
        secondPanel.add(playButton);

        mainPanel.add(secondPanel, BorderLayout.CENTER);

        // Text Area for Displaying Search Results (100x100 JTextArea)
        displayResultsTextArea = new JTextArea(5, 20); // 5 rows, 20 columns
        displayResultsTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(displayResultsTextArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.pack(); // Adjusts the frame size based on the preferred sizes of its components
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        display();
    }
}   
class SearchPanel{
    private JFrame frame;
    private JTextArea displayResultsTextArea; // Declare the JTextArea
    private JPanel searchPanel;
    private JTextField searchBar;
    private JButton searchButton;
    static ArrayList<AudioTrack> addedSongs1;
    static ArrayList<AudioTrack> founds;
    private JPanel secondPanel;
    private JTextField textField2;
    private JButton prevButton;
    private JButton nextButton;
    private JButton playButton;
    int selected;
    
    void play()
    {
        if(selected==-1)
        {
            
        }
        else
        {
            selectedSearched = founds.get(selected);
            System.out.println(selected);
            playSelectedMainThread();
        }
        
        frame.dispose();
     
    }
    
    void display()
    {
       displayResultsTextArea.setText("");
        for(AudioTrack adtk: founds)
        {
            displayResultsTextArea.setText(displayResultsTextArea.getText()+"\n"+adtk.getTitle());
        }
       
    }

    void binarySearch(ArrayList<AudioTrack> songs, String target, int start , int end){
        if(start > end)
        {
            return;
        }
        else
        {
            int mid = (start+end)/2;
            int cmp = target.toLowerCase().compareTo(songs.get(mid).getTitle().toLowerCase());
            if(cmp==0 || songs.get(mid).getTitle().toLowerCase().startsWith(target.toLowerCase()))
            {
                binarySearch(songs,target,start,mid-1);
                founds.add(songs.get(mid));
                binarySearch(songs,target,mid+1,end);
               
            }
            else if(cmp < 0)
            {
                binarySearch(songs,target,start,mid-1);
            }
            else
            {
                binarySearch(songs,target,mid+1,end);
            }
               
        }
    }
   
    void prev()
    {
        if(founds==null || founds.size()==0)
        {
            return;
        }
        else
        {
            if(selected>0)
            {
                selected--;
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Nothing previous");
            }
        }
        textField2.setText(founds.get(selected).getTitle());
    }
    
    void next()
    {
        if(founds==null || founds.size()==0)
        {
            selected = 0;
        }
        else
        {
            if(selected < founds.size()-1)
            {
                selected++;
            }
            else
            {
                JOptionPane.showMessageDialog(null, "No next audiotrack");
            }
        }
        textField2.setText(founds.get(selected).getTitle());
    }
    
    void search(String key)
    {
        selected = -1;
        if(key=="")
        {
            return;
        }
        else
        {   
            founds = null;
            founds = new ArrayList<>();
            binarySearch(addedSongs1,key,0,addedSongs1.size()-1);
            display();
            
            if(founds.size()>0 && founds!=null)
            {
                selected = 0;
                textField2.setText(founds.get(selected).getTitle());
            }
        }
    }
    
    public SearchPanel()
    {
        addedSongs1 = null;
        addedSongs1 = new ArrayList<>();
        addedSongs1.addAll(addedSongs);
        algo.mergeSort(addedSongs1, 0, addedSongs.size()-1);
        createAndShowGUI();
        selected = -1;
        //this.setVisible(false);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Search Panel Example");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Search Panel
        searchPanel = new JPanel(new FlowLayout());
        searchBar = new JTextField(20);
        searchButton = new JButton("Search");

        // ActionListener for the Search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchBar.getText();
                selected = -1;
                search(searchText);
            }
        });

        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Second JTextField and Buttons
        secondPanel = new JPanel(new FlowLayout());
        textField2 = new JTextField(15);
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        playButton = new JButton("Play");

        // ActionListener for the Prev button
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Prev button action
                prev();
            }
        });

        // ActionListener for the Next button
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                next();
            }
        });

        // ActionListener for the Play button
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                play();
            }
        });

        secondPanel.add(textField2);
        secondPanel.add(prevButton);
        secondPanel.add(nextButton);
        secondPanel.add(playButton);

        mainPanel.add(secondPanel, BorderLayout.CENTER);

        // Text Area for Displaying Search Results (100x100 JTextArea)
        displayResultsTextArea = new JTextArea(5, 20); // 5 rows, 20 columns
        displayResultsTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(displayResultsTextArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        frame.getContentPane().add(mainPanel);
        frame.pack(); // Adjusts the frame size based on the preferred sizes of its components
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDesktopPane1 = new javax.swing.JDesktopPane();
        jTextField1 = new javax.swing.JTextField();
        jDesktopPane2 = new javax.swing.JDesktopPane();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        my_logo = new javax.swing.JLabel();
        playbtn = new javax.swing.JLabel();
        volumeUp = new javax.swing.JLabel();
        LoopButton = new javax.swing.JLabel();
        volumeDown = new javax.swing.JLabel();
        muteButton = new javax.swing.JLabel();
        playPreviousBtn = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        addTrkBtn = new javax.swing.JPanel();
        addTrackLabel = new javax.swing.JLabel();
        shuffleBtn = new javax.swing.JPanel();
        shuffleLab = new javax.swing.JLabel();
        repeatBtn = new javax.swing.JPanel();
        repeatLab = new javax.swing.JLabel();
        remTrack = new javax.swing.JPanel();
        shuffleLab1 = new javax.swing.JLabel();
        repeatBtn1 = new javax.swing.JPanel();
        repeatLab1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        playNext = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        nowPlaying = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        dsj = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        AllSongsName = new javax.swing.JTextArea();
        addFavBtn = new javax.swing.JLabel();
        mute_state = new javax.swing.JLabel();
        sortButton = new javax.swing.JButton();
        searchBtn = new javax.swing.JButton();

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jDesktopPane1Layout.setVerticalGroup(
            jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout jDesktopPane2Layout = new javax.swing.GroupLayout(jDesktopPane2);
        jDesktopPane2.setLayout(jDesktopPane2Layout);
        jDesktopPane2Layout.setHorizontalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jDesktopPane2Layout.setVerticalGroup(
            jDesktopPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jToggleButton1.setText("jToggleButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));

        my_logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/Logo4.png"))); // NOI18N

        playbtn.setBackground(new java.awt.Color(255, 255, 255));
        playbtn.setForeground(new java.awt.Color(255, 255, 255));
        playbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/play.png"))); // NOI18N
        playbtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playbtnMouseClicked(evt);
            }
        });

        volumeUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/volume_up.png"))); // NOI18N
        volumeUp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                volumeUpMouseClicked(evt);
            }
        });

        LoopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/repeat.png"))); // NOI18N
        LoopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LoopButtonMouseClicked(evt);
            }
        });

        volumeDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/volume_down.png"))); // NOI18N
        volumeDown.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                volumeDownMouseClicked(evt);
            }
        });

        muteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/mute.png"))); // NOI18N
        muteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                muteButtonMouseClicked(evt);
            }
        });

        playPreviousBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/previous_x.png"))); // NOI18N
        playPreviousBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playPreviousBtnMouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        addTrkBtn.setBackground(new java.awt.Color(4, 59, 83));
        addTrkBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addTrkBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addTrkBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addTrkBtnMouseExited(evt);
            }
        });

        addTrackLabel.setForeground(new java.awt.Color(255, 255, 255));
        addTrackLabel.setText("Add Track");

        javax.swing.GroupLayout addTrkBtnLayout = new javax.swing.GroupLayout(addTrkBtn);
        addTrkBtn.setLayout(addTrkBtnLayout);
        addTrkBtnLayout.setHorizontalGroup(
            addTrkBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addTrkBtnLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(addTrackLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        addTrkBtnLayout.setVerticalGroup(
            addTrkBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addTrkBtnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addTrackLabel)
                .addContainerGap())
        );

        shuffleBtn.setBackground(new java.awt.Color(4, 59, 83));
        shuffleBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shuffleBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                shuffleBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                shuffleBtnMouseExited(evt);
            }
        });

        shuffleLab.setForeground(new java.awt.Color(255, 255, 255));
        shuffleLab.setText("Shuffle");

        javax.swing.GroupLayout shuffleBtnLayout = new javax.swing.GroupLayout(shuffleBtn);
        shuffleBtn.setLayout(shuffleBtnLayout);
        shuffleBtnLayout.setHorizontalGroup(
            shuffleBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shuffleBtnLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shuffleLab)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        shuffleBtnLayout.setVerticalGroup(
            shuffleBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shuffleBtnLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shuffleLab)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        repeatBtn.setBackground(new java.awt.Color(4, 59, 83));
        repeatBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repeatBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                repeatBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                repeatBtnMouseExited(evt);
            }
        });

        repeatLab.setForeground(new java.awt.Color(255, 255, 255));
        repeatLab.setText("Play Favorites");

        javax.swing.GroupLayout repeatBtnLayout = new javax.swing.GroupLayout(repeatBtn);
        repeatBtn.setLayout(repeatBtnLayout);
        repeatBtnLayout.setHorizontalGroup(
            repeatBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repeatBtnLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(repeatLab)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        repeatBtnLayout.setVerticalGroup(
            repeatBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repeatBtnLayout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(repeatLab)
                .addContainerGap())
        );

        remTrack.setBackground(new java.awt.Color(4, 59, 83));
        remTrack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                remTrackMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                remTrackMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                remTrackMouseExited(evt);
            }
        });

        shuffleLab1.setForeground(new java.awt.Color(255, 255, 255));
        shuffleLab1.setText("Remove Track");

        javax.swing.GroupLayout remTrackLayout = new javax.swing.GroupLayout(remTrack);
        remTrack.setLayout(remTrackLayout);
        remTrackLayout.setHorizontalGroup(
            remTrackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(remTrackLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(shuffleLab1)
                .addContainerGap(117, Short.MAX_VALUE))
        );
        remTrackLayout.setVerticalGroup(
            remTrackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(remTrackLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shuffleLab1)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        repeatBtn1.setBackground(new java.awt.Color(4, 59, 83));
        repeatBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repeatBtn1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                repeatBtn1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                repeatBtn1MouseExited(evt);
            }
        });

        repeatLab1.setForeground(new java.awt.Color(255, 255, 255));
        repeatLab1.setText("Recently Played");

        javax.swing.GroupLayout repeatBtn1Layout = new javax.swing.GroupLayout(repeatBtn1);
        repeatBtn1.setLayout(repeatBtn1Layout);
        repeatBtn1Layout.setHorizontalGroup(
            repeatBtn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repeatBtn1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(repeatLab1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        repeatBtn1Layout.setVerticalGroup(
            repeatBtn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repeatBtn1Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(repeatLab1)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(4, 59, 83));
        jPanel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel4MouseClicked(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Log Out");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(repeatBtn1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addTrkBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(repeatBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shuffleBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(remTrack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addTrkBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(remTrack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shuffleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(repeatBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 223, Short.MAX_VALUE)
                .addComponent(repeatBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        playNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/next.png"))); // NOI18N
        playNext.setAlignmentX(5.0F);
        playNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playNextMouseClicked(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        nowPlaying.setBackground(new java.awt.Color(255, 255, 255));
        nowPlaying.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        nowPlaying.setForeground(new java.awt.Color(255, 255, 255));
        nowPlaying.setText("We Hit Different!!!");

        jLabel2.setText("line");

        dsj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/Line2.png"))); // NOI18N
        dsj.setText("jLabel3");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(nowPlaying, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dsj)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(nowPlaying)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dsj)
                        .addContainerGap())))
        );

        AllSongsName.setBackground(new java.awt.Color(0, 0, 0));
        AllSongsName.setColumns(20);
        AllSongsName.setForeground(new java.awt.Color(255, 255, 255));
        AllSongsName.setRows(5);
        jScrollPane1.setViewportView(AllSongsName);

        addFavBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mediaplayer/add_to_fav.png"))); // NOI18N
        addFavBtn.setText("Add to fav");
        addFavBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addFavBtnMouseClicked(evt);
            }
        });

        mute_state.setForeground(new java.awt.Color(255, 255, 255));
        mute_state.setText("Enjoy the Music.....");

        sortButton.setText("A-Z Play");
        sortButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sortButtonMouseClicked(evt);
            }
        });

        searchBtn.setText("Search Song");
        searchBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchBtnMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(my_logo))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addFavBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(LoopButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(playPreviousBtn)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(playbtn)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(playNext)
                                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                                        .addGap(76, 76, 76)
                                                        .addComponent(volumeDown)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(volumeUp)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(muteButton))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                                        .addComponent(searchBtn)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(sortButton))))
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 544, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 38, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(mute_state)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(my_logo, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(mute_state)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFavBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(playbtn, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sortButton)
                            .addComponent(searchBtn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(volumeUp)
                                    .addComponent(volumeDown)
                                    .addComponent(muteButton))
                                .addGap(14, 14, 14))
                            .addComponent(playNext, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LoopButton)
                            .addComponent(playPreviousBtn))
                        .addGap(23, 23, 23)))
                .addGap(13, 13, 13))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addTrkBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrkBtnMouseEntered
        addTrkBtn.setBackground(Color.WHITE);
        addTrackLabel.setForeground(Color.BLACK);
    }//GEN-LAST:event_addTrkBtnMouseEntered

    private void addTrkBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrkBtnMouseExited
        addTrkBtn.setBackground(Color.decode("#043B53"));
        addTrackLabel.setForeground(Color.WHITE);
    }//GEN-LAST:event_addTrkBtnMouseExited

    private void shuffleBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_shuffleBtnMouseEntered
        shuffleBtn.setBackground(Color.WHITE);
        shuffleLab.setForeground(Color.BLACK);
    }//GEN-LAST:event_shuffleBtnMouseEntered

    private void shuffleBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_shuffleBtnMouseExited
        shuffleBtn.setBackground(Color.decode("#043B53"));
        shuffleLab.setForeground(Color.WHITE);
    }//GEN-LAST:event_shuffleBtnMouseExited

    private void repeatBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtnMouseEntered
        repeatBtn.setBackground(Color.WHITE);
        repeatLab.setForeground(Color.BLACK);
    }//GEN-LAST:event_repeatBtnMouseEntered

    private void repeatBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtnMouseExited
        repeatBtn.setBackground(Color.decode("#043B53"));
        repeatLab.setForeground(Color.WHITE);
    }//GEN-LAST:event_repeatBtnMouseExited

    private void addTrkBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addTrkBtnMouseClicked
        addTracks();
    }//GEN-LAST:event_addTrkBtnMouseClicked

    private void playbtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playbtnMouseClicked

        if (addedSongs.size() == 0) {
            JOptionPane.showMessageDialog(null, "No Playsits found", "Sorry: ", HEIGHT);
            return;
        }

        if (!isPlaying) {
            ImageIcon icon = new ImageIcon("E:\\Java Projects\\Test Project\\MediaPlayer\\src\\mediaplayer\\play_enabled.png");
            playbtn.setIcon(icon);
            playMusic();
        } else {
            ImageIcon icon = new ImageIcon("E:\\Java Projects\\Test Project\\MediaPlayer\\src\\mediaplayer\\play.png");
            playbtn.setIcon(icon);
            positionPtr = clip.getMicrosecondLength();
            musicThread = null;
            stopMusic();
        }


    }//GEN-LAST:event_playbtnMouseClicked

    private void playNextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playNextMouseClicked
        musicThread = null;
        positionPtr = 0;
        System.out.println(positionPtr);
        playNext();
    }//GEN-LAST:event_playNextMouseClicked

    private void LoopButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LoopButtonMouseClicked
        toLoop = !toLoop;
        String path;
        if (!toLoop) {
            path = "E:\\Java Projects\\Test Project\\MediaPlayer\\src\\mediaplayer\\repeat.png";
        } else {
            path = "E:\\Java Projects\\Test Project\\MediaPlayer\\src\\mediaplayer\\repeat_enabled.png";
        }
        ImageIcon icon = new ImageIcon(path);
        LoopButton.setIcon(icon);
    }//GEN-LAST:event_LoopButtonMouseClicked

    private void playPreviousBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playPreviousBtnMouseClicked
        playPrevious();
    }//GEN-LAST:event_playPreviousBtnMouseClicked

    private void shuffleBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_shuffleBtnMouseClicked
        shuffle();
    }//GEN-LAST:event_shuffleBtnMouseClicked

    private void addFavBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addFavBtnMouseClicked
        addToFav();
    }//GEN-LAST:event_addFavBtnMouseClicked

    private void repeatBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtnMouseClicked
        playFavorites();
    }//GEN-LAST:event_repeatBtnMouseClicked

    private void muteButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_muteButtonMouseClicked
        mute();
    }//GEN-LAST:event_muteButtonMouseClicked

    private void volumeUpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_volumeUpMouseClicked
        volumeUp();
    }//GEN-LAST:event_volumeUpMouseClicked

    private void volumeDownMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_volumeDownMouseClicked
        volumeDown();
    }//GEN-LAST:event_volumeDownMouseClicked

    boolean state_sort = false;
    private void sortButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sortButtonMouseClicked

        state_sort = !state_sort;
        if (state_sort == true) {
            sortButton.setText("Z-A Play");
            sortAscending();
        } else {
            sortButton.setText("A-Z Play");
            sortDescending();
        }

    }//GEN-LAST:event_sortButtonMouseClicked

    private void remTrackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remTrackMouseClicked
         if (addedSongs.size() == 0) {
            JOptionPane.showMessageDialog(null, "Cant Search....");
            return;
        }
        RemoveSongPanel rsp;
        rsp = new RemoveSongPanel();
    }//GEN-LAST:event_remTrackMouseClicked

    private void remTrackMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remTrackMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_remTrackMouseEntered

    private void remTrackMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remTrackMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_remTrackMouseExited

    private void searchBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBtnMouseClicked

        if (addedSongs.size() == 0) {
            JOptionPane.showMessageDialog(null, "Cant Search....");
            return;
        }

        //activate_deactivate();
        SearchPanel sp = new SearchPanel();
        

    }//GEN-LAST:event_searchBtnMouseClicked

    private void repeatBtn1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtn1MouseClicked
       RecentPanel rp = new RecentPanel();
    }//GEN-LAST:event_repeatBtn1MouseClicked

    private void repeatBtn1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtn1MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_repeatBtn1MouseEntered

    private void repeatBtn1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repeatBtn1MouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_repeatBtn1MouseExited

    private void jPanel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseClicked
        stopMusic();
        musicThread = null;
        
        
        
        if(user == null || addedSongs.size()==0)
        {
            
            this.dispose();
        }
        
        else{
            if(user!=null)
            mantainHistory();
            this.dispose();
        }
        
        JOptionPane.showMessageDialog(null, "Logged Out Successfully");
    }//GEN-LAST:event_jPanel4MouseClicked

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MediaPlayerApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MediaPlayerApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MediaPlayerApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MediaPlayerApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MediaPlayerApplication(null).setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea AllSongsName;
    private javax.swing.JLabel LoopButton;
    private javax.swing.JLabel addFavBtn;
    private javax.swing.JLabel addTrackLabel;
    private javax.swing.JPanel addTrkBtn;
    private javax.swing.JLabel dsj;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JDesktopPane jDesktopPane2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel muteButton;
    private javax.swing.JLabel mute_state;
    private javax.swing.JLabel my_logo;
    private javax.swing.JLabel nowPlaying;
    private javax.swing.JLabel playNext;
    private javax.swing.JLabel playPreviousBtn;
    private javax.swing.JLabel playbtn;
    private javax.swing.JPanel remTrack;
    private javax.swing.JPanel repeatBtn;
    private javax.swing.JPanel repeatBtn1;
    private javax.swing.JLabel repeatLab;
    private javax.swing.JLabel repeatLab1;
    private javax.swing.JButton searchBtn;
    private javax.swing.JPanel shuffleBtn;
    private javax.swing.JLabel shuffleLab;
    private javax.swing.JLabel shuffleLab1;
    private javax.swing.JButton sortButton;
    private javax.swing.JLabel volumeDown;
    private javax.swing.JLabel volumeUp;
    // End of variables declaration//GEN-END:variables
}
