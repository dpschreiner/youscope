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
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.uielements.DynamicPanel;

class SelectMeasurementPanel extends DynamicPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7931514904649793034L;
//	private final JTextField folderField = new JTextField();
	private final JTextField configFileField = new JTextField();

//	private final IntegerTextField imageNumberField = new IntegerTextField(1);
//	private final PeriodField previousRuntimeField = new PeriodField();
	final YouScopeClient client;
	private final ArrayList<SelectionListener> listeners = new ArrayList<>();
	public SelectMeasurementPanel(YouScopeClient client, String lastFolder, String lastConfigFile) 
	{
		this.client = client;
		/**
		 *  You might find out that stuff by some kind of wizzard, but since I am lazy there is no wizard, yet, 
		 *  and one has to enter the stuff by hand.
		 *  You might e.g. want to only ask for the path for the config file. By having the config file, you can construct the
		 *  previous save settings. With the previous save settings, you can query the location of the image table. Then, you can load the
		 *  image table and search for the highest image number (=delta-1) and the time when the last image was taken (=last runtime). Similarly, you can
		 *  reconstruct the base folder of the measurement by asking the old save settings for their base folder, and compare it with the folder where you found the config.
		 */
		// internally, we count zero based, but for the user we count one based.
//		imageNumberField.setMinimalValue(1);
//		imageNumberField.setValue(100);
//		if(lastFolder == null)
//			lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
//		folderField.setText(lastFolder == null ? "" : lastFolder);
//		previousRuntimeField.setDuration(3600*1000);
		configFileField.setText(lastConfigFile == null ? "" : lastConfigFile);
		
//		add(new JLabel("Directory of measurement which should be continued:"));
//		JPanel folderPanel = new JPanel(new BorderLayout(10, 10));
//		folderPanel.add(folderField, BorderLayout.CENTER);
//		if(getClient().isLocalServer())
//		{
//			JButton openFolderChooser = new JButton("Edit");
//			openFolderChooser.addActionListener(new ActionListener()
//			{
//				@Override
//				public void actionPerformed(ActionEvent arg0)
//				{
//					JFileChooser fileChooser = new JFileChooser(folderField.getText());
//					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//					int returnVal = fileChooser.showDialog(null, "Open");
//					if(returnVal == JFileChooser.APPROVE_OPTION)
//					{
//						folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
//					}
//				}
//			});
//			folderPanel.add(openFolderChooser, BorderLayout.EAST);
//		}
//		add(folderPanel);
		
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
					}
				}
			});
			configFilePanel.add(configFileBrowseButton, BorderLayout.EAST);
		}
		add(configFilePanel);

		// TODO construct a panel to hold output extracted from the config and its basedir (e.g. last image number)
		JPanel outputPanel = new JPanel();
		
//		add(new JLabel("Image number to proceed with:"));
//		add(imageNumberField);
//		add(new JLabel("Runtime of previous measurement:"));
//		add(previousRuntimeField);
		
		addFillEmpty();

        JButton appendButton = new JButton("Continue Measurement");
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
	
	private void appendMeasurement()
	{
		String configFileName = configFileField.getText();
		MeasurementConfiguration lastConfig;
		try {
			Configuration lastConfigTemp = ConfigurationManagement.loadConfiguration(configFileName);
			if(!(lastConfigTemp instanceof MeasurementConfiguration))
			{
				getClient().sendError("Provided configuration is not a measurement configuration.");
				return;
			}
			lastConfig = (MeasurementConfiguration) lastConfigTemp;
		} catch (IOException e) {
			getClient().sendError("Could not open last config.", e);
			return;
		}
		new SaveSettingsConfiguration()lastConfig.getSaveSettings().getTypeIdentifier()

		// TODO get the info from the config file and its base dir
		String baseFolder = new File(configFileName).getParent();
		String imagesCsvFileName = baseFolder + "images.csv"; // if this can be user-specified, then get the right version

		getClient().sendError("baseFolder " + baseFolder + ", imagesCsvFileName " + imagesCsvFileName);

		long deltaEvaluation = getLastImageNumber(imagesCsvFileName) - 1;
		long previousRuntime = getLastRuntime(imagesCsvFileName);
		for(SelectionListener listener : listeners)
		{
			listener.selectionMade(lastConfig, baseFolder, deltaEvaluation, previousRuntime);
		}
	}

	private long getLastImageNumber (String imagesCsvFileName) {
		return Long.parseLong(getFieldFromLastLineOfQuotedCsv(imagesCsvFileName, ";", 1));
	}

	private long getLastRuntime (String imagesCsvFileName) {
		return Long.parseLong(getFieldFromLastLineOfQuotedCsv(imagesCsvFileName, ";", 2));
	}

	private String getFieldFromLastLineOfQuotedCsv(String csvFileName, String delimiter, int fieldNumber) {
		String field = "";
		try
		{
			ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(csvFileName), Charset.forName("US-ASCII"));
			String line;
			String patternString = "^";
			for (int i = 0; i < fieldNumber; i++) {
				String group = (i == fieldNumber - 1)
						? "\"(.*?)\""
						: "\".*?\"";
				patternString += group + delimiter;
			}

			Pattern pattern = Pattern.compile(patternString);
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
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
