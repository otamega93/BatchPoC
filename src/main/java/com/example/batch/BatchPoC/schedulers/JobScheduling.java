package com.example.batch.BatchPoC.schedulers;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduling {

	@Autowired
	@Qualifier("importUserJob")
	private Job job;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	/**
	 * When running every job separately (like we're doing here) you need to specify which one you want to run/inject.
	 * Since every job is a Job object/bean, we need to choose the one we want with @Qualifier.
	 * The regular Spring batch run executed when starting the app runs every single job without this happening
	 * The regular Spring batch run is disabled in the application.properties by spring.batch.job.enabled=false
	 * 
	 * @throws JobExecutionAlreadyRunningException
	 * @throws JobRestartException
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobParametersInvalidException
	 */
    @Scheduled(fixedRate=5000)
    public void executeJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        
    	jobLauncher.run(job, new JobParameters());
    }
}
