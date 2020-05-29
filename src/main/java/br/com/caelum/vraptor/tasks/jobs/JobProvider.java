package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

public interface JobProvider {

	Job newJob(Task task, Scheduled options, JobDetail detail);
	boolean canProvide(Class<? extends Job> job);
	boolean canDecorate(Class<? extends Task> task);
	Class<? extends Job> getJobWrapper(Scheduled options);

}