# Getting Started

MongoDB docker image dependency.

```shell
docker pull mongo
docker run -d mongo
```
Application create `test` database and tables: `doorman_data, emails`

This example takes 5 hours of development. Unfortunately I had not more time, because of family issues.
In general, you should catch the main idea.

You don't find in the project:
- unit tests;
- integration tests;
- app did not test `resources` (getting by url with recursion loop);
- swagger;
- docker compose for running mongo and app in common script;
