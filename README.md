# BatchPoC
Spring Batch PoC

This is just a very basic PoC of Spring Batch. The master branch has a classic single running when executed approach (you start the java app and then it executes the batch jobs).

The Scheluder branch drops the single running approach and instead uses a scheduler approach (it runs two jobs manually every 30 seconds).

The jpa-transaction branch is the same as the previous ones but uses a jpa writer (and explains that the @EnabledBatchConfigurer, by default, set a transaction manager into the jobRepository. So, by default, our jobs are transactional.

The jdbc_reader-jpa_writer branch is like the jpa-transaction but this one reads the data from DB, process it and insert it using the jpa writer.