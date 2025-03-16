package com.example.demo;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class DoctorReviewGUI extends JFrame {
	private DoctorRepository doctorRepository;
	private JList<Doctor> doctorList;
	private JTextArea feedbackTextArea;
	private JTextField newRatingField;
	private JTextField searchField;
	private List<Doctor> originalDoctors; 

	public DoctorReviewGUI() {
		super("Doctor Review");
		doctorRepository = new DoctorRepository();
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 800); 
		setLocationRelativeTo(null);
		setVisible(true);
	}

	JPanel doctorCardsPanel;
	JButton backButton;

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());


		JPanel sidebarPanel = new JPanel();
		sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
		sidebarPanel.setBackground(Color.WHITE);


		JLabel logoLabel = new JLabel(new ImageIcon("icon.png"));

		sidebarPanel.add(logoLabel);


		String[] sidebarItems = {"Doctor Review", "Appointment", "Patient Information", "Pharmacy",  "Restaurant", "Finances"};
		for (String item : sidebarItems) {
			JButton button = new JButton(item);
			sidebarPanel.add(button);
		}

		getContentPane().add(sidebarPanel, BorderLayout.WEST);


		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.LIGHT_GRAY);


		doctorCardsPanel = new JPanel(new GridLayout(0, 2, 10, 10)); 
		doctorCardsPanel.setBackground(new Color(102, 205, 170)); 


		originalDoctors = doctorRepository.getAllDoctors();
		for (Doctor doctor : originalDoctors) {
			doctorCardsPanel.add(createDoctorCard(doctor));
		}


		JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchField = new JTextField(20);
		searchField.addActionListener(e -> searchDoctors());
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> searchDoctors());
		searchBarPanel.add(searchField);
		searchBarPanel.add(searchButton);
		mainPanel.add(searchBarPanel, BorderLayout.NORTH);

		JScrollPane doctorCardsScrollPane = new JScrollPane(doctorCardsPanel);
		mainPanel.add(doctorCardsScrollPane, BorderLayout.CENTER);

		// Back Button
		backButton = new JButton("Back");
		backButton.addActionListener(e -> showAllDoctors()); 
		backButton.setEnabled(false); 
		mainPanel.add(backButton, BorderLayout.SOUTH);

		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createDoctorCard(Doctor doctor) {
		JPanel cardPanel = new JPanel(new BorderLayout());
		cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 

		JLabel nameLabel = new JLabel("Name: " + doctor.getName());
		JLabel specialtyLabel = new JLabel("Specialty: " + doctor.getSpecialty());
		JLabel ratingLabel = new JLabel("Average Rating: " + String.format("%.1f", doctor.getAverageRating()));
		JLabel totalReviewsLabel = new JLabel("Total Reviews: " + doctor.getTotalReviews());

		JPanel infoPanel = new JPanel(new GridLayout(0, 1));
		infoPanel.add(nameLabel);
		infoPanel.add(specialtyLabel);
		infoPanel.add(ratingLabel);
		infoPanel.add(totalReviewsLabel);

		cardPanel.add(infoPanel, BorderLayout.CENTER);

		JButton feedbackButton = new JButton("Give Feedback");
		feedbackButton.addActionListener(e -> {

			String ratingInput = JOptionPane.showInputDialog(this, "Enter your rating (1-5):");
			if (ratingInput != null && !ratingInput.isEmpty()) {
				try {
					int rating = Integer.parseInt(ratingInput);
					if (rating < 1 || rating > 5) {
						JOptionPane.showMessageDialog(this, "Rating must be between 1 and 5.", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						String comment = JOptionPane.showInputDialog(this, "Enter your feedback comment:");
						if (comment != null && !comment.isEmpty()) {

							doctor.addFeedback(rating, comment);

							doctorRepository.saveDoctors();

							updateDoctorCardsPanel();
							JOptionPane.showMessageDialog(this, "Feedback Added Successfully");
							System.out.println("Feedback Added Successfully");
						}
					}
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Please enter a valid rating.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		cardPanel.add(feedbackButton, BorderLayout.SOUTH);

		return cardPanel;
	}

	private void updateDoctorCardsPanel() {
		doctorCardsPanel.removeAll();
		List<Doctor> updatedDoctors = doctorRepository.getAllDoctors();
		for (Doctor doctor : updatedDoctors) {
			doctorCardsPanel.add(createDoctorCard(doctor));
		}
		doctorCardsPanel.revalidate();
		doctorCardsPanel.repaint();
	}

	private void searchDoctors() {
		String searchText = searchField.getText().trim().toLowerCase();
		if (!searchText.isEmpty()) {
			List<Doctor> filteredDoctors = doctorRepository.searchDoctors(searchText);
			filteredDoctors.sort(Comparator.comparingDouble(Doctor::getAverageRating).reversed());
			doctorCardsPanel.removeAll();
			for (Doctor doctor : filteredDoctors) {
				doctorCardsPanel.add(createDoctorCard(doctor));
			}
			backButton.setEnabled(true);
			doctorCardsPanel.revalidate();
			doctorCardsPanel.repaint();
		}
	}

	private void showAllDoctors() {
		doctorCardsPanel.removeAll();
		for (Doctor doctor : originalDoctors) {
			doctorCardsPanel.add(createDoctorCard(doctor));
		}
		backButton.setEnabled(false);
		doctorCardsPanel.revalidate();
		doctorCardsPanel.repaint();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> new DoctorReviewGUI());
	}
}

class DoctorRepository {
	private List<Doctor> doctors;

	public DoctorRepository() {
		this.doctors = new ArrayList<>();
		loadDoctors();
	}



	private void loadDoctors() {
		try (Scanner scanner = new Scanner(new File("doctors.csv"))) {
			scanner.nextLine(); 
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] tokens = line.split(",");


				if(tokens.length >= 4) {
					String name = tokens[0].trim();
					String specialty = tokens[1].trim();
					double averageRating = Double.parseDouble(tokens[2].trim());
					int totalReviews = Integer.parseInt(tokens[3].trim());
					Doctor doctor = new Doctor(name, specialty, averageRating, totalReviews);
					doctors.add(doctor);
				} else {

					System.err.println("Invalid line format: " + line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<Doctor> getAllDoctors() {
		return doctors;
	}

	public void saveDoctors() {
		try (PrintWriter writer = new PrintWriter(new FileWriter("doctors.csv"))) {
			writer.println("Name,Specialization,Review Rating,Total Reviews");
			for (Doctor doctor : doctors) {
				writer.println(doctor.getName() + "," + doctor.getSpecialty() + "," + String.format("%.1f", doctor.getAverageRating()) + "," + doctor.getTotalReviews());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Doctor> searchDoctors(String searchText) {
		List<Doctor> filteredDoctors = new ArrayList<>();
		for (Doctor doctor : doctors) {
			if (doctor.getName().equalsIgnoreCase(searchText) || doctor.getSpecialty().equalsIgnoreCase(searchText)) {
				filteredDoctors.add(doctor);
			}
		}
		return filteredDoctors;
	}
}

class Doctor {
	private String name;
	private String specialty;
	private double averageRating;
	private int totalReviews;
	private List<String> feedback;

	public Doctor(String name, String specialty, double averageRating, int totalReviews) {
		this.name = name;
		this.specialty = specialty;
		this.averageRating = averageRating;
		this.totalReviews = totalReviews;
		this.feedback = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public String getSpecialty() {
		return specialty;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public int getTotalReviews() {
		return totalReviews;
	}

	public List<String> getFeedback() {
		return feedback;
	}

	public void addFeedback(int rating, String comment) {
		feedback.add(comment);
		double newAverageRating = ((averageRating * totalReviews) + rating) / (totalReviews + 1.0);
		totalReviews++;
		averageRating = newAverageRating;
	}

	@Override
	public String toString() {
		return name + " (" + specialty + ") - Rating: " + String.format("%.1f", averageRating);
	}
}
