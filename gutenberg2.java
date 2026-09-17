import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.lang.Integer;

// Utilisation des différentes classes de Lucene 
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.util.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

public class gutenberg2 {
    
    public static String doc_titre = "";
    public static String doc_auteur = "";
    public static String doc_contenu = "";
    public static String rep_index;
    public static Analyzer analyseur;
    public static IndexWriterConfig conf;
    public static Directory dir;
    public static IndexWriter writer;
    public static int id = 0;
    public static String s_id = "ID";
    public static String s_titre = "Titre";
    public static String s_auteur = "Auteur";
    public static String s_contenu = "Contenu";
    public static String report = "---RAPPORT---\n"; //rapport qui sera afficher dans la console apres les test qui mettra 
                                                     //en evidence les temps d'operation et les tailles des ficheirs

    public static void setupindex(){
        try {
            //set up l'index et l'analyseur pour l'ajout de documents
            rep_index = ".\\lucene-4.6.1\\etext10\\";
            //rep_index = ".\\lucene-4.6.1\\etext20\\";
            //rep_index = ".\\lucene-4.6.1\\etext30\\";
            //rep_index = ".\\lucene-4.6.1\\etextxx\\";
            analyseur = new EnglishAnalyzer(Version.LUCENE_46);
            conf = new IndexWriterConfig(Version.LUCENE_46,analyseur);
            dir = FSDirectory.open(new File(rep_index));
            writer = new IndexWriter(dir, conf);
        } catch (Exception e) {
            System.err.println("Erreur rencontrée dans setupindex: " + e.toString());
        }
        
    }

