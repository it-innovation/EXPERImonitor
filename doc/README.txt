# EXPERIMEDIA ECC Documentation #
#-------------------------------#

This is the documentation for the Experiment Content Component (ECC) developed by
the IT Innovation Centre at the University of Southampton in the EXPERIMEDIA
project.

The documentation is written with [reStructuredText](http://docutils.sourceforge.net/rst.html)
(rst), which needs to be built (HTML, PDF, etc.) using the [Sphinx](sphinx-doc.org/) tool.

If you already have Python installed on your machine, installing Sphinx should
be as simple as:

    $ easy_install -U Sphinx

If it does not work, browse the Sphinx website to find the information you
need for downloading and installing Sphinx (http://sphinx-doc.org/).

The documentation is written in the rst files within the 'source' directory, which you can 
open up in your favourite text editor. When you want to generate the final documentation, 
use the `make` tool (or run `make.bat` if you're on Windows). For instance, to generate the 
HTML output of the documentation, just enter:

    $ make html

The output can be found in `buid/html/index.html`.

If you want to see the list of commands, just enter `make` without argments:

    $ make
    
    Please use `make <target>' where <target> is one of
      html       to make standalone HTML files
      dirhtml    to make HTML files named index.html in directories
      singlehtml to make a single large HTML file
      pickle     to make pickle files
      json       to make JSON files
      htmlhelp   to make HTML files and a HTML help project
      qthelp     to make HTML files and a qthelp project
      devhelp    to make HTML files and a Devhelp project
      epub       to make an epub
      latex      to make LaTeX files, you can set PAPER=a4 or PAPER=letter
      latexpdf   to make LaTeX files and run them through pdflatex
      text       to make text files
      man        to make manual pages
      changes    to make an overview of all changed/added/deprecated items
      linkcheck  to check all external links for integrity
      doctest    to run all doctests embedded in the documentation (if enabled)
      release    copy files to scm.gforge.inria.fr:/home/groups/bonfire-dev/htdocs/doc
