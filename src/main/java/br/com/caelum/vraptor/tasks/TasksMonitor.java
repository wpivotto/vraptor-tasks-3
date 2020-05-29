package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;

import com.google.common.collect.Maps;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.callback.TaskEventNotifier;

@Component
@ApplicationScoped
public class TasksMonitor implements JobListener {
	
	private final TaskEventNotifier notifier;
	private Scheduler scheduler;
	private Map<String, TaskStatistics> statistics = Maps.newHashMap();

	public TasksMonitor(TaskEventNotifier notifier) {
		this.notifier = notifier;
	}
	
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		notifier.notifyExecutionVetoedEvent(getId(context));
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		findStats(context);
		notifier.notifyBeforeExecuteEvent(getId(context));
	}
	
	
	public TaskStatistics getStatisticsFor(String taskId) {
		TaskStatistics stats = statistics.get(taskId);
		if(stats != null)
			stats.updateTriggerState(scheduler);
		return stats;
	}

	public Collection<TaskStatistics> getStatistics() {
		for(TaskStatistics stats : statistics.values()) {
			stats.updateTriggerState(scheduler);
		}
		return statistics.values();
	}
	
	private String getId(JobExecutionContext context) {
		return context.getJobDetail().getJobDataMap().getString("task-id");
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		String taskId = getId(context);
		TaskStatistics stats = findStats(context);
		if (stats != null) {
			stats.update(context, exception);
			if(exception != null)
				notifier.notifyFailedEvent(taskId, stats, exception);
			else
				notifier.notifyExecutedEvent(taskId, stats);
		}
	}
	
	private TaskStatistics findStats(JobExecutionContext context) {
		String taskId = getId(context);
		if(!statistics.containsKey(taskId))
			statistics.put(taskId, new TaskStatistics(taskId, context));
		return statistics.get(taskId);
	}
	

}
