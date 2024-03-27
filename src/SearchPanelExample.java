import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SearchPanelExample extends JFrame {
    private static JTextArea displayResultsTextArea; // Declare the JTextArea

    public static void main(String[] args) {
       SearchPanelExample spe = new SearchPanelExample();
    }
    public SearchPanelExample()
    {
        this.setVisible(true);
        createAndShowGUI();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Search Panel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchBar = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // ActionListener for the Search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchBar.getText();
                // Perform search or update UI based on the search text
                // Display the results in the text area
                displayResultsTextArea.append("Searching for: " + searchText + "\n");
            }
        });

        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Second JTextField and Buttons
        JPanel secondPanel = new JPanel(new FlowLayout());
        JTextField textField2 = new JTextField(15);
        JButton prevButton = new JButton("Prev");
        JButton nextButton = new JButton("Next");
        JButton playButton = new JButton("Play");

        // ActionListener for the Prev button
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Prev button action
                displayResultsTextArea.append("Prev button clicked\n");
            }
        });

        // ActionListener for the Next button
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Next button action
                displayResultsTextArea.append("Next button clicked\n");
            }
        });

        // ActionListener for the Play button
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle Play button action
                displayResultsTextArea.append("Play button clicked\n");
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
    }
}
