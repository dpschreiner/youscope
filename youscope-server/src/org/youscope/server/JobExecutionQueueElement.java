/**
 * 
 */
package org.youscope.server;

import org.youscope.common.job.Job;

/**
 * An element in the job execution queue.
 * @author Moritz Lang
 * 
 */
class JobExecutionQueueElement
{
	public final int			evaluationNumber;
	public final Job	job;
	public final long			measurementStartTime;

	public JobExecutionQueueElement(Job job, int evaluationNumber, long measurementStartTime)
	{
		this.job = job;
		this.evaluationNumber = evaluationNumber;
		this.measurementStartTime = measurementStartTime;
	}
}