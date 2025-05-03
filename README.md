# xml-auto-validator
Provides an option for validating XML when a schema may not be available or is not comprehensive. The tool is intended 
for use with forms, so some design decisions may not make this an ideal tool for other use cases.



Glossary:
Instance, or Form Instance - Within an XML file, the portion of the document that corresponds to one instance of the form.
Section - A grouping of data within a form.
Root Section - A section that is a child of the form root. 
Subsection - Any section that is nested within a section. Technically all sections are subsections, but the term is used 
here to refer to sections that are not root sections.
Field - Any element or attribute in a form that provides data.

Form Root - The starting point of a form instance. If there were multiple instances of a form within a file, this is the
node where the forms repeat.
Form Root Index - The first child node of an instance that we care about. 