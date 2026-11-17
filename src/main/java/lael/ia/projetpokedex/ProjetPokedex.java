/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package lael.ia.projetpokedex;

import java.util.List;
import lael.ia.projetpokedex.dao.PokemonDAO;
import lael.ia.projetpokedex.database.DatabaseManager;
import lael.ia.projetpokedex.dao.TypeDAO;
import lael.ia.projetpokedex.model.Pokemon;
import lael.ia.projetpokedex.model.TypePokemon;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lael.ia.projetpokedex.database.DatabaseManager;

/**
 *
 * @author laelt
 */
public class ProjetPokedex extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Initialise SQLite avant d'afficher l'interface
        DatabaseManager.initialiserBase();

        // 2. Charge le fichier visuel FXML depuis src/main/resources/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pokedex.fxml"));
        Parent root = loader.load();

        // 3. Configure et affiche la fenêtre
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Mon Pokédex SQLite");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
