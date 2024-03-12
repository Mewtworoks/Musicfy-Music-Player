import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Musicfy extends JFrame implements ActionListener {
    // Buttons for player controls
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton nextButton;
    private JButton previousButton;
    private JButton chooseFolderButton; // Renamed from "playlistButton"
    private JButton shuffleButton;

    // Sliders for progress and volume control
    private JSlider progressSlider;
    private JSlider volumeSlider;

    // Labels for track information and volume display
    private JLabel trackLabel;
    private JLabel timeLabel;
    private JLabel volumeLabel;

    // Playlist components
    private JList<String> playlist;
    private DefaultListModel<String> playlistModel;

    // Variables for storing music files and track index
    private File[] musicFiles;
    private int currentTrackIndex;

    // Audio clip for playing music
    private Clip clip;

    public Musicfy() {
        setTitle("Musicfy");
        setSize(530, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set custom icon
        ImageIcon icon = new ImageIcon("Musicfy.png");  // Replace with the path to your icon image
        setIconImage(icon.getImage());

        ImageIcon playIcon = new ImageIcon("play.png"); // Replace "play.png" with the path to your play button icon image
        ImageIcon pauseIcon = new ImageIcon("pause.png"); // Replace "pause.png" with the path to your pause button icon image
        ImageIcon stopIcon = new ImageIcon("stop.png"); // Replace "stop.png" with the path to your stop button icon image
        ImageIcon nextIcon = new ImageIcon("next.png"); // Replace "next.png" with the path to your next button icon image
        ImageIcon previousIcon = new ImageIcon("previous.png"); // Replace "previous.png" with the path to your previous button icon image
        ImageIcon playlistIcon = new ImageIcon("playlist.png"); // Replace "playlist.png" with the path to your playlist button icon image
        ImageIcon shuffleIcon = new ImageIcon("shuffle.png"); // Replace "shuffle.png" with the path to your shuffle button icon image
        
        // Resize icon images
        playIcon = resizeIcon(playIcon, 15, 15); // Adjust the size (32x32) as needed
        pauseIcon = resizeIcon(pauseIcon, 15, 15);
        stopIcon = resizeIcon(stopIcon, 15, 15);
        nextIcon = resizeIcon(nextIcon, 15, 15);
        previousIcon = resizeIcon(previousIcon, 15, 15);
        playlistIcon = resizeIcon(playlistIcon, 15, 15);
        shuffleIcon = resizeIcon(shuffleIcon, 15, 15);

        // Create buttons with icons
        playButton = new JButton("Play", playIcon);
        pauseButton = new JButton("Pause", pauseIcon);
        stopButton = new JButton("Stop", stopIcon);
        nextButton = new JButton("Next", nextIcon);
        previousButton = new JButton("Previous", previousIcon);
        chooseFolderButton = new JButton("Playlist", playlistIcon);
        shuffleButton = new JButton("Shuffle", shuffleIcon);

        // Create buttons
        playButton = new JButton("", playIcon);
        pauseButton = new JButton("", pauseIcon);
        stopButton = new JButton("", stopIcon);
        nextButton = new JButton("", nextIcon);
        previousButton = new JButton("", previousIcon);
        chooseFolderButton = new JButton("", playlistIcon);
        shuffleButton = new JButton("", shuffleIcon);

        // Create sliders
        progressSlider = new JSlider(0, 100);
        progressSlider.setPreferredSize(new Dimension(50, 10));
        volumeSlider = new JSlider(0, 100);
        volumeSlider.setPreferredSize(new Dimension(50, 10));

        // Create labels
        trackLabel = new JLabel("No Track Selected");
        timeLabel = new JLabel("00:00 / 00:00");
        volumeLabel = new JLabel("Volume: 50%");

        // Create playlist components
        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel);
        playlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlist.setFixedCellHeight(15);

        // Add action listeners to buttons
        playButton.addActionListener(this);
        pauseButton.addActionListener(this);
        stopButton.addActionListener(this);
        nextButton.addActionListener(this);
        previousButton.addActionListener(this);
        chooseFolderButton.addActionListener(e -> {
            // Create a file chooser dialog to select a folder for the playlist
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = folderChooser.showOpenDialog(Musicfy.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // Get the selected directory path and load music files
                String directoryPath = folderChooser.getSelectedFile().getAbsolutePath();
                loadMusicFiles(directoryPath);
            }
        });
        shuffleButton.addActionListener(this);

        // Create button panel and add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(previousButton);
        buttonPanel.add(shuffleButton); // Add the Shuffle button
        buttonPanel.add(nextButton);
        buttonPanel.add(chooseFolderButton);

        // Create playlist scroll pane
        JScrollPane playlistScrollPane = new JScrollPane(playlist);
        playlistScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        playlistScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Create slider panel and add sliders
        JPanel sliderPanel = new JPanel(new GridLayout(3, 1));
        sliderPanel.add(trackLabel);
        sliderPanel.add(timeLabel);
        sliderPanel.add(progressSlider);

        // Create label panel and add labels
        JPanel labelPanel = new JPanel(new GridLayout(3, 1));
        labelPanel.add(volumeLabel);
        labelPanel.add(volumeSlider);

        // Create center and east panels
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel eastPanel = new JPanel(new BorderLayout());

        // Add components to center and east panels
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        centerPanel.add(sliderPanel, BorderLayout.CENTER);
        eastPanel.add(labelPanel, BorderLayout.EAST);
        eastPanel.add(playlistScrollPane, BorderLayout.CENTER);

        // Add center and east panels to main frame
        add(centerPanel, BorderLayout.CENTER);
        add(eastPanel, BorderLayout.SOUTH);

        // Add change listener to volume slider
        volumeSlider.addChangeListener(e -> {
            // Update volume label and set clip volume
            int volume = volumeSlider.getValue();
            volumeLabel.setText("Volume: " + volume + "%");
            setClipVolume(volume);
        });

        // Add change listener to progress slider
        progressSlider.addChangeListener(e -> {
            // Set clip position based on the progress slider value
            if (progressSlider.getValueIsAdjusting() && clip != null) {
                clip.setMicrosecondPosition(progressSlider.getValue() * 1000);
            }
        });
    }

    // Method to load music files from a directory
    private void loadMusicFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            // Get the music files in the directory
            musicFiles = directory.listFiles();
            playlistModel.clear(); // Clear the playlist
            for (File file : musicFiles) { // Add songs to the playlist
                playlistModel.addElement(file.getName());
            }
            currentTrackIndex = 0;
            trackLabel.setText(musicFiles[currentTrackIndex].getName());
        } else {
            JOptionPane.showMessageDialog(this, "Invalid directory path", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to set the volume of the audio clip
    private void setClipVolume(int volume) {
        if (clip != null) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume / 100.0) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    // Method to play the current track
    private void playCurrentTrack() {
        try {
            if (clip != null && clip.isOpen()) {
                clip.stop();
                clip.close();
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFiles[currentTrackIndex]);
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this, "Unsupported audio format", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);

            int trackDuration = (int) (clip.getMicrosecondLength() / 1000);
            progressSlider.setMaximum(trackDuration);
            progressSlider.setValue(0);

            Timer timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!progressSlider.getValueIsAdjusting()) {
                        int currentPosition = (int) (clip.getMicrosecondPosition() / 1000);
                        progressSlider.setValue(currentPosition);

                        int currentSeconds = currentPosition / 1000;
                        int totalSeconds = trackDuration / 1000;

                        int currentMinutes = currentSeconds / 60;
                        int currentRemSeconds = currentSeconds % 60;

                        int totalMinutes = totalSeconds / 60;
                        int totalRemSeconds = totalSeconds % 60;

                        timeLabel.setText(String.format("%02d:%02d / %02d:%02d", currentMinutes, currentRemSeconds, totalMinutes, totalRemSeconds));

                        if (currentPosition >= trackDuration) {
                            clip.stop();
                            clip.close();
                            playNextTrack();
                        }
                    }
                }
            });
            timer.start();

            setClipVolume(volumeSlider.getValue());
            clip.start();

        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            JOptionPane.showMessageDialog(this, "Only Supports WAV Audio Files, Make Sure File is not Corrupt !", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to play the next track
    private void playNextTrack() {
        currentTrackIndex++;
        if (currentTrackIndex >= musicFiles.length) {
            currentTrackIndex = 0;
        }
        trackLabel.setText(musicFiles[currentTrackIndex].getName());
        playCurrentTrack();
    }

    // ActionListener implementation for button events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            playCurrentTrack();
        } else if (e.getSource() == pauseButton) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        } else if (e.getSource() == stopButton) {
            if (clip != null) {
                clip.stop();
                clip.close();
                progressSlider.setValue(0);
                timeLabel.setText("00:00 / 00:00");
            }
        } else if (e.getSource() == nextButton) {
            playNextTrack();
        } else if (e.getSource() == previousButton) {
            currentTrackIndex--;
            if (currentTrackIndex < 0) {
                currentTrackIndex = musicFiles.length - 1;
            }
            trackLabel.setText(musicFiles[currentTrackIndex].getName());
            playCurrentTrack();
        } else if (e.getSource() == shuffleButton) {
            if (clip != null && clip.isOpen()) {
                clip.stop();
                clip.close();
            }
            int previousTrackIndex = currentTrackIndex;
            currentTrackIndex = (int) (Math.random() * musicFiles.length);
            if (currentTrackIndex == previousTrackIndex) {
                currentTrackIndex = (currentTrackIndex + 1) % musicFiles.length;
            }
            trackLabel.setText(musicFiles[currentTrackIndex].getName());
            playCurrentTrack();
        }
    }

    // Method to resize an ImageIcon
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }
public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set the look and feel of the UI to Windows
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                // Customize UI colors to match VLC theme
                UIManager.put("Slider.background", new ColorUIResource(35, 38, 41));  // Dark gray background
                UIManager.put("Slider.foreground", new ColorUIResource(213, 219, 219)); // Light gray foreground
                UIManager.put("Slider.track", new ColorUIResource(213, 219, 219)); // Light gray track
                UIManager.put("Slider.thumb", new ColorUIResource(255, 179, 0)); // Orange thumb
                UIManager.put("Button.background", new ColorUIResource(35, 38, 41)); // Dark gray background
                UIManager.put("Button.foreground", new ColorUIResource(213, 219, 219)); // Light gray foreground
                UIManager.put("Button.select", new ColorUIResource(255, 179, 0)); // Orange selection

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create and show the media player
            Musicfy musicPlayer = new Musicfy();
            musicPlayer.setVisible(true);

            // Open file chooser to select a folder for the playlist
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(musicPlayer);
            if (result == JFileChooser.APPROVE_OPTION) {
                String directoryPath = fileChooser.getSelectedFile().getAbsolutePath();
                musicPlayer.loadMusicFiles(directoryPath);
            }
        });
    }
}