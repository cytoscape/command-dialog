/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.cytoscape.commandDialog.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.handlers.MessageHandler;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class CommandToolDialog extends JDialog implements ActionListener {

	private static final String NEXT = "next";
	private static final String PREVIOUS = "previous";
	
	private List<String> commandList;
	private int commandIndex = 0;

	// Dialog components
	private JResultsPane resultsText;
	private JTextField inputField;
	private CommandHandler commandHandler;
	
	public CommandToolDialog (final Frame parent, final CommandHandler commandHandler) {
		super(parent, false);
		commandList = new ArrayList<>();
		this.commandHandler = commandHandler;
		
		initComponents();
	}

	@Override
	public void setVisible(boolean tf) {
		super.setVisible(tf);
		getInputField().requestFocusInWindow();
	}

	/**
	 * Initialize all of the graphical components of the dialog
	 */
	private void initComponents() {
		setTitle("Command Line Dialog");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Create a panel for the main content
		final JPanel dataPanel = new JPanel();

		final JLabel resultsLabel = new JLabel("Reply Log:");
		final JLabel inputLabel = new JLabel("Command:");
		
		resultsText = new JResultsPane(this, dataPanel);
		resultsText.setEditable(false);
		
		final JScrollPane scrollPane = new JScrollPane(resultsText);
		// scrollPane.getVerticalScrollBar().addAdjustmentListener(resultsText);
		resultsText.setScrollPane(scrollPane); // So we can update the scroll position

		// Create the button box
		final JButton doneButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		final JButton clearButton = new JButton("Clear");
		clearButton.setToolTipText("Clear Log");
		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);
		clearButton.putClientProperty("JButton.buttonType", "gradient");

		final JPanel buttonBox = LookAndFeelUtil.createOkCancelPanel(null, doneButton);
		buttonBox.add(clearButton);
		buttonBox.add(doneButton);
		
		final GroupLayout layout = new GroupLayout(dataPanel);
		dataPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(resultsLabel)
								.addComponent(clearButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(inputLabel)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(scrollPane, DEFAULT_SIZE, 880, Short.MAX_VALUE)
								.addComponent(getInputField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
				.addComponent(buttonBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(resultsLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addGap(1, 1, Short.MAX_VALUE)
								.addComponent(clearButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(scrollPane, DEFAULT_SIZE, 580, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(inputLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getInputField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(buttonBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		setContentPane(dataPanel);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, doneButton.getAction());
		pack();
	}

	/**
 	 * External interface for users to inject commands
 	 */
	public void executeCommand(String command) {
		resultsText.appendCommand(command);
		commandHandler.handleCommand((MessageHandler) resultsText, command);
	}

	/**
	 * External interface to run a single command and get the result.
	 */
	public String executeCommandAndReturnResult(String command) {
		return commandHandler.handleCommand((MessageHandler) resultsText, command);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("clear".equals(e.getActionCommand())) {
			resultsText.clear();
		} else {
			String input = getInputField().getText();
			resultsText.appendCommand(input);
			commandList.add(input);
			commandIndex = commandList.size();

			commandHandler.handleCommand((MessageHandler) resultsText, input);

			getInputField().setText("");
		}
	}

	private JTextField getInputField() {
		if (inputField == null) {
			inputField = new JTextField();
			
			// Set up our up-arrow/down-arrow actions
			final Action previousAction = new LineAction(PREVIOUS);
			inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), PREVIOUS);
			inputField.getActionMap().put(PREVIOUS, previousAction);

			final Action nextAction = new LineAction(NEXT);
			inputField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), NEXT);
			inputField.getActionMap().put(NEXT, nextAction);
			inputField.addActionListener(this);
		}
		
		return inputField;
	}
	
	private class LineAction extends AbstractAction {
		
		String action = null;
		
		public LineAction(String action) {
			super();
			this.action = action;
		}
			
		@Override
		public void actionPerformed(ActionEvent e) {
			if (commandList.size() == 0)
				return;
			
			if (action.equals(NEXT)) {
				commandIndex++;
			} else if (action.equals(PREVIOUS)) {
				commandIndex--;
			} else {
				return;
			}

			final String inputCommand;
			
			if (commandIndex >= commandList.size()) {
				inputCommand = "";
				commandIndex = commandList.size();
			} else if (commandIndex < 0) {
				inputCommand = "";
				commandIndex = -1;
			} else {
				inputCommand = commandList.get(commandIndex);
			}
			
			getInputField().setText(inputCommand);
			getInputField().selectAll();
		}
	}
}
