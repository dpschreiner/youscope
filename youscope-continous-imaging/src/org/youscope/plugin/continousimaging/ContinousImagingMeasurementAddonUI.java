/**
 * 
 */
package org.youscope.plugin.continousimaging;

import javax.swing.ImageIcon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 */
class ContinousImagingMeasurementAddonUI extends MeasurementAddonUIAdapter<ContinousImagingMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param client YouScope client.
	 * @throws AddonException 
	 */
	ContinousImagingMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Continuous Imaging Measurement");
		
		String description = "A continuous imaging measurement is used to (rapidly) take images at the current position every given period.\n\n"+
				"One can select the channel, the exposure time and the imaging period. Instead of choosing an imaging period, one can also choose to \"bulk image\", which means to image as fast as possible.";
		ImageIcon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/continousimaging/images/continous-imaging.jpg", "Continuous Measurement");
		addPage(new DescriptionPage(null, description, image, null));
		addPage(new GeneralSettingsPage(client, server)); 
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new ImagingDefinitionPage(client, server));

	}
	
	static ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ContinousImagingMeasurementConfiguration>(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER, 
				ContinousImagingMeasurementConfiguration.class, 
				Measurement.class, "Continuous Imaging Measurement", new String[0], "icons/camcorder.png");
	}
}
