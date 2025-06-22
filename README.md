# xml-auto-validator
Provides an option for validating XML when a schema may not be available or is not comprehensive. The tool is intended 
for use with forms, so some design decisions may not make this an ideal tool for other use cases.



Glossary:
Instance, or Form Instance - Within an XML file, the portion of the document that corresponds to one instance of the form.
Section - A grouping of data within a form.
Top Section - A section that is a child of the form root. 
Subsection - Any section that is nested within a section. Technically all sections are subsections, but the term is used 
here to refer to sections that are not root sections.
Field - Any element or attribute in a form that provides data.

Form Root - A unique, non-repeating, top level node of the document being parsed. May or may not line up with the start 
of a form. Everything being read must be a descendant of this node. 
Form Start - The starting point for an instance of a form. If there were multiple instances of a form within a file, 
this is the node where the forms repeat.
Form Root Index - The location of the form start index, likely 0 if the node is uniquely named. The value can be 
changed if multiple elements share the root node's name and a different element should be selected.



Example:
An XML file containing job applications is being parsed. There are three applicants, each of whom have completed the form 
separately. Their applications have been exported as XML and needs to be validated and parsed.
The application contains two parts; personal information and job history. 

The XML would be expected be structured as so:

<hrExport>                              <-- Form root, form root index = 0
	<application>                       <-- Form start, the beginning of an instance.
		<personalInfo>                  <-- Top-level section
            <applicant>                 <-- Subsection
                <name>                      <-- Could be treated as a subsection or ignored       
				    <given></given>         <-- Field
				    <family></family>
			    </name>
			    <address>                   
                    <street></street>       <-- Field
                    <city></city>
                    <state></state>
                </address>
                <contactInfo>
                    <type></type>
                    <value></value>
                </contactInfo>
            </applicant>
			<reference>                     <-- Subsection
                <name>                      
				    <given></given>         <-- Field
				    <family></family>
			    </name>
                <contactInfo>
                    <type></type>
                    <value></value>
                </contactInfo>
            </reference>
		</personalInfo>
		<jobHistory>                                    <-- Top-level section
			<job>                                       <-- Subsection
				<companyName></companyName>
				<contact>
                    <name>
				        <given></given>
				        <family></family>
			            </name>
                </contact>
				<start></start>
				<end></end>
			</job>
			<job>
				<companyName></companyName>
				<contact>
                    <name>
				        <given></given>
				        <family></family>
			            </name>
                </contact>
				<start></start>
				<end></end>
			</job>
			<job>
				<companyName></companyName>
				<contact>
                    <name>
				        <given></given>
				        <family></family>
			            </name>
                </contact>
				<start></start>
				<end></end>
			</job>
		</jobHistory>
	</application>                      <-- End of the first form instance
	<application>                       <-- Form start
		<personalInfo>                  <-- Top-level section
            <applicant>                 <-- Subsection
                <name>   
				    <given></given>
				    <family></family>
			    </name>
			    <address>                   
                    <street></street>
                    <city></city>
                    <state></state>
                </address>
                <contactInfo>
                    ...
                </contactInfo>
            </applicant>
			<reference>
                ...
            </reference>
		</personalInfo>
		<jobHistory>
			<job>
				...
			</job>
			<job>
                ...
			</job>
			<job>
                ...
			</job>
		</jobHistory>
	</application>
	<application>
		...
	</application>
</hrExport>