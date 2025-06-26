/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmc.Utils;

/**
 *
 * @author alumne
 */
public class Prints {

    public static String printNav(String title){
        return
"<nav>\n" +
"    <div class=\"nav-wrapper blue darken-1\">\n" +
"        <ul id=\"nav-mobile\" class=\"left hide-on-med-and-down\">\n" +
"            <li style=\"padding: 0 15px;\">" + title +"</li>\n" +
"        </ul>" +
"        <ul id=\"nav-mobile2\" class=\"right hide-on-med-and-down\">\n" +
"            <li><a href=\"logout\">Logout</a></li>\n" +
"        </ul>\n" +
"    </div>\n" +
"</nav>\n" +
"\n" ;
    }

/*
"        <ul id=\"nav-mobile\" class=\"left hide-on-med-and-down\">\n" +
"            <li><a href=\"home.jsp\">Home</a></li>\n" +
"            <li><a href=\"addFile.jsp\" class=\"collection-item\">Add file</a></li>\n" +
"            <li><a href=\"ownFiles.jsp\">Own files</a></li>\n" +
"            <li><a href=\"searchDatasetGroup.jsp\">Search Dataset Group</a></li>\n" +
"            <li><a href=\"searchDataset.jsp\">Search Dataset</a></li>\n" +
"            <li><a href=\"decrypt.jsp\">Decrypt files</a></li>\n" +
"            <li><a href=\"crypt4gh.jsp\">Crypt4GH Utility</a></li>\n" +
"        </ul>\n" +
*/

}
