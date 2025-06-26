/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

/**
 *
 * @author alumne
 */
public class Prints {
    
    public static String printHead(String title){
        return "<!DOCTYPE html>\n" +
"<head>\n" +
"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
"    <title>"+title+"</title>\n" +
"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css\"/>\n" +
"    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\" rel=\"stylesheet\"/>\n" +
"</head>\n" +
"<body>\n" +
"<nav>\n" +
"    <div class=\"nav-wrapper blue darken-1\">\n" +
"        <ul id=\"nav-mobile\" class=\"left hide-on-med-and-down\">\n" +
"            <li><a href=\"home.jsp\">Home</a></li>\n" +
"            <li><a href=\"addFile.jsp\" class=\"collection-item\">Add file</a></li>\n" +
"            <li><a href=\"ownFiles.jsp\">Own files</a></li>\n" +
"            <li><a href=\"searchDatasetGroup.jsp\">Search Dataset Group</a></li>\n" +
"            <li><a href=\"searchDataset.jsp\">Search Dataset</a></li>\n" +
"            <li><a href=\"decrypt.jsp\">Decrypt files</a></li>\n" +
"            <li><a href=\"crypt4gh.jsp\">Crypt4GH Utility</a></li>\n" +
"        </ul>\n" +
"        <ul id=\"nav-mobile2\" class=\"right hide-on-med-and-down\">\n" +
"            <li><a href=\"logout\">Logout</a></li>\n" +
"        </ul>\n" +
"    </div>\n" +
"</nav>\n" +
"\n" ;
    }
    
    public static String printFile(String name, String id){
        return "    <tr>\n" +
"        <div class=\"row\">\n" +
"            <div class=\"col s12 m6\">\n" +
"                <div class=\"card blue lighten-1\">\n" +
"                    <div class=\"card-content white-text\">\n" +
"                        <span class=\"card-title\" >File "+name+" id: "+id+"</span>\n" +
"                    </div>\n" +
"                    <div class=\"card-action blue lighten-5\">\n" +
"                        <form action=\"getDatasetGroups\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"file_id\" value=\""+id+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get DG\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addDatasetGroup.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+id+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add DG\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"deleteFile\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+id+"\">\n" +
"                            <button class=\"btn waves-effect waves-light red\" type=\"submit\">Delete file\n" +
"                            </button>\n" +
"                        </form>\n" +
"                    </div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"    </tr>\n";
    }
    
    public static String printDG(String dg_id, String name){
        return "    <tr>\n" +
"        <div class=\"row\">\n" +
"            <div class=\"col s24 m12\">\n" +
"                <div class=\"card blue lighten-1\">\n" +
"                    <div class=\"card-content white-text\">\n" +
"                        <span class=\"card-title\" >Dataset Group "+dg_id+" of file "+name+"</span>\n" +
"                    </div>\n" +
"                    <div class=\"card-action blue lighten-5\">\n" +
"                        <form action=\"getDGFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"GetMetadataContentDG\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get MD\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getDGFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"GetProtectionDG\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get PR\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getDatasets\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get DTs\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getEncDGFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get encrypted MD\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getKeys\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get secret key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"editDatasetGroup.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light yellow\" type=\"submit\">Edit\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"deleteDatasetGroup\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light red\" type=\"submit\">Delete\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addDataset.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"file_id\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add DT\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addKey.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addProtection.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add protection\n" +
"                            </button>\n" +
"                        </form>\n" +
"                    </div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"    </tr>\n";
    }
    
    public static String printDT(String dt_id, String dg_id, String name){
        return "    <tr>\n" +
"        <div class=\"row\">\n" +
"            <div class=\"col s24 m12\">\n" +
"                <div class=\"card blue lighten-1\">\n" +
"                    <div class=\"card-content white-text\">\n" +
"                        <span class=\"card-title\" >Dataset "+dt_id+" of DatasetGroup "+dg_id+" of file "+name+"</span>\n" +
"                    </div>\n" +
"                    <div class=\"card-action blue lighten-5\">\n" +
"                        <form action=\"getDTFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"GetMetadataContent\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get MD\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getDTFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"GetProtection\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get PR\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getAccessUnits\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get AUs\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getEncDTFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get encrypted MD\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getKeys\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get secret key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"editDataset.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light yellow\" type=\"submit\">Edit\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"deleteDataset\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light red\" type=\"submit\">Delete\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addAccessUnit.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"file_id\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add AU\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addKey.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addProtection.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add protection\n" +
"                            </button>\n" +
"                        </form>\n" +
"                    </div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"    </tr>\n";
    }
    
    public static String printAU(String au_id, String dt_id, String dg_id, String name){
        return "    <tr>\n" +
"        <div class=\"row\">\n" +
"            <div class=\"col s24 m12\">\n" +
"                <div class=\"card blue lighten-1\">\n" +
"                    <div class=\"card-content white-text\">\n" +
"                        <span class=\"card-title\" >Access Unit "+au_id+" of Dataset "+dt_id+" of DatasetGroup "+dg_id+" of file "+name+"</span>\n" +
"                    </div>\n" +
"                    <div class=\"card-action blue lighten-5\">\n" +
"                        <form action=\"getAUFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"data\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get data\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getAUFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <input type=\"hidden\" name=\"resource\" value=\"protection\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get PR\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getEncAUFiles\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get encrypted data\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"getKeys\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light\" type=\"submit\">Get secret key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"editAccessUnit.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light yellow\" type=\"submit\">Edit\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"deleteAccessUnit\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light red\" type=\"submit\">Delete\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addKey.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add key\n" +
"                            </button>\n" +
"                        </form>\n" +
"                        <form action=\"addProtection.jsp\" method=\"POST\" style=\"display: inline-block\">\n" +
"                            <input type=\"hidden\" name=\"au_id\" value=\""+au_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\">\n" +
"                            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\">\n" +
"                            <input type=\"hidden\" name=\"mpegfile\" value=\""+name+"\">\n" +
"                            <button class=\"btn waves-effect waves-light green\" type=\"submit\">Add protection\n" +
"                            </button>\n" +
"                        </form>\n" +
"                    </div>\n" +
"                </div>\n" +
"            </div>\n" +
"        </div>\n" +
"    </tr>\n";
    }
}
