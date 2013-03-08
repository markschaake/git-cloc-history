# git cloc history #
Command line tool for generating CSV of lines of code history for a repository.

You must have [cloc](http://cloc.sourceforge.net/ ) installed and on your PATH.

## Building ##
To build, run the project in SBT
    $ cd /path/to/git-cloc-history
	$ sbt
	> assembly
This should generate target/git-cloc-history-assembly-0.1-SNAPSHOT.jar

## Running the Application##

    $ java target/git-cloc-history-assembly-0.1-SNAPSHOT.jar [args]

## Command Line Options ##

By default, the application assumes you want cloc history for the master branch in the current directory. The following options are available:

* Specify a different branch with "-b [branch]" or "--branch [branch]"
* Specify a specific subdirectory for cloc with "-d [subdir path]" or "--clocdir [subdir path]"
* Specify a "from" date to limit git revs to consider with "-f [yyyy-MM-dd]" or "--fromdate [yyyy-MM-dd]"

## Tip: use a shell script for easier use ##

For convenience, I create use a shell script that encapsulates the java application:

    #!/bin/bash
	# script location: ~/bin/git-cloc-history.sh
	java /absolute/path/to/git-cloc-history/target/git-cloc-history-assembly-0.1-SNAPSHOT.jar 
	
Assuming ~/bin is on your PATH, you can call it from anywhere.

    $ cd ~/gitrepos/somerepo
	$ git-cloc-history.sh
	CLOC history successfully written to ~/gitrepos/somerepo/cloc-history.csv
	$ cat cloc-history.csv # this is the output CSV file