    public static void addtoindex(File textFile) {
        if (id>=10) return;
        //if (id>=20) return;
        //if (id>=30) return;
        //trouver le titre et l'auteur dans le fichier texte
        try {
            Scanner scanner = new Scanner(textFile);
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                if (line.startsWith("Title: ")){
                    doc_titre = line.substring(7);
                }
                else if (line.startsWith("Author: ")){
                    doc_auteur = line.substring(8);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur rencontree dans addtoindex pendant le scan du fichier texte: " + e.getMessage());
        }
        try {
            //fabriquer le document
            Document doc = new Document();
            //id
            doc.add(new StringField(s_id, "Doc "+id, Field.Store.YES));
            //titre
            if (!doc_titre.equals("")) {
                doc.add(new StringField(s_titre, doc_titre, Field.Store.YES));
            } else {
                doc.add(new StringField(s_titre, "title-"+id+"-"+textFile.getName(), Field.Store.YES));
            }
            //auteur
            if (!doc_auteur.equals("")) {
                doc.add(new StringField(s_auteur, doc_auteur, Field.Store.YES));
            } else {
                doc.add(new StringField(s_auteur, "auteur-"+id+"-"+textFile.getName(), Field.Store.YES));
            }
            //contenu
            Reader r = new BufferedReader(new FileReader(textFile));
            org.apache.lucene.document.TextField content = new org.apache.lucene.document.TextField(s_contenu, r);
            doc.add(content);
            //ajouter le document a l'index
            writer.addDocument(doc);
            writer.commit();
            System.out.println("Document "+(id++)+" ajoute."); //INCREMENT ID!!! IMPORTANT!!!!
            return;
        } catch (Exception e) {
            System.err.println("Erreur rencontrée dans addtoindex pendant la creation du document: " + e.toString());
        }
    }


    public static void main(String[] args) {
        long totalFilesSize = 0; //taille totale des fichiers indexer en octets ///////////
        //ajout a l'index
        long avantSetUpIndexDansMain = System.currentTimeMillis(); ///////////
        try{
            long avantFCTsetupindex = System.currentTimeMillis(); ///////////
            setupindex();
            long apresFCTsetupindex = System.currentTimeMillis(); ///////////
            report += "La fonction set up index a pris "+(apresFCTsetupindex-avantFCTsetupindex)/1000.0+" secondes.\n"; ///////////
            Path projetgutenberg = Paths.get(".\\PG2003-08_files\\pg\\");
            //pour tous les ficheirs dans le repertoire du projet gutenberg
            long avantParcoursFichier = System.currentTimeMillis(); ///////////
            for (File dirFile : projetgutenberg.toFile().listFiles()) {
                //si le fichier est un repertoire et contient son nom est etext##
                if (id<10 && dirFile.isDirectory() && Pattern.matches(".\\\\PG2003-08_files\\\\pg\\\\etext\\d\\d", dirFile.getPath().toString())){
                //if (id<20 && dirFile.isDirectory() && Pattern.matches(".\\\\PG2003-08_files\\\\pg\\\\etext\\d\\d", dirFile.getPath().toString())){
                //if (id<30 && dirFile.isDirectory() && Pattern.matches(".\\\\PG2003-08_files\\\\pg\\\\etext\\d\\d", dirFile.getPath().toString())){
                //if (dirFile.isDirectory() && Pattern.matches(".\\\\PG2003-08_files\\\\pg\\\\etext\\d\\d", dirFile.getPath().toString())){
                    //pour tous les fichiers dans le repertoire etext##
                    for (File textFile : dirFile.listFiles()){
                        //si c'est un fichier texte
                        if (id<10 && textFile.getName().endsWith(".txt")){
                        //if (id<20 && textFile.getName().endsWith(".txt")){
                        //if (id<30 && textFile.getName().endsWith(".txt")){
                        //if (textFile.getName().endsWith(".txt")){
                            addtoindex(textFile);
                            totalFilesSize += textFile.length(); ///////////
                        }
                        else if (id>=10) break;
                        //else if (id>=20) break;
                        //else if (id>=30) break;
                    }
                }
                else if (id>=10) break;
                //else if (id>=20) break;
                //else if (id>=30) break;
            }
            long apresParcoursFichier = System.currentTimeMillis(); ///////////
            report += "Le parcours des fichiers a pris "+(apresParcoursFichier-avantParcoursFichier)/1000.0+" secondes.\n"; ///////////
        } catch (Exception e) {
            System.err.println("Erreur rencontrée dans main pendant l'ajout: " + e.toString());
        }
        long apresSetUpIndexDansMain = System.currentTimeMillis(); ///////////
        report += "Le set up de l'index dans main a pris "+(apresSetUpIndexDansMain-avantSetUpIndexDansMain)/1000.0+" secondes.\n"; ///////////
        report += "On a indexe "+id+" fichiers.\n";
        report += "Les fichiers indexes occupent "+totalFilesSize+" octets sur le disque.\n"; ///////////
        long indexSize = Paths.get(rep_index).toFile().length(); //taille de l'index en octets ///////////
        report += "La taille de l'index est de "+indexSize+" octets.\n"; ///////////
        //recherche dans l'index
        try {
            String[] champs_recherche = {s_titre, s_auteur, s_contenu}; 
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, champs_recherche, analyseur);
            long totalTime = 0; //temps total passe a rechercher dans l'index en millisecondes
            // Boucle permettant de faire les 100 recherches automatiquement
			for (int j=0; j<100; j++) {
                String[] recherches = {"what", "Shakespeare", "number", "complain", "law", "mention", "appreciate", "work", "school", "object"};
                //faire une recherche pour chaque mot
                for (String lettres : recherches){
                    long debutRecherche = System.currentTimeMillis(); ///////////
                    // lancement de la recherche
                    Query query = parser.parse(lettres);
                    //Query query = MultiFieldQueryParser.parse(lettres, champs_recherche, analyseur); 
                    IndexSearcher searcher =  new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(rep_index)))); 
                    TopDocs results = searcher.search(query,4096); 
                    ScoreDoc[] hits = results.scoreDocs;

                    // Impression des résultats
                    System.out.println("Resultat: " + hits.length + " documents contenaient " + lettres); 
                    for(int i = 0; i < hits.length; i++){ 
                        Document doc = searcher.doc(hits[i].doc); 
                        int lucene_id = hits[i].doc; 
                        String my_id = doc.get(s_id); 
                        String titre = doc.get(s_titre); 
                        String auteur = doc.get(s_auteur); 
                        System.out.println("ID: " + my_id + " - Lucene ID: " + lucene_id + " - Titre: " + titre + " - Auteur: " + auteur ); 
                    } 
                    long finRecherche = System.currentTimeMillis(); ///////////
                    totalTime += (finRecherche-debutRecherche); //ajout le temps de cette recherche au total ///////////
                }
            }
            report += "Les recherches ont pris en moyenne "+(totalTime)/(10*100.0)+" millisecondes par recherche.\n"; ///////////
        } catch (Exception e) {
            System.err.println("Erreur rencontrée dans main pendant la recherche: " + e.toString());
        }
        System.out.println(report); ///////////
    }
} 