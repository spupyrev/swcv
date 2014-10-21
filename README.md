Semantic Word Cloud Visualization
=====
**Cloudy** is a system to create semantics-aware word cloud visualizations. A [live version of this code](http://wordcloud.cs.arizona.edu) is available, including a description of the algorithms implemented and several research papers describing how the system works.

Basic Setup and Usage Information
--------

The system can be accessed via command-line or web interfaces. It can also be used as a java library.

###Command-line usage:

1. Install [Java SE Runtime Environment 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Download [cloudy.jar](downloads/cloudy.jar)

3. Prepare input text file and invoke the tool by running *"java -jar cloudy.jar [options] [input file]"* or simply *"cloudy.jar [options] [input file]"* (without quotes). The options can be printed by running *"cloudy.jar -?"*

###Using web interface:

1. Install [Java SE Runtime Environment 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Install [GWT](http://www.gwtproject.org) library

3. Download [cloudy](/cloudy) and [webapp](/webapp) projects

4. Follow the [instructions](http://www.gwtproject.org/gettingstarted.html) on how to compile and deploy the web application

License
--------
Code is released under the [MIT License](LICENSE).
