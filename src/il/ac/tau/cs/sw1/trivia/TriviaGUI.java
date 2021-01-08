package il.ac.tau.cs.sw1.trivia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TriviaGUI {

	private static final int MAX_ERRORS = 3;
	private Shell shell;
	private Label scoreLabel;
	private Composite questionPanel;
	private Label startupMessageLabel;
	private Font boldFont;
	private String lastAnswer;
	private int questionsAsked;
	private int questionsAnswered;
	private List<Question> qArr = new ArrayList<Question>();
	private int wrongAnswers;
	private Question curQuestion;
	private int curScores = 0;
	private boolean isAnswersAvaliable = true;
	private boolean isPassAvaliable = true;
	
	private boolean isFiftyFiftyAvaliable = true;
	
	// Currently visible UI elements.
	Label instructionLabel;
	Label questionLabel;
	private List<Button> answerButtons = new LinkedList<>();
	private Button passButton;
	private Button fiftyFiftyButton;

	public void open() {
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	private void createShell() {
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Trivia");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(new Point(monitor_bounds.width / 3,
				monitor_bounds.height / 4));
		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);

		// create window panels
		createFileLoadingPanel();
		createScorePanel();
		createQuestionPanel();
	}

	/**
	 * Creates the widgets of the form for trivia file selection
	 */
	private void createFileLoadingPanel() {
		final Composite fileSelection = new Composite(shell, SWT.NULL);
		fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
		fileSelection.setLayout(new GridLayout(4, false));

		final Label label = new Label(fileSelection, SWT.NONE);
		label.setText("Enter trivia file path: ");

		// text field to enter the file path
		final Text filePathField = new Text(fileSelection, SWT.SINGLE
				| SWT.BORDER);
		filePathField.setLayoutData(GUIUtils.createFillGridData(1));

		// "Browse" button
		final Button browseButton = new Button(fileSelection, SWT.PUSH);
		browseButton.setText("Browse");
		
		// "Browse" listener
		browseButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		        if (e.type == SWT.Selection) {
		        	filePathField.setText(GUIUtils.getFilePathFromFileDialog(shell));
		        }
		      }
		});

		// "Play!" button
		final Button playButton = new Button(fileSelection, SWT.PUSH);
		playButton.setText("Play!");
	
		// "Play!" listener
		playButton.addListener(SWT.Selection, new Listener() {
		   	public void handleEvent(Event e) {
		        if (e.type == SWT.Selection) {
		        	File file = new File(filePathField.getText());
		        	try {
						FileReader fr = new FileReader(file);
						BufferedReader br = new BufferedReader(fr);
						String line = br.readLine();
						while (line != null) {
							String[] splittedLine = line.split("\t");
							qArr.add(new Question(splittedLine));
							line = br.readLine();
						}
						br.close();
					}
		        	catch (FileNotFoundException e1) { // won't happen
						return;
					}
		        	catch (IOException e1) { // won't happen
						return;
					}
		        	lastAnswer = "";
		        	questionsAsked = 0;
		        	questionsAnswered = 0;
		        	wrongAnswers = 0;
		        	curScores = 0;
		        	scoreLabel.setText("0");
		        	isAnswersAvaliable = true;
		        	isPassAvaliable = true;
		        	isFiftyFiftyAvaliable = true;
		        	updateRandomQuestion("Play");		        	
		        }
		      }
		});
	
	}
	
	private void updateRandomQuestion(String instruction) {
		Random rand = new Random();
    	int qNum = rand.nextInt(qArr.size());
    	while (qArr.get(qNum).wasAsked) {
    		qNum = rand.nextInt(qNum);
    	}
    	curQuestion = qArr.get(qNum);
    	curQuestion.wasAsked = true;
    	updateQuestionPanel(curQuestion.question, curQuestion.shuffledAnswers);
    	questionsAsked++;
    	if (!instruction.equals("Pass"))
    		questionsAnswered++;
	}

	/**
	 * Creates the panel that displays the current score
	 */
	private void createScorePanel() {
		Composite scorePanel = new Composite(shell, SWT.BORDER);
		scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
		scorePanel.setLayout(new GridLayout(2, false));

		final Label label = new Label(scorePanel, SWT.NONE);
		label.setText("Total score: ");

		// The label which displays the score; initially empty
		scoreLabel = new Label(scorePanel, SWT.NONE);
		scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
	}

	/**
	 * Creates the panel that displays the questions, as soon as the game
	 * starts. See the updateQuestionPanel for creating the question and answer
	 * buttons
	 */
	private void createQuestionPanel() {
		questionPanel = new Composite(shell, SWT.BORDER);
		questionPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		questionPanel.setLayout(new GridLayout(2, true));

		// Initially, only displays a message
		startupMessageLabel = new Label(questionPanel, SWT.NONE);
		startupMessageLabel.setText("No question to display, yet.");
		startupMessageLabel.setLayoutData(GUIUtils.createFillGridData(2));
	}

	/**
	 * Serves to display the question and answer buttons
	 */
	private void updateQuestionPanel(String question, List<String> answers) {
		// Save current list of answers.
		List<String> currentAnswers = answers;
		
		// clear the question panel
		Control[] children = questionPanel.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		// create the instruction label
		instructionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		instructionLabel.setText(lastAnswer + "Answer the following question:");
		instructionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the question label
		questionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		questionLabel.setText(question);
		questionLabel.setFont(boldFont);
		questionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the answer buttons
		answerButtons.clear();
		for (int i = 0; i < 4; i++) {
			Button answerButton = new Button(questionPanel, SWT.PUSH | SWT.WRAP);
			answerButton.setText(answers.get(i));
			GridData answerLayoutData = GUIUtils.createFillGridData(1);
			answerLayoutData.verticalAlignment = SWT.FILL;
			answerButton.setLayoutData(answerLayoutData);
			
			answerButtons.add(answerButton);
		}
		
		// answers listener
		if (isAnswersAvaliable) { //###############################################
			for (Button b : answerButtons) {
				b.addListener(SWT.Selection, new Listener() {
				   	public void handleEvent(Event e) {
				        if (e.type == SWT.Selection) {
							String rightAns = curQuestion.answers.get(0);
							if (b.getText().equals(rightAns)) {
								curScores += 3;
								wrongAnswers = 0;
							}
							else {
								curScores -= 2;
								wrongAnswers += 1;
							}
							
							scoreLabel.setText(String.valueOf(curScores));
							if (questionsAsked == qArr.size()) {
								GUIUtils.showInfoDialog(shell, "YOU WON", "Your final score is " + curScores + " after " + questionsAnswered + " questions.");
								isAnswersAvaliable = false;
							}
							else if (wrongAnswers == MAX_ERRORS) {
								GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + curScores + " after " + questionsAnswered + " questions.");
								isAnswersAvaliable = false;
							}
							else
								updateRandomQuestion("Answer");
							}
					   	}
				});
			}
		}

		// create the "Pass" button to skip a question
		passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.END, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		passButton.setLayoutData(data);
		
		// Pass Listener
		passButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		        if (e.type == SWT.Selection) {
		        	updateRandomQuestion("Pass");
		        }
		      }
		});

		
		// create the "50-50" button to show fewer answer options
		fiftyFiftyButton = new Button(questionPanel, SWT.PUSH);
		fiftyFiftyButton.setText("50-50");
		data = new GridData(GridData.BEGINNING, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		fiftyFiftyButton.setLayoutData(data);

		// two operations to make the new widgets display properly
		questionPanel.pack();
		questionPanel.getParent().layout();
	}

	/**
	 * Opens the main window and executes the event loop of the application
	 */
	private void runApplication() {
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
	}
	
	private class Question {
		private boolean wasAsked;
		private String question;
		private List<String> answers = new ArrayList<String>();
		private List<String> shuffledAnswers = new ArrayList<String>();
		
		public Question(String[] arr) {
			this.question = arr[0];
			for (int i=0; i<4; i++) {
				this.answers.add(arr[i+1]);
				this.shuffledAnswers.add(arr[i+1]);
			}
			Collections.shuffle(this.shuffledAnswers);
		}
	}
	
}
