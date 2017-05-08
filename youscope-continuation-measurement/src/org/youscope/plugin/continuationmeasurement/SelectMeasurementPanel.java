/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.plugin.continuationmeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.input.ReversedLinesFileReader;

import org.youscope.addon.ConfigurationManagement;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.uielements.DynamicPanel;

class SelectMeasurementPanel extends DynamicPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7931514904649793034L;
	private final JTextField configFileField = new JTextField();
    private JButton appendButton = new JButton("Continue Measurement");
    private JLabel nextEvaluationNumberField = new JLabel();
    private JLabel previousRuntimeField = new JLabel();
	private long deltaEvaluation = 0;
	private long previousRuntime = 0;
	
	final YouScopeClient client;
	private final ArrayList<SelectionListener> listeners = new ArrayList<>();	
	
	/**
	 * Simple UI to:
	 *  - load an existing configuration file (e.g. configuration.csb)
	 *  - parse images.csv to determine what would be the next evaluation number and already elapsed runtime
	 *  - and pass the measurement off to be executed again, effectively continuing the measurement by starting 
	 *    with the next evaluation number and adding to the previous runtime
	 * @param client
	 * @param lastFolder remembered previous value
	 * @param lastConfigFile remembered previous value
	 */
	public SelectMeasurementPanel(YouScopeClient client, String lastFolder, String lastConfigFile) 
	{
		this.client = client;

		// Set the last used location if it exists
		configFileField.setText(lastConfigFile == null ? "" : lastConfigFile);
		
		add(new JLabel("Configuration file (e.g. configuration.csb) of measurement you wish to continue:"));
		addEmpty();
		JPanel configFilePanel = new JPanel(new BorderLayout(10, 100));
		configFilePanel.add(configFileField, BorderLayout.CENTER);
		if(getClient().isLocalServer())
		{
			JButton configFileBrowseButton = new JButton("Browse");
			configFileBrowseButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser configFileChooser = new JFileChooser(configFileField.getText());
					configFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = configFileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						configFileField.setText(configFileChooser.getSelectedFile().getAbsolutePath());
						parseConfig();
						nextEvaluationNumberField.setText(Long.toString(deltaEvaluation+1)); // add 1 to show next one to use
						previousRuntimeField.setText(Long.toString(previousRuntime));
						appendButton.setEnabled(deltaEvaluation > 0 && previousRuntime > 0); // disable continue button if the values weren't parsed from file
					}
				}
			});
			configFilePanel.add(configFileBrowseButton, BorderLayout.EAST);
		}
		add(configFilePanel);

		// Panel showing the values to be used as extracted from images.csv
		JPanel evaluationNumberPanel = new JPanel(new BorderLayout(10, 100));
		JLabel evaluationNumberLabel = new JLabel("Evaluation/loop number to proceed with: ");
		evaluationNumberPanel.add(evaluationNumberLabel, BorderLayout.WEST);
		evaluationNumberPanel.add(nextEvaluationNumberField);
		JPanel previousRuntimePanel = new JPanel(new BorderLayout(10, 100));
		JLabel previousRuntimeLabel = new JLabel("Runtime of previous measurement: ");
		previousRuntimePanel.add(previousRuntimeLabel, BorderLayout.WEST);
		previousRuntimePanel.add(previousRuntimeField);		
		addEmpty();
		add(evaluationNumberPanel);
		add(previousRuntimePanel);
		addFillEmpty();

		appendButton.setEnabled(false); // don't enable button until a config is successfully loaded
        appendButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				appendMeasurement();
			}
		});
		add(appendButton);

	}
	private YouScopeClient getClient()
	{
		return client;
	}
	
	/**
	 * return the result of trying to load a filename as a MeasurementConfiguration
	 * @param configFileName
	 * @return
	 */
	private MeasurementConfiguration getConfigFile(String configFileName) {
		MeasurementConfiguration lastConfig = null;
		try {
			Configuration lastConfigTemp = ConfigurationManagement.loadConfiguration(configFileName);
			if(!(lastConfigTemp instanceof MeasurementConfiguration))
			{
				getClient().sendError("Provided configuration is not a measurement configuration.");
			}
			lastConfig = (MeasurementConfiguration) lastConfigTemp;
		} catch (IOException e) {
			getClient().sendError("Could not open last config.", e);
		}
		return lastConfig;
	}
	
	/**
	 * return the basename of the string passed in
	 * @param fileName
	 * @return
	 */
	private String getBaseFolder(String fileName) {
		return new File(fileName).getParent();
	}
	
	/**
	 * get the filename from the text box and try to load last evaluation number and runtime from corresponding images.csv
	 */
	private void parseConfig()
	{
		String baseFolder = getBaseFolder(configFileField.getText());
		
		// get the last evaluation number and runtime from the images.csv file
		String imagesCsvFileName = baseFolder + File.separator + "images.csv"; // TODO if this can be user-specified, then get the right version

		deltaEvaluation = getLastEvaluationNumber(imagesCsvFileName);
		previousRuntime = getLastRuntime(imagesCsvFileName);				
	}
	
	/**
	 * start the actual measurement
	 */
	private void appendMeasurement()
	{
		String configFileName = configFileField.getText();
		MeasurementConfiguration lastConfig = getConfigFile(configFileName);
		String baseFolder = new File(configFileName).getParent();
		
		getClient().sendMessage("Continuation Measurement: deltaEvaluation " + deltaEvaluation + ", previousRuntime " + previousRuntime);
		for(SelectionListener listener : listeners)
		{
			listener.selectionMade(lastConfig, baseFolder, deltaEvaluation, previousRuntime);
		}				
	}

	/**
	 * the evaluation number is the first field of the csv file - get the last one recorded in the images.csv file passed in
	 * @param imagesCsvFileName
	 * @return the last image/evaluation number
	 */
	private long getLastEvaluationNumber (String imagesCsvFileName) {
		return Long.parseLong(getFieldFromLastLineOfQuotedCsv(imagesCsvFileName, ";", 1));
	}

	/**
	 * the runtime value is the second field of the csv file - get the last one recorded in the images.csv file passed in
	 * @param imagesCsvFileName
	 * @return
	 */
	private long getLastRuntime (String imagesCsvFileName) {
		return Long.parseLong(getFieldFromLastLineOfQuotedCsv(imagesCsvFileName, ";", 2));
	}

	/**
	 * return a field from the last line of the input file
	 * @param csvFileName the file whose last line should be examined
	 * @param delimiter the delimiter used in the file
	 * @param fieldNumber which field/column to extract from the file (1-based)
	 * @return String representation of the extracted field or null
	 */
	private String getFieldFromLastLineOfQuotedCsv(String csvFileName, String delimiter, int fieldNumber) {
		String field = null;
		try
		{
			// Read file last line first
			ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(csvFileName), 4, Charset.forName("US-ASCII"));
			String line;
			// Construct the pattern used to match the requested fieldNumber
			String patternString = "^"; // start pattern at beginning of line
			for (int i = 0; i < fieldNumber; i++) {
				// use grouping () for the desired fieldNumber only
				String group = (i == fieldNumber - 1)
						? "\"(.*?)\""
						: "\".*?\"";
				patternString += group + delimiter;
			}

			Pattern pattern = Pattern.compile(patternString);
			/* Read the lines of the file from the end, matching on the first line that matches (assumed to be the last, 
			   but if there is a partial/malformed last line, it will continue to the second to last line, etc. */
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					// if there's a match, the grouped part is the desired fieldNumber
					field = matcher.group(1);
					break;
				}
			}
			reader.close();
		}
		catch (Exception e)
		{
			getClient().sendError("Exception occurred trying to read field " + fieldNumber + " from " + csvFileName);
		}
		return field;
	}
	
	public static interface SelectionListener
	{
		void selectionMade(MeasurementConfiguration lastConfig, String folder, long deltaEvaluation, long previousRuntime);
	}
	public void addSelectionListener(SelectionListener listener)
	{
		listeners.add(listener);
	}
	public void removeSelectionListener(SelectionListener listener)
	{
		listeners.remove(listener);
	}
}
