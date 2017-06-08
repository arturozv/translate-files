**What it does:**

Translates the content of the input files. Check the problem definition at [problem.txt](Test.txt)

**How to run it:**

- _Option 1_: import the project to your IDE and run the TranslateFilesApplication.java main
- _Option 2_: install the project with maven and run the jar. check the shell `run.sh`

**Why Spring Batch:**

- Fits the use case very well
- Focus on the business logic, not random things like reading or writing files, flows etc.
- Easy to scale and adapt (threads, chunk size) without changing the architecture
- Good old battle tested framework

**Run times:**

- 8 files, 100k lines per file = 800k lines -> 40secs
- 8 files, 10M lines per file = 80M lines -> 4000secs