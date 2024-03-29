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
import java.util.Stack;

import org.eclipse.swt.SWT;
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
	private int questionsAnswered;
	private Stack<Question> qStack = new Stack<Question>();
	private int wrongAnswers;
	private Question curQuestion;
	private int curScores;
	private boolean isAnswersAvaliable;
	private boolean passUsedFirst;
	private boolean fiftyFiftyUsedFirst;
	
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
							qStack.add(new Question(splittedLine));
							line = br.readLine();
						}
						Collections.shuffle(qStack);
						br.close();
					}
		        	catch (FileNotFoundException e1) { // won't happen
						GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containing a question and four answers, seperated by tabs.");
					}
		        	catch (IOException e1) { // won't happen
		        		GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containing a question and four answers, seperated by tabs.");
					}
		        	lastAnswer = "";
		        	questionsAnswered = 0;
		        	wrongAnswers = 0;
		        	curScores = 0;
		        	scoreLabel.setText("0");
		        	isAnswersAvaliable = true;
		        	passUsedFirst = false;
		        	fiftyFiftyUsedFirst = false;
		        	updateRandomQuestion("Play");		        	
		        }
		      }
		});
	
	}
	
	private void updateRandomQuestion(String instruction) {
    	curQuestion = qStack.pop();
    	updateQuestionPanel(curQuestion.question, curQuestion.shuffledAnswers);
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
		//List<String> currentAnswers = answers;
		
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
			for (Button b : answerButtons) {
				b.addListener(SWT.Selection, new Listener() {
				   	public void handleEvent(Event e) {
				        if (e.type == SWT.Selection && isAnswersAvaliable) {
							String rightAns = curQuestion.answers.get(0);
							if (b.getText().equals(rightAns)) {
								curScores += 3;
								wrongAnswers = 0;
								lastAnswer = "Correct! ";
							}
							else {
								curScores -= 2;
								wrongAnswers += 1;
								lastAnswer = "Wrong... ";
							}
							
							scoreLabel.setText(String.valueOf(curScores));
							if (qStack.isEmpty() || wrongAnswers == MAX_ERRORS) {
								GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + curScores + " after " + questionsAnswered + " questions.");
								isAnswersAvaliable = false;
							}
							else
								updateRandomQuestion("Answer");
							}
					   	}
				});
			}
		

		// create the "Pass" button to skip a question
		passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.END, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		passButton.setLayoutData(data);
		if (curScores <= 0 && passUsedFirst)
    		passButton.setEnabled(false);
		
		// Pass Listener
		passButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {  
		    	if (e.type == SWT.Selection) {
		        	if (!passUsedFirst)
		        		passUsedFirst = true;
		        	else {
		        		curScores -= 1;
		        		scoreLabel.setText(String.valueOf(curScores));
		        	}
		        	lastAnswer = "";
		        	if (qStack.isEmpty()) {
						GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + curScores + " after " + questionsAnswered + " questions.");
						isAnswersAvaliable = false;
					}
		        	else
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
		if (curScores <= 0 && fiftyFiftyUsedFirst)
    		fiftyFiftyButton.setEnabled(false);
		
		// 50-50 Listener
		Random rand = new Random();
		fiftyFiftyButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		    	if (e.type == SWT.Selection) {
		        	if (!fiftyFiftyUsedFirst)
		        		fiftyFiftyUsedFirst = true;
		        	else {
		        		curScores -= 1;
		        		scoreLabel.setText(String.valueOf(curScores));
		        	}
		        	int numAnswerToKeep = rand.nextInt(3) + 1; // 1/2/3
		        	String rightAns = curQuestion.answers.get(0);
		        	String ansToKeep = curQuestion.answers.get(numAnswerToKeep);
		        	for (Button b : answerButtons) {
		        		if (!b.getText().equals(rightAns) && !b.getText().equals(ansToKeep))
		        			b.setEnabled(false);
		        	}
		        	fiftyFiftyButton.setEnabled(false);
		        }
		      }
		});
		
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
