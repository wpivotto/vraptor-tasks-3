package br.com.caelum.vraptor.tasks.scheduler;

import javax.annotation.PreDestroy;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.ComponentFactory;
import br.com.caelum.vraptor.tasks.TasksMonitor;

@Component
@ApplicationScoped
public class SchedulerCreator implements ComponentFactory<Scheduler> {

	private static Logger logger = LoggerFactory.getLogger(SchedulerCreator.class);
	private final Scheduler scheduler;

	public SchedulerCreator(JobFactory factory, TasksMonitor monitor) {

		try {
			this.scheduler = new StdSchedulerFactory().getScheduler();
			this.scheduler.setJobFactory(factory);
			this.scheduler.start();
			this.scheduler.getListenerManager().addJobListener(monitor);
			monitor.setScheduler(this.scheduler);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	public Scheduler getInstance() {
		return scheduler;
	}

	@PreDestroy
	public void stop() {
		try {
			this.scheduler.shutdown(true);
		} catch (SchedulerException e) {
			logger.error("ERROR", e);
		}

	}

}
