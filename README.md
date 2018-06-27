# Program to find clusters of near duplicate records with different classifications.

This program pairwise compares each row in the selected table and forms clusters
of rows whose contents are duplicates or near duplicates. The text column is 
converted to attributes using the same algorithm as used for SVM classification.
The dot product of each pair of attribute vectors is computed. Pairs whose
dot products are less than a specified threshold are grouped together. Attribute
vectors that are identical will have a dot product of 1.0. Attribute vectors that
contain no common elements will have a dot product of 0.0. The default threshold
is 0.7 which represents an angle of 45 degrees. The clusters are then filtered
to eliminate those whose elements are all classified the same.

Command line argument values used should be the same as those used for training
and classification.  The command arguments for this program are as follows:

<dl>
<dt>-Xmxnnnnm</dt>
<dd>This is an optional parameter, but if specified it must be first. 
The value nnnn is the number of megabytes of heap space that will be allocated
by the JVM</dd>
<dl>--datasource</dl><dd>The file providing the Datasource parameters.</dd>
<dl>--table_name</dl><dd>Table or query containing the data to be processed.</dd>
<dl>--id_column</dl><dd>Column containing the ID</dd>
<dl>--text_column</dl><dd>Column(s) containing the text</dd>
<dl>--code_column</dl><dd>Column containing the code</dd>
<dl>--output_table_name</dl><dd>Table where output is to be inserted.</dd>
<dl>--cluster_column</dl><dd>Column where the result is set</dd> 
<dl>--remove_stopwords [TRUE|FALSE|language]</dl>
<dd>If true, remove common “stop words” from the text using language specific 
stop words defined by Chris Buckley and Gerard Salton. If a language is omitted, 
the stop words are those provided by Porter.
  Default is true</dd>
<dl>--do_stemming [TRUE|FALSE|language]</dl>
<dd>If true, pass all words through the Porter stemmer. If a language is specified
 pass all words through a language-specific stemmer. The language specific 
stemmers are also defined by Porter. The one for English is an improvement over 
Porter’s original. Default is true</dd>
