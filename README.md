# git cloc history #
Command line tool for generating CSV of lines of code history for a repository.

You must have [cloc](http://cloc.sourceforge.net/ ) installed and on your PATH.

## Building ##
To build, run the project in SBT

    $ cd /path/to/git-cloc-history
	$ sbt
	> universal:packageBin
	
This should generate `target/universal/git-cloc-history-0.1-SNAPSHOT.zip`

## Running the Application##

Unzip the artifact and you'll find it contains `bin` and `lib` directories:

    $ unzip target/universal/git-cloc-history-0.1-SNAPSHOT.zip
    $ l -al target/universal/git-cloc-history-0.1-SNAPSHOT
    >> bin lib

Use the executable in the `bin` directory apporopriate for your system:

    $ target/universal/git-cloc-history-0.1-SNAPSHOT/bin/git-cloc-history [args]

## Command Line Options ##

By default, the application assumes you want cloc history for the current branch in the current directory.

Run the executable without any arguments and you will get a listing of the available options and defaults.

## Tip: create a symbolic link to the executable for easier use ##

For convenience, I have a symbolic link to the executable in my ~/bin directory (which is loaded to my PATH):

    $ ln -s target/universal/git-cloc-history-0.1-SNAPSHOT/bin/git-cloc-history ~/bin/git-cloc
	
Assuming ~/bin is on your PATH and git-cloc-history.sh is executable, you can call it from anywhere.

    $ cd ~/gitrepos/somerepo
	$ git-cloc
	CLOC history successfully written to ~/gitrepos/somerepo/cloc-history.csv
	$ cat cloc-history.csv # this is the output CSV file
