import org.apache.taglibs.standard.functions.Functions

title = Functions.escapeXml(currentNode.displayableName)
//                    +index+" "+ nbOfChilds+" "+closeUl
description = currentNode.properties['jcr:description']
linkTitle = description ? " title=\"${Functions.escapeXml(description.string)}\"" : ""

link = currentNode.url

print "<a href=\"${link}\"${linkTitle}>${title}</a>"