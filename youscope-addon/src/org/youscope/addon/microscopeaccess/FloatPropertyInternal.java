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
/**
 * 
 */
package org.youscope.addon.microscopeaccess;

import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
public interface FloatPropertyInternal extends PropertyInternal
{
	/**
	 * Returns the current value of the device property as a float.
	 * @return Value of property.
	 * @throws MicroscopeException
	 * @throws NumberFormatException Thrown when value is not a float.
	 * @throws InterruptedException
	 */
	public float getFloatValue() throws MicroscopeException, NumberFormatException, InterruptedException;
	
	/**
	 * Sets the current value of the property.
	 * @param value Value to set the property to.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 */
	public void setValue(float value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;
	
	/**
	 * Sets the value of the microscope relative to its current value. Throws a NumberFormatException if the current value does not correspond to a float value.
	 * @param offset Amount for which the current value should be changed.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws NumberFormatException Thrown when current value is not a float.
	 * @throws InterruptedException
	 */
	public void setValueRelative(float offset, int accessID) throws MicroscopeException, MicroscopeLockedException, NumberFormatException, InterruptedException;

	/**
	 * Returns the lower limit of this value.
	 * Returns Float.MIN_VALUE if this property does not have limits.
	 * @return Minimal value this property can have.
	 */
	float getLowerLimit();

	/**
	 * Returns the upper limit of this value.
	 * Returns Float.MAX_VALUE if this property does not have limits.
	 * @return Maximal value this property can have.
	 */
	float getUpperLimit();
}
