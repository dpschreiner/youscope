package org.youscope.plugin.measurementviewer.standalone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionProvider;

class MinimalMeasurementMetadataProvider implements MetadataDefinitionProvider
{

	@Override
	public boolean isAllowCustomMetadata() {
		return false;
	}

	@Override
	public Iterator<MetadataDefinition> iterator() {
		return getMetadataDefinitions().iterator();
	}

	@Override
	public Collection<MetadataDefinition> getMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public Collection<MetadataDefinition> getMandatoryMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public MetadataDefinition getMetadataDefinition(String name) {
		return null;
	}

	@Override
	public void setMetadataDefinition(MetadataDefinition property) {
		// do nothing.
	}

	@Override
	public void setMetadataDefinitions(Collection<MetadataDefinition> properties) {
		// do nothing.
	}

	@Override
	public boolean deleteMetadataDefinition(String name) 
	{
		return false;
	}

	@Override
	public Collection<MetadataDefinition> getDefaultMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public int getNumMetadataDefinitions() {
		return 0;
	}

}