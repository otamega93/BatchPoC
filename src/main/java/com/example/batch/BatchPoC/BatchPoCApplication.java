package com.example.batch.BatchPoC;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.batch.BatchPoC.entities.Person;
import com.example.batch.BatchPoC.listeners.JobCompletionNotificationListener;
import com.example.batch.BatchPoC.processors.PersonItemProcessor;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class BatchPoCApplication {

	@Value("classpath*:sample*.csv")
	private Resource[] resources;

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
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>().name("personItemReader")
				//.resource(new ClassPathResource("sample-data.csv"))
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
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO person (name, lastName) VALUES (:name, :lastName)")
				.dataSource(dataSource)
				.build();
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
	public Job importUserJob2(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob2")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1)
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
	public Step step1(JdbcBatchItemWriter<Person> writer) {
		return stepBuilderFactory.get("step1")
				.<Person, Person>chunk(10)
				//.reader(reader())
				.reader(multiResourceItemReader()) // To load multiple resources
				.processor(processor())
				.writer(writer)
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchPoCApplication.class, args);
	}

}
