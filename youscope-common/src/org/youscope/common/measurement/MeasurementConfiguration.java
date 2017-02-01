/**
 * 
 */
package org.youscope.common.measurement;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.SaveSettingsConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Superclass of all configurations of measurements.
 * 
 * @author Moritz Lang
 */
public abstract class MeasurementConfiguration implements Configuration
{
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the measurementRuntime
	 */
	public int getMeasurementRuntime()
	{
		return measurementRuntime;
	}

	/**
	 * @param measurementRuntime
	 *            the measurementRuntime to set
	 */
	public void setMeasurementRuntime(int measurementRuntime)
	{
		this.measurementRuntime = measurementRuntime;
	}

	/**
	 * @return the deviseSettingsOn
	 */
	public DeviceSetting[] getDeviseSettingsOn()
	{
		return deviseSettingsOn;
	}

	/**
	 * @param deviseSettingsOn
	 *            the deviseSettingsOn to set
	 */
	public void setDeviseSettingsOn(DeviceSetting[] deviseSettingsOn)
	{
		this.deviseSettingsOn = deviseSettingsOn;
	}

	/**
	 * @return the deviseSettingsOff
	 */
	public DeviceSetting[] getDeviseSettingsOff()
	{
		return deviseSettingsOff;
	}

	/**
	 * @param deviseSettingsOff
	 *            the deviseSettingsOff to set
	 */
	public void setDeviseSettingsOff(DeviceSetting[] deviseSettingsOff)
	{
		this.deviseSettingsOff = deviseSettingsOff;
	}

	/**
	 * Serial version UID.
	 */
	private static final long		serialVersionUID	= -4103638994655960751L;

	/**
	 * The name of the measurement.
	 */
	@XStreamAlias("name")
	private String					name				= "unnamed";

	@XStreamAlias("save-settings")
	private SaveSettingsConfiguration	saveSettings		= null;

	/**
	 * Runtime of the measurement in milliseconds. If time is over, measurement
	 * will be quit.
	 */
	@XStreamAlias("runtime")
	private int						measurementRuntime	= -1;

	/**
	 * Device Settings which should be activated at the beginning of the
	 * simulation
	 */
	@XStreamAlias("start-device-settings")
	private DeviceSetting[]		deviseSettingsOn	= new DeviceSetting[0];

	/**
	 * Device Settings which should be activated at the end of the simulation
	 */
	@XStreamAlias("end-device-settings")
	private DeviceSetting[]		deviseSettingsOff	= new DeviceSetting[0];

	/**
	 * Defines how the measurement should be saved. Set to null if the measurement should not be saved.
	 * @param saveSettings Definition how the measurement should be saved.
	 */
	public void setSaveSettings(SaveSettingsConfiguration saveSettings)
	{
		this.saveSettings = saveSettings;
	}

	/**
	 * Returns the definition how the measurement should be saved. Returns null if the measurement should not be saved.
	 * @return Definition how the measurement should be saved.
	 */
	public SaveSettingsConfiguration getSaveSettings()
	{
		return saveSettings;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(deviseSettingsOff == null)
			throw new ConfigurationException("Device settings off are null.");
		if(deviseSettingsOn == null)
			throw new ConfigurationException("Device settings on are null.");
	}
}
