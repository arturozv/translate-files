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
- 3 files with 50 words per file = 150 lines -> 3,5s (using google translate api) ([GC logs](http://gceasy.io/my-gc-report.jsp?p=c2hhcmVkLzIwMTcvMDYvOC8tLWdjLmxvZy0tNy0xMS0zOQ==))
- 8 files, 100k lines per file = 800k lines -> 40secs (using mock translations to avoid api rate limits)

**Batch design:**

![Batch](/translate-files-test.png)
