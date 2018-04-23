package com.example.batch.BatchPoC;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.batch.BatchPoC.entities.Person;
import com.example.batch.BatchPoC.listeners.JobCompletionNotificationListener;
import com.example.batch.BatchPoC.processors.PersonItemProcessor;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class BatchPoCApplication {

	@Value("classpath*:sample-data*.csv")
	private Resource[] resources;
	
	@Value("classpath*:sample-jpa*.csv")
	private Resource[] resources2;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	
	@Bean
	public MultiResourceItemReader<Person> multiResourceItemReader() {
		MultiResourceItemReader<Person> resourceItemReader = new MultiResourceItemReader<Person>();
		resourceItemReader.setResources(resources);
		resourceItemReader.setDelegate(reader());
		return resourceItemReader;
	}

	@Bean
	public MultiResourceItemReader<Person> multiResourceItemReader2() {
		MultiResourceItemReader<Person> resourceItemReader = new MultiResourceItemReader<Person>();
		resourceItemReader.setResources(resources2);
		resourceItemReader.setDelegate(reader());
		return resourceItemReader;
	}
	
	@Bean
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>().name("personItemReader")
				.delimited()
				.names(new String[] { "name", "lastName" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
						setTargetType(Person.class);
				}})
				.build();
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JpaItemWriter<Person> writer(DataSource dataSource, EntityManagerFactory emf) throws Exception {
		JpaItemWriter<Person> jpa = new JpaItemWriter<Person>();
		jpa.setEntityManagerFactory(emf);
		jpa.afterPropertiesSet();
		return jpa;
	}

	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1)
				.end()
				.build();
	}
	
	@Bean
	public Job importUserJob2(JobCompletionNotificationListener listener, Step step2) {
		return jobBuilderFactory.get("importUserJob2")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step2)
				.end()
				.build();
	}

	/**
	 * chunk is the size of elements that will be read before the batch starts a new db transaction
	 * so, if chunk(10), then the batch will read 10 elements and then starts a transaction to 
	 * (in this case) insert the elements
	 * @param writer
	 * @return
	 */
	@Bean
	public Step step1(JpaItemWriter<Person> writer) {
		return stepBuilderFactory.get("step1")
				.<Person, Person>chunk(10)
				.reader(multiResourceItemReader()) // To load multiple resources
				.processor(processor())
				.writer(writer)
				.build();
	}
	
	@Bean
	public Step step2(JpaItemWriter<Person> writer) {
		return stepBuilderFactory.get("step2")
				.<Person, Person>chunk(10)
				.reader(multiResourceItemReader2()) // To load multiple resources
				.processor(processor())
				.writer(writer)
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchPoCApplication.class, args);
	}

}
