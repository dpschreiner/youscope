/**
 * 
 */
package org.youscope.plugin.cellx;

import java.awt.Desktop;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenCellXFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		if(OpenCellX.TYPE_IDENTIFIER.equals(ID))
		{
			return new OpenCellX(client, server, measurementFolder);
		}
		throw new AddonException("Type identifer "+ID+" not supported by this factory.");
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		if(Desktop.isDesktopSupported())
			return new String[]{OpenCellX.TYPE_IDENTIFIER};
		return new String[0];
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		if(OpenCellX.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException {
		if(OpenCellX.TYPE_IDENTIFIER.equals(typeIdentifier))
			return OpenCellX.getMetadata();
		throw new AddonException("Type identifer "+typeIdentifier+" not supported by this factory.");
	}

}